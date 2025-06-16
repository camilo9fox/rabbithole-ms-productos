package com.rabbithole.productos.service.impl;

import com.rabbithole.productos.dto.AnguloDTO;
import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import com.rabbithole.productos.dto.ElementoDTO;
import com.rabbithole.productos.dto.ElementoDisenoDTO;
import com.rabbithole.productos.mapper.DisenoPersonalizadoMapper;
import com.rabbithole.productos.model.*;
import com.rabbithole.productos.repository.*;
import com.rabbithole.productos.exception.DisenoProcesamientoException;
import com.rabbithole.productos.service.CloudinaryResourceService;
import com.rabbithole.productos.service.ProductoPersonalizadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones sin cambios
import com.rabbithole.productos.exception.ImageProcessingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para productos personalizados
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductoPersonalizadoServiceImpl implements ProductoPersonalizadoService {

    private final DisenoPersonalizadoRepository disenoPersonalizadoRepository;
    private final AnguloRepository anguloRepository;
    private final ElementoImagenRepository elementoImagenRepository;
    private final ElementoTextoRepository elementoTextoRepository;
    private final ColorRepository colorRepository;
    private final TallaRepository tallaRepository;
    private final EstadoDisenoRepository estadoDisenoRepository;
    private final CloudinaryResourceRepository cloudinaryResourceRepository;
    private final FuenteRepository fuenteRepository;
    
    // Clases auxiliares
    private final DisenoPersonalizadoMapper mapper;
    private final AnguloProcessor anguloProcessor;
    private final CloudinaryImageProcessor cloudinaryImageProcessor;
    private final CloudinaryResourceService cloudinaryResourceService;

    @Override
    @Transactional
    public DisenoPersonalizadoDTO crearDisenoPersonalizado(DisenoPersonalizadoDTO disenoDTO) {
        log.info("Creando nuevo diseño personalizado");
        
        try {
            // 1. Crear el diseño personalizado principal
            DisenoPersonalizado diseno = new DisenoPersonalizado();
            diseno.setUsuarioId(disenoDTO.getUsuarioId());
            diseno.setNombre(disenoDTO.getNombre() != null ? disenoDTO.getNombre() : "Polera Personalizada");
            diseno.setColor(colorRepository.findById(disenoDTO.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color no encontrado")));
            diseno.setTalla(tallaRepository.findById(disenoDTO.getTallaId())
                    .orElseThrow(() -> new RuntimeException("Talla no encontrada")));
            diseno.setPrecio(disenoDTO.getPrecio() != null ? disenoDTO.getPrecio() : BigDecimal.ZERO);
            diseno.setEstado(estadoDisenoRepository.findById(disenoDTO.getEstadoId())
                    .orElseThrow(() -> new RuntimeException("Estado no encontrado")));
            diseno.setMotivoRechazo(disenoDTO.getMotivoRechazo());
            diseno.setNotasModificacion(disenoDTO.getNotasModificacion());
            diseno.setCreadoPorAdmin(disenoDTO.getCreadoPorAdmin() != null ? disenoDTO.getCreadoPorAdmin() : false);
            
            // Guardar el diseño para obtener su ID
            DisenoPersonalizado disenoGuardado = disenoPersonalizadoRepository.save(diseno);
            
            // 2. Procesar los ángulos y sus elementos
            List<AnguloDiseno> angulos = new ArrayList<>();
            if (disenoDTO.getAngulos() != null && !disenoDTO.getAngulos().isEmpty()) {
                for (AnguloDTO anguloDTO : disenoDTO.getAngulos()) {
                    AnguloDiseno angulo = anguloProcessor.procesarAngulo(anguloDTO, disenoGuardado);
                    angulos.add(anguloRepository.save(angulo));
                }
            }
            
            // 3. Crear DTO de respuesta
            return mapper.toDTO(disenoGuardado, angulos);
        } catch (Exception e) {
            log.error("Error al crear diseño personalizado: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear diseño personalizado: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public DisenoPersonalizadoDTO obtenerDisenoPersonalizado(Long id) {
        log.info("Obteniendo diseño personalizado con id: {}", id);
        
        DisenoPersonalizado diseno = disenoPersonalizadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Diseño personalizado no encontrado con ID: " + id));
        
        List<AnguloDiseno> angulos = anguloRepository.findByDisenoPersonalizadoId(diseno.getId());
        
        return mapper.toDTO(diseno, angulos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisenoPersonalizadoDTO> obtenerDisenosPorUsuario(Long usuarioId) {
        log.info("Obteniendo diseños personalizados del usuario con id: {}", usuarioId);
        
        // Usar Pageable y obtener todos los resultados
        org.springframework.data.domain.Pageable unpaged = org.springframework.data.domain.Pageable.unpaged();
        List<DisenoPersonalizado> disenos = disenoPersonalizadoRepository.findByUsuarioId(usuarioId, unpaged).getContent();
        List<DisenoPersonalizadoDTO> resultados = new ArrayList<>();
        
        for (DisenoPersonalizado diseno : disenos) {
            List<AnguloDiseno> angulos = anguloRepository.findByDisenoPersonalizadoId(diseno.getId());
            resultados.add(mapper.toDTO(diseno, angulos));
        }
        
        return resultados;
    }
    
    /**
     * Actualiza los campos básicos de un DiseñoPersonalizado con los datos del DTO
     * @param existente Entidad existente a actualizar
     * @param dto DTO con los nuevos datos
     * @return La entidad actualizada
     */
    private DisenoPersonalizado actualizarCamposBasicos(DisenoPersonalizado existente, DisenoPersonalizadoDTO dto) {
        // Actualizar nombre
        existente.setNombre(dto.getNombre() != null ? dto.getNombre() : existente.getNombre());
        
        // Actualizar color si se proporciona
        if (dto.getColorId() != null) {
            existente.setColor(colorRepository.findById(dto.getColorId())
                .orElseThrow(() -> new RuntimeException("Color no encontrado")));
        }
        
        // Actualizar talla si se proporciona
        if (dto.getTallaId() != null) {
            existente.setTalla(tallaRepository.findById(dto.getTallaId())
                .orElseThrow(() -> new RuntimeException("Talla no encontrada")));
        }
        
        // Actualizar precio si se proporciona
        if (dto.getPrecio() != null) {
            existente.setPrecio(dto.getPrecio());
        }
        
        // Actualizar estado si se proporciona
        if (dto.getEstadoId() != null) {
            existente.setEstado(estadoDisenoRepository.findById(dto.getEstadoId())
                .orElseThrow(() -> new RuntimeException("Estado no encontrado")));
        }
        
        // Actualizar otros campos
        existente.setMotivoRechazo(dto.getMotivoRechazo());
        existente.setNotasModificacion(dto.getNotasModificacion());
        
        return existente;
    }
    
    /**
     * Extrae el public_id de una URL de Cloudinary
     * @param url URL de la imagen en Cloudinary
     * @return public_id extraído o null si no se puede extraer
     */
    private String extraerPublicIdDeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // El formato típico de URL de Cloudinary es:
        // https://res.cloudinary.com/[cloud_name]/image/upload/v[version]/[public_id].[extension]
        Pattern pattern = Pattern.compile("/upload/.*?/([^/]+)\\.[a-zA-Z0-9]+");
        Matcher matcher = pattern.matcher(url);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Si no encuentra el patrón, retorna la URL completa (como fallback)
        return url;
    }
    
    /**
     * Procesa los ángulos nuevos o actualizados
     * @param angulos Lista de DTOs de ángulos a procesar
     * @param disenoGuardado Diseño al que pertenecen los ángulos
     * @param angulosExistentesMap Mapa de ángulos existentes para identificar actualizaciones
     * @return Lista con los ángulos procesados
     */
    private List<AnguloDiseno> procesarAngulosNuevosOActualizados(
            List<AnguloDTO> angulos,
            DisenoPersonalizado disenoGuardado,
            Map<Long, AnguloDiseno> angulosExistentesMap) {
        
        List<AnguloDiseno> angulosActualizados = new ArrayList<>();
        
        for (AnguloDTO anguloDTO : angulos) {
            AnguloDiseno anguloAPersistir;
            
            try {
                if (anguloDTO.getId() != null && angulosExistentesMap.containsKey(anguloDTO.getId())) {
                    // Actualizar ángulo existente en lugar de eliminarlo y recrearlo
                    AnguloDiseno anguloExistente = angulosExistentesMap.get(anguloDTO.getId());
                    
                    // Actualizar tipo de ángulo si se proporciona
                    if (anguloDTO.getTipoAnguloId() != null && 
                        (anguloExistente.getTipoAngulo() == null || 
                         !anguloDTO.getTipoAnguloId().equals(anguloExistente.getTipoAngulo().getId()))) {
                        TipoAngulo tipoAngulo = new TipoAngulo();
                        tipoAngulo.setId(anguloDTO.getTipoAnguloId());
                        anguloExistente.setTipoAngulo(tipoAngulo);
                    }
                    
                    // Actualizar thumbnail si ha cambiado
                    if (anguloDTO.getThumbnailUrl() != null && 
                        (anguloExistente.getThumbnailResource() == null || 
                         !anguloDTO.getThumbnailUrl().equals(anguloExistente.getThumbnailResource().getUrlImagen()))) {
                        
                        // En lugar de eliminar y recrear, actualizamos el recurso existente o creamos uno nuevo
                        CloudinaryResource thumbnailResource;
                        
                        if (anguloExistente.getThumbnailResource() != null) {
                            thumbnailResource = anguloExistente.getThumbnailResource();
                            thumbnailResource.setUrlImagen(anguloDTO.getThumbnailUrl());
                            thumbnailResource.setPublicId(extraerPublicIdDeUrl(anguloDTO.getThumbnailUrl()));
                            thumbnailResource = cloudinaryResourceRepository.save(thumbnailResource);
                        } else {
                            thumbnailResource = new CloudinaryResource();
                            thumbnailResource.setUrlImagen(anguloDTO.getThumbnailUrl());
                            thumbnailResource.setPublicId(extraerPublicIdDeUrl(anguloDTO.getThumbnailUrl()));
                            thumbnailResource = cloudinaryResourceRepository.save(thumbnailResource);
                        }
                        anguloExistente.setThumbnailResource(thumbnailResource);
                    }
                    
                    // Actualizar elemento (texto o imagen) según el tipo
                    if (anguloDTO.getElemento() != null) {
                        actualizarElemento(anguloDTO.getElemento(), anguloExistente);
                    }
                    
                    // Guardar el ángulo actualizado
                    anguloAPersistir = anguloRepository.save(anguloExistente);
                    
                    // Quitar del mapa para identificar los que hay que eliminar después
                    angulosExistentesMap.remove(anguloDTO.getId());
                } else {
                    // Crear nuevo ángulo si no existe
                    anguloAPersistir = anguloProcessor.procesarAngulo(anguloDTO, disenoGuardado);
                    anguloAPersistir = anguloRepository.save(anguloAPersistir);
                }
                
                angulosActualizados.add(anguloAPersistir);
                
            } catch (ImageProcessingException e) {
                log.error("Error al procesar ángulo: {}", e.getMessage(), e);
                throw new DisenoProcesamientoException("Error al procesar ángulo del diseño ID: " + 
                        (anguloDTO.getId() != null ? anguloDTO.getId() : "nuevo"), e);
            } catch (Exception e) {
                log.error("Error inesperado procesando ángulo: {}", e.getMessage(), e);
                throw new RuntimeException("Error procesando ángulo: " + e.getMessage(), e);
            }
        }
        
        return angulosActualizados;
    }
    
    /**
     * Actualiza el elemento (imagen o texto) de un ángulo existente
     * @param elementoDTO DTO con los datos nuevos
     * @param anguloExistente Ángulo a actualizar
     */
    private void actualizarElemento(ElementoDTO elementoDTO, AnguloDiseno anguloExistente) {
        if ("IMAGEN".equalsIgnoreCase(elementoDTO.getTipo())) {
            ElementoImagen elementoImagen;
            
            // Actualizar o crear el elemento imagen
            if (anguloExistente.getElementoImagen() != null) {
                elementoImagen = anguloExistente.getElementoImagen();
            } else {
                elementoImagen = new ElementoImagen();
            }
            
            // Actualizar propiedades de diseño del elemento imagen (posición, tamaño, etc.)
            if (elementoDTO.getPropiedadesDiseno() != null) {
                ElementoDisenoDTO propiedadesDiseno = elementoDTO.getPropiedadesDiseno();
                
                if (propiedadesDiseno.getPosicionX() != null) {
                    elementoImagen.setPosicionX(propiedadesDiseno.getPosicionX().intValue());
                }
                if (propiedadesDiseno.getPosicionY() != null) {
                    elementoImagen.setPosicionY(propiedadesDiseno.getPosicionY().intValue());
                }
                if (propiedadesDiseno.getAnchura() != null) {
                    elementoImagen.setAnchura(propiedadesDiseno.getAnchura().intValue());
                }
                if (propiedadesDiseno.getAltura() != null) {
                    elementoImagen.setAltura(propiedadesDiseno.getAltura().intValue());
                }
                if (propiedadesDiseno.getRotacion() != null) {
                    elementoImagen.setRotacion(propiedadesDiseno.getRotacion());
                }
            }
            
            // Actualizar propiedades específicas del elemento imagen (URL, etc.)
            if (elementoDTO.getPropiedadesElemento() != null) {
                Map<String, Object> propiedades = elementoDTO.getPropiedadesElemento();
                
                // Obtener URL de la imagen
                Object urlObj = propiedades.get("url");
                String url = urlObj != null ? urlObj.toString() : null;
                
                if (url != null && !url.isEmpty()) {
                    // Si ya hay un recurso, actualizarlo en lugar de crear uno nuevo
                    CloudinaryResource cloudinaryResource;
                    if (elementoImagen.getCloudinaryResource() != null) {
                        cloudinaryResource = elementoImagen.getCloudinaryResource();
                        cloudinaryResource.setUrlImagen(url);
                        cloudinaryResource.setPublicId(extraerPublicIdDeUrl(url));
                        cloudinaryResource = cloudinaryResourceRepository.save(cloudinaryResource);
                    } else {
                        cloudinaryResource = new CloudinaryResource();
                        cloudinaryResource.setUrlImagen(url);
                        cloudinaryResource.setPublicId(extraerPublicIdDeUrl(url));
                        cloudinaryResource = cloudinaryResourceRepository.save(cloudinaryResource);
                    }
                    elementoImagen.setCloudinaryResource(cloudinaryResource);
                }
            }
            
            // Guardar el elemento imagen
            elementoImagen = elementoImagenRepository.save(elementoImagen);
            anguloExistente.setElementoImagen(elementoImagen);
            anguloExistente.setElementoTexto(null); // Asegurar que no hay un elemento de texto
            
        } else if ("TEXTO".equalsIgnoreCase(elementoDTO.getTipo())) {
            ElementoTexto elementoTexto;
            
            // Actualizar o crear el elemento texto
            if (anguloExistente.getElementoTexto() != null) {
                elementoTexto = anguloExistente.getElementoTexto();
            } else {
                elementoTexto = new ElementoTexto();
            }
            
            // Actualizar propiedades de diseño del elemento texto (posición, tamaño, etc.)
            if (elementoDTO.getPropiedadesDiseno() != null) {
                ElementoDisenoDTO propiedadesDiseno = elementoDTO.getPropiedadesDiseno();
                
                if (propiedadesDiseno.getPosicionX() != null) {
                    elementoTexto.setPosicionX(propiedadesDiseno.getPosicionX().intValue());
                }
                if (propiedadesDiseno.getPosicionY() != null) {
                    elementoTexto.setPosicionY(propiedadesDiseno.getPosicionY().intValue());
                }
            }
            
            // Actualizar propiedades específicas del elemento texto (contenido, fuente, color, etc.)
            if (elementoDTO.getPropiedadesElemento() != null) {
                Map<String, Object> propiedades = elementoDTO.getPropiedadesElemento();
                
                // Actualizar contenido del texto
                Object contenidoObj = propiedades.get("contenido");
                if (contenidoObj != null) {
                    elementoTexto.setContenido(contenidoObj.toString());
                }
                
                // Manejar la fuente - ElementoTexto tiene un objeto Fuente, no un String
                Object fuenteObj = propiedades.get("fuente");
                if (fuenteObj != null) {
                    // Buscar la fuente por ID en lugar de crear una nueva
                    Long fuenteId = 1L; // ID por defecto
                    if (fuenteObj instanceof Number) {
                        fuenteId = ((Number) fuenteObj).longValue();
                    } else if (fuenteObj instanceof String) {
                        try {
                            fuenteId = Long.parseLong((String) fuenteObj);
                        } catch (NumberFormatException e) {
                            log.warn("No se pudo convertir la fuente '{}' a Long, usando valor por defecto", fuenteObj);
                        }
                    }
                    
                    // Buscar fuente existente usando el repositorio para evitar problemas de entidad transitoria
                    Fuente fuente = fuenteRepository.findById(fuenteId)
                        .orElseGet(() -> {
                            // Si no encontramos la fuente, usar la fuente por defecto
                            return fuenteRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new IllegalStateException("No hay fuentes configuradas en el sistema"));
                        });
                    elementoTexto.setFuente(fuente);
                }
                
                // Manejar el color - ElementoTexto tiene un objeto Color, no un String
                Object colorObj = propiedades.get("color");
                if (colorObj != null) {
                    // Buscar el color por ID en lugar de crear uno nuevo
                    String colorId = "1"; // ID por defecto
                    if (colorObj instanceof String) {
                        colorId = (String) colorObj;
                    }
                    // Buscar color existente usando el repositorio para evitar problemas de entidad transitoria
                    Color color = colorRepository.findById(colorId)
                        .orElseGet(() -> {
                            // Si no encontramos el color, usar el color por defecto (podríamos obtenerlo del primer color disponible)
                            return colorRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new IllegalStateException("No hay colores configurados en el sistema"));
                        });
                    elementoTexto.setColorTexto(color);
                }
                
                // ElementoTexto usa 'tamanoFuente' en lugar de 'tamanoTexto'
                Object tamanoObj = propiedades.get("tamanoTexto");
                if (tamanoObj instanceof Number) {
                    elementoTexto.setTamanoFuente(((Number) tamanoObj).intValue());
                }
            }
            
            // Guardar el elemento texto
            elementoTexto = elementoTextoRepository.save(elementoTexto);
            anguloExistente.setElementoTexto(elementoTexto);
            anguloExistente.setElementoImagen(null); // Asegurar que no hay un elemento de imagen
        }
    }
    private void eliminarAngulosNoPresentes(Collection<AnguloDiseno> angulosAEliminar) {
        for (AnguloDiseno anguloAEliminar : angulosAEliminar) {
            limpiarRecursosAngulo(anguloAEliminar);
        }
    }

    @Override
    @Transactional
    public DisenoPersonalizadoDTO actualizarDisenoPersonalizado(Long id, DisenoPersonalizadoDTO disenoDTO) {
        log.info("Actualizando diseño personalizado con id: {}", id);
        
        try {
            // 1. Verificar si el diseño existe
            DisenoPersonalizado disenoExistente = disenoPersonalizadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño personalizado no encontrado con ID: " + id));
            
            // 2. Actualizar campos básicos del diseño
            disenoExistente = actualizarCamposBasicos(disenoExistente, disenoDTO);
            
            // 3. Guardar el diseño actualizado
            DisenoPersonalizado disenoGuardado = disenoPersonalizadoRepository.save(disenoExistente);
            
            // 4. Procesar los ángulos si se proporcionan
            List<AnguloDiseno> angulosActualizados = new ArrayList<>();
            
            if (disenoDTO.getAngulos() != null && !disenoDTO.getAngulos().isEmpty()) {
                // Obtener los ángulos existentes mapeados por ID
                Map<Long, AnguloDiseno> angulosExistentesMap = anguloRepository
                    .findByDisenoPersonalizadoId(id)
                    .stream()
                    .collect(Collectors.toMap(AnguloDiseno::getId, angulo -> angulo, (a, b) -> a));
                
                // Procesar ángulos nuevos y actualizados
                angulosActualizados = procesarAngulosNuevosOActualizados(
                    disenoDTO.getAngulos(), disenoGuardado, angulosExistentesMap);
                
                // Eliminar ángulos que ya no están en el DTO
                eliminarAngulosNoPresentes(angulosExistentesMap.values());
            }
            
            // 5. Crear DTO de respuesta
            return mapper.toDTO(disenoGuardado, angulosActualizados);
            
        } catch (Exception e) {
            log.error("Error al actualizar diseño personalizado: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar diseño personalizado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina de forma segura un recurso de Cloudinary si existe
     * @param resource El recurso de Cloudinary a eliminar
     * @param resourceType Descripción del tipo de recurso (para logs)
     */
    private void eliminarCloudinaryResourceSeguro(CloudinaryResource resource, String resourceType) {
        if (resource == null) return;
        try {
            cloudinaryResourceService.eliminarRecurso(resource);
            log.info("{} de Cloudinary eliminado correctamente, ID: {}", resourceType, resource.getId());
        } catch (Exception e) {
            log.warn("No se pudo eliminar el {} de Cloudinary: {}", resourceType, resource.getId(), e);
        }
    }
    
    /**
     * Elimina la imagen de Cloudinary por publicId de forma segura
     * (método usado para compatibilidad con elementos antiguos)
     * @param publicId ID público de la imagen en Cloudinary
     */
    private void eliminarImagenPorPublicIdSeguro(String publicId) {
        if (publicId == null || publicId.isEmpty()) return;
        try {
            cloudinaryImageProcessor.eliminarImagen(publicId);
            log.info("Imagen eliminada de Cloudinary por publicId: {}", publicId);
        } catch (Exception e) {
            // Convertimos cualquier excepción a runtime para mantener la firma del método
            log.warn("No se pudo eliminar la imagen de Cloudinary: {}", publicId, e);
        }
    }
    
    /**
     * Limpia los recursos asociados a un ElementoImagen
     * @param elementoImagen El elemento imagen a limpiar
     */
    private void limpiarRecursosElementoImagen(ElementoImagen elementoImagen) {
        if (elementoImagen == null) return;
        
        // Limpiar CloudinaryResource si existe
        CloudinaryResource cloudinaryResource = elementoImagen.getCloudinaryResource();
        if (cloudinaryResource != null) {
            eliminarCloudinaryResourceSeguro(cloudinaryResource, "CloudinaryResource");
        } 
        // Para compatibilidad con elementos antiguos
        else if (elementoImagen.getPublicId() != null && !elementoImagen.getPublicId().isEmpty()) {
            eliminarImagenPorPublicIdSeguro(elementoImagen.getPublicId());
        }
        
        // Eliminar el elemento imagen de la BD
        elementoImagenRepository.delete(elementoImagen);
    }
    
    /**
     * Limpia todos los recursos asociados a un ánguloDiseno
     * @param angulo El anguloDiseno a limpiar
     */
    private void limpiarRecursosAngulo(AnguloDiseno angulo) {
        if (angulo == null) return;
        
        // Eliminar elemento de texto si existe
        ElementoTexto elementoTexto = angulo.getElementoTexto();
        if (elementoTexto != null) {
            elementoTextoRepository.delete(elementoTexto);
        }
        
        // Eliminar elemento de imagen y sus recursos
        limpiarRecursosElementoImagen(angulo.getElementoImagen());
        
        // Eliminar el thumbnail del ángulo si existe
        eliminarCloudinaryResourceSeguro(angulo.getThumbnailResource(), "Thumbnail");
        
        // Eliminar el ángulo
        anguloRepository.delete(angulo);
    }

    @Override
    @Transactional
    public void eliminarDisenoPersonalizado(Long id) {
        log.info("Eliminando diseño personalizado con id: {}", id);
        
        try {
            // 1. Verificar si el diseño existe
            DisenoPersonalizado diseno = disenoPersonalizadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño personalizado no encontrado con ID: " + id));
            
            // 2. Obtener todos los ángulos del diseño
            List<AnguloDiseno> angulos = anguloRepository.findByDisenoPersonalizadoId(id);
            
            // 3. Para cada ángulo, eliminar sus elementos asociados y las imágenes en Cloudinary
            for (AnguloDiseno angulo : angulos) {
                limpiarRecursosAngulo(angulo);
            }
            
            // 4. Finalmente eliminar el diseño
            disenoPersonalizadoRepository.delete(diseno);
            
            log.info("Diseño personalizado con id: {} eliminado exitosamente", id);
        } catch (Exception e) {
            log.error("Error al eliminar diseño personalizado: {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar diseño personalizado: " + e.getMessage(), e);
        }
    }
}
