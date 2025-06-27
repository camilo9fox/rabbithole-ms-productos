package com.rabbithole.productos.service.impl;

import com.rabbithole.productos.dto.AnguloDTO;
import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import com.rabbithole.productos.dto.ElementoDTO;
import com.rabbithole.productos.dto.ElementoDisenoDTO;
import com.rabbithole.productos.mapper.DisenoPersonalizadoMapper;
import com.rabbithole.productos.model.*;
import com.rabbithole.productos.repository.*;
import com.rabbithole.productos.exception.DisenoProcesamientoException;
import com.rabbithole.productos.exception.ImageProcessingException;
import com.rabbithole.productos.service.CloudinaryResourceService;
import com.rabbithole.productos.service.ProductoPersonalizadoService;
import com.rabbithole.productos.util.Base64ImageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
            diseno.setDetalle(disenoDTO.getDetalle() != null ? disenoDTO.getDetalle() : "Polera Personalizada");
            diseno.setColor(colorRepository.findById(disenoDTO.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color no encontrado")));
            diseno.setTalla(tallaRepository.findById(disenoDTO.getTallaId())
                    .orElseThrow(() -> new RuntimeException("Talla no encontrada")));
            diseno.setPrecio(disenoDTO.getPrecio() != null ? disenoDTO.getPrecio() : BigDecimal.ZERO);
            diseno.setEstado(estadoDisenoRepository.findById(disenoDTO.getEstadoId())
                    .orElseThrow(() -> new RuntimeException("Estado no encontrado")));
            diseno.setMotivoRechazo(disenoDTO.getMotivoRechazo());
            diseno.setNotasModificacion(disenoDTO.getNotasModificacion());
            diseno.setCreadoPorAdmin(disenoDTO.getCreadoPorAdmin() != null && disenoDTO.getCreadoPorAdmin());
            
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
        // Actualizar detalle
        existente.setDetalle(dto.getDetalle() != null ? dto.getDetalle() : existente.getDetalle());
        
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
                    if (anguloDTO.getThumbnailBase64() != null && 
                        (anguloExistente.getThumbnailResource() == null || 
                         !anguloDTO.getThumbnailBase64().equals(anguloExistente.getThumbnailResource().getUrlImagen()))) {
                        
                        try {
                            // Guardar el recurso anterior para eliminarlo después si es necesario
                            CloudinaryResource oldResource = anguloExistente.getThumbnailResource();
                            CloudinaryResource thumbnailResource;
                            
                            // Verificar si es una imagen base64
                            if (Base64ImageUtil.isValidBase64(anguloDTO.getThumbnailBase64())) {
                                log.info("Detectada imagen Base64 en thumbnail. Procesando...");
                                
                                // Procesar imagen base64 y subirla a Cloudinary
                                Map<String, Object> result = cloudinaryImageProcessor.procesarYSubirImagenBase64(anguloDTO.getThumbnailBase64(), "angulo");
                                // Crear un nuevo CloudinaryResource con los datos de Cloudinary
                                thumbnailResource = new CloudinaryResource();
                                thumbnailResource.setUrlImagen((String) result.get("url"));
                                thumbnailResource.setPublicId((String) result.get("public_id"));
                            } else {
                                // Es una URL normal, actualizar normalmente
                                if (anguloExistente.getThumbnailResource() != null) {
                                    thumbnailResource = anguloExistente.getThumbnailResource();
                                    thumbnailResource.setUrlImagen(anguloDTO.getThumbnailUrl());
                                    
                                    // Extraer publicId y verificar que no sea nulo
                                    String publicId = extraerPublicIdDeUrl(anguloDTO.getThumbnailUrl());
                                    if (publicId == null || publicId.trim().isEmpty()) {
                                        if (thumbnailResource.getPublicId() == null || thumbnailResource.getPublicId().trim().isEmpty()) {
                                            publicId = "generated_" + System.currentTimeMillis();
                                            log.warn("Se generó un publicId ({}) para thumbnail porque no se pudo extraer de la URL", publicId);
                                        } else {
                                            publicId = thumbnailResource.getPublicId();
                                            log.info("Se mantuvo el publicId existente para thumbnail: {}", publicId);
                                        }
                                    }
                                    thumbnailResource.setPublicId(publicId);
                                } else {
                                    thumbnailResource = new CloudinaryResource();
                                    thumbnailResource.setUrlImagen(anguloDTO.getThumbnailUrl());
                                    
                                    String publicId = extraerPublicIdDeUrl(anguloDTO.getThumbnailUrl());
                                    if (publicId == null || publicId.trim().isEmpty()) {
                                        publicId = "generated_" + System.currentTimeMillis();
                                        log.warn("Se generó un publicId ({}) para nuevo thumbnail porque no se pudo extraer de la URL", publicId);
                                    }
                                    thumbnailResource.setPublicId(publicId);
                                }
                            }
                            
                            // Guardar el recurso y asignarlo al ángulo usando el método auxiliar para evitar error ORA-01461
                            thumbnailResource = guardarOActualizarCloudinaryResource(thumbnailResource);
                            anguloExistente.setThumbnailResource(thumbnailResource);
                            
                            // Eliminar el recurso anterior de Cloudinary si existe
                            if (oldResource != null && oldResource.getPublicId() != null) {
                                try {
                                    cloudinaryImageProcessor.eliminarImagen(oldResource.getPublicId());
                                    cloudinaryResourceRepository.delete(oldResource);
                                    log.info("Thumbnail anterior eliminado de Cloudinary: {}", oldResource.getPublicId());
                                } catch (Exception e) {
                                    log.warn("No se pudo eliminar el thumbnail anterior de Cloudinary: {}", oldResource.getPublicId(), e);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error al procesar thumbnail: {}", e.getMessage(), e);
                            throw new ImageProcessingException("Error al procesar thumbnail", e);
                        }
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
 * SOLUCIÓN PROPUESTA:
 * Este archivo contiene el método "actualizarElemento" modificado para resolver
 * los problemas con la gestión de imágenes en Cloudinary
 */

/**
 * Actualiza un elemento (imagen o texto) de un ángulo
 * @param elementoDTO El DTO con los datos del elemento
 * @param anguloExistente El ángulo al que pertenece el elemento
 */
private void actualizarElemento(ElementoDTO elementoDTO, AnguloDiseno anguloExistente) {
    // IMPORTANTE: Primero verificamos si estamos cambiando el tipo de elemento
    // Si teníamos un ElementoImagen y ahora queremos un ElementoTexto, debemos limpiar los recursos del ElementoImagen primero
    if ("TEXTO".equalsIgnoreCase(elementoDTO.getTipo()) && anguloExistente.getElementoImagen() != null) {
        log.info("Cambiando de ElementoImagen a ElementoTexto. Limpiando recursos de imagen...");
        // Limpiar los recursos de Cloudinary y eliminar la entidad ElementoImagen
        limpiarRecursosElementoImagen(anguloExistente.getElementoImagen());
        anguloExistente.setElementoImagen(null);
    }
    // Si teníamos un ElementoTexto y ahora queremos un ElementoImagen, eliminamos el ElementoTexto
    else if ("IMAGEN".equalsIgnoreCase(elementoDTO.getTipo()) && anguloExistente.getElementoTexto() != null) {
        log.info("Cambiando de ElementoTexto a ElementoImagen. Eliminando elemento texto...");
        elementoTextoRepository.delete(anguloExistente.getElementoTexto());
        anguloExistente.setElementoTexto(null);
    }
    
    // Ahora procedemos con la actualización según el tipo
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
        
        // Actualizar propiedades específicas del elemento imagen (URL o imagen base64, etc.)
        if (elementoDTO.getPropiedadesElemento() != null) {
            Map<String, Object> propiedades = elementoDTO.getPropiedadesElemento();
            
            // Obtener URL o imagen base64
            Object urlObj = propiedades.get("url");
            String url = urlObj != null ? urlObj.toString() : null;
            
            if (url != null && !url.isEmpty()) {
                CloudinaryResource cloudinaryResource;
                
                try {
                    // Verificar si la URL es una imagen base64
                    if (Base64ImageUtil.isValidBase64(url)) {
                        log.info("Detectada imagen Base64 en actualización de elemento. Procesando...");
                        
                        // Si hay un recurso previo, guardamos su public_id para eliminarlo después
                        String oldPublicId = null;
                        CloudinaryResource oldResource = null;
                        if (elementoImagen.getCloudinaryResource() != null) {
                            oldResource = elementoImagen.getCloudinaryResource();
                            oldPublicId = oldResource.getPublicId();
                        }
                        
                        // Procesar imagen base64 y subirla a Cloudinary
                        // IMPORTANTE: Esto es lo que evita que el BASE64 se guarde en la BD
                        Map<String, Object> uploadResult = cloudinaryImageProcessor.procesarYSubirImagenBase64(url, "elementoImagen");
                        
                        if (uploadResult != null && !uploadResult.isEmpty()) {
                            // Crear un nuevo recurso CloudinaryResource
                            cloudinaryResource = new CloudinaryResource();
                            cloudinaryResource.setUrlImagen((String) uploadResult.get("url"));
                            cloudinaryResource.setPublicId((String) uploadResult.get("public_id"));
                            
                            // Guardar el recurso usando el método seguro
                            cloudinaryResource = guardarOActualizarCloudinaryResource(cloudinaryResource);
                            
                            // Asignar el nuevo recurso al elemento imagen
                            elementoImagen.setCloudinaryResource(cloudinaryResource);
                            
                            // Eliminar el recurso anterior de Cloudinary si existe
                            if (oldPublicId != null) {
                                try {
                                    cloudinaryImageProcessor.eliminarImagen(oldPublicId);
                                    cloudinaryResourceRepository.delete(oldResource);
                                    log.info("Imagen anterior eliminada de Cloudinary: {}", oldPublicId);
                                } catch (Exception e) {
                                    log.warn("No se pudo eliminar la imagen anterior de Cloudinary: {}", oldPublicId, e);
                                }
                            }
                        } else {
                            throw new ImageProcessingException("Error al procesar imagen base64: resultado de carga nulo o vacío");
                        }
                    } else {
                        // Es una URL normal, actualizar normalmente
                        // Si ya existe un recurso, actualizamos sus propiedades
                        if (elementoImagen.getCloudinaryResource() != null) {
                            cloudinaryResource = elementoImagen.getCloudinaryResource();
                            
                            // Guardar la URL anterior para verificar si hubo cambio
                            String oldUrl = cloudinaryResource.getUrlImagen();
                            
                            cloudinaryResource.setUrlImagen(url);
                            
                            // Extraer publicId y verificar que no sea nulo
                            String publicId = extraerPublicIdDeUrl(url);
                            if (publicId == null || publicId.trim().isEmpty()) {
                                // Si es nulo, mantener el publicId anterior o generar uno nuevo
                                if (cloudinaryResource.getPublicId() == null || cloudinaryResource.getPublicId().trim().isEmpty()) {
                                    publicId = "generated_" + System.currentTimeMillis();
                                    log.warn("Se generó un publicId ({}) porque no se pudo extraer de la URL", publicId);
                                } else {
                                    publicId = cloudinaryResource.getPublicId();
                                    log.info("Se mantuvo el publicId existente: {}", publicId);
                                }
                            }
                            cloudinaryResource.setPublicId(publicId);
                            
                            // Actualizar el recurso en la BD usando el método seguro
                            cloudinaryResource = guardarOActualizarCloudinaryResource(cloudinaryResource);
                            
                            // Si la URL cambió, intentar eliminar la imagen anterior de Cloudinary
                            if (!url.equals(oldUrl)) {
                                String oldPublicId = extraerPublicIdDeUrl(oldUrl);
                                if (oldPublicId != null && !oldPublicId.isEmpty()) {
                                    try {
                                        cloudinaryImageProcessor.eliminarImagen(oldPublicId);
                                        log.info("Imagen anterior eliminada de Cloudinary por cambio de URL: {}", oldPublicId);
                                    } catch (Exception e) {
                                        log.warn("No se pudo eliminar la imagen anterior de Cloudinary: {}", oldPublicId, e);
                                    }
                                }
                            }
                        } else {
                            // Crear un nuevo recurso
                            cloudinaryResource = new CloudinaryResource();
                            cloudinaryResource.setUrlImagen(url);
                            
                            // Extraer publicId y verificar que no sea nulo
                            String publicId = extraerPublicIdDeUrl(url);
                            if (publicId == null || publicId.trim().isEmpty()) {
                                // Generar un publicId único basado en timestamp
                                publicId = "generated_" + System.currentTimeMillis();
                                log.warn("Se generó un publicId ({}) porque no se pudo extraer de la URL", publicId);
                            }
                            cloudinaryResource.setPublicId(publicId);
                            
                            // Guardar usando el método seguro
                            cloudinaryResource = guardarOActualizarCloudinaryResource(cloudinaryResource);
                        }
                        elementoImagen.setCloudinaryResource(cloudinaryResource);
                    }
                } catch (ImageProcessingException e) {
                    log.error("Error al procesar imagen: {}", e.getMessage(), e);
                    throw e;
                } catch (Exception e) {
                    log.error("Error al actualizar recurso en Cloudinary: {}", e.getMessage(), e);
                    throw new DisenoProcesamientoException("Error al actualizar imagen en Cloudinary", e);
                }
            }
        }
        
        // Guardar el elemento imagen
        elementoImagen = elementoImagenRepository.save(elementoImagen);
        anguloExistente.setElementoImagen(elementoImagen);
        
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
                        // Si no encontramos el color, usar el color por defecto
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
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Método auxiliar para guardar o actualizar CloudinaryResource de manera segura
     * Evita el error ORA-01461 usando SQL nativo con EntityManager y control total de la sentencia
     * @param resource El recurso a guardar o actualizar
     * @return El recurso guardado o actualizado
     */
    private CloudinaryResource guardarOActualizarCloudinaryResource(CloudinaryResource resource) {
        if (resource == null) return null;
        
        try {
            if (resource.getId() == null) {
                // Si es un nuevo recurso, crear mediante SQL nativo para evitar problemas con datos largos
                String sql = "INSERT INTO cloudinary_resources (public_id, url_imagen) VALUES (?, ?)";
                
                // Usamos PreparedStatement directamente a través de EntityManager's Connection
                // para mantener el control total sobre cómo se vinculan los parámetros
                jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
                query.setParameter(1, resource.getPublicId());
                query.setParameter(2, resource.getUrlImagen());
                query.executeUpdate();
                
                // Obtener el ID generado mediante una consulta SELECT MAX(id)
                BigDecimal newId = (BigDecimal) entityManager.createNativeQuery("SELECT MAX(id) FROM cloudinary_resources").getSingleResult();
                
                // Crear un nuevo objeto con los datos guardados
                CloudinaryResource freshResource = new CloudinaryResource();
                freshResource.setId(newId.longValue());
                freshResource.setPublicId(resource.getPublicId());
                freshResource.setUrlImagen(resource.getUrlImagen());
                
                return freshResource;
            } else {
                // Para recursos existentes, utilizamos actualizaciones parciales y verificamos longitud
                Long resourceId = resource.getId();
                String publicId = resource.getPublicId();
                String urlImagen = resource.getUrlImagen();
                
                // Desacoplar la entidad del contexto de persistencia para evitar sincronización automática
                entityManager.detach(resource);
                
                // Actualizar public_id con JDBC PreparedStatement para controlar el tipo de dato
                if (publicId != null) {
                    // Verificamos si el valor es muy largo (probablemente una imagen codificada en Base64)
                    if (publicId.length() > 255) {
                        log.warn("public_id demasiado largo ({}), truncando a 255 caracteres", publicId.length());
                        publicId = publicId.substring(0, 255); // Truncar a 255 caracteres
                    }
                    
                    // Usamos consulta preparada para actualizaciones parciales
                    jakarta.persistence.Query updatePublicId = entityManager.createNativeQuery(
                            "UPDATE cloudinary_resources SET public_id = ? WHERE id = ?");
                    updatePublicId.setParameter(1, publicId);
                    updatePublicId.setParameter(2, resourceId);
                    updatePublicId.executeUpdate();
                    
                    log.debug("public_id actualizado para id={}, longitud={}", resourceId, publicId.length());
                }
                
                // Actualizar url_imagen con JDBC PreparedStatement para controlar el tipo de dato
                if (urlImagen != null) {
                    // Verificamos si el valor es muy largo
                    if (urlImagen.length() > 255) {
                        log.warn("url_imagen demasiado largo ({}), truncando a 255 caracteres", urlImagen.length());
                        urlImagen = urlImagen.substring(0, 255); // Truncar a 255 caracteres
                    }
                    
                    // Usamos consulta preparada para actualizaciones parciales
                    jakarta.persistence.Query updateUrlImagen = entityManager.createNativeQuery(
                            "UPDATE cloudinary_resources SET url_imagen = ? WHERE id = ?");
                    updateUrlImagen.setParameter(1, urlImagen);
                    updateUrlImagen.setParameter(2, resourceId);
                    updateUrlImagen.executeUpdate();
                    
                    log.debug("url_imagen actualizado para id={}, longitud={}", resourceId, urlImagen.length());
                }
                
                // Limpiar completamente el contexto de persistencia
                entityManager.clear();
                
                // Recargar la entidad fresca desde la base de datos
                CloudinaryResource freshResource = entityManager.find(CloudinaryResource.class, resourceId);
                if (freshResource == null) {
                    return resource; // Fallback si no se encuentra
                }
                return freshResource;
            }
        } catch (Exception e) {
            log.error("Error al guardar/actualizar CloudinaryResource: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar/actualizar CloudinaryResource", e);
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
     * Limpia los recursos asociados a un ElementoImagen
     * @param elementoImagen El elemento imagen a limpiar
     */
    private void limpiarRecursosElementoImagen(ElementoImagen elementoImagen) {
        if (elementoImagen == null) return;
        
        // Guardamos una referencia al CloudinaryResource antes de eliminarlo
        CloudinaryResource cloudinaryResourceToDelete = null;
        String publicIdToDelete = null;
        
        // Verificar primero que el ElementoImagen exista en la BD
        boolean elementoExiste = elementoImagenRepository.existsById(elementoImagen.getId());
        if (!elementoExiste) {
            log.info("ElementoImagen ID={} ya no existe, no se elimina", elementoImagen.getId());
            return;
        }
        
        try {
            // Guardar referencias antes de eliminar
            if (elementoImagen.getCloudinaryResource() != null) {
                cloudinaryResourceToDelete = elementoImagen.getCloudinaryResource();
                // Desconectamos la instancia de CloudinaryResource del contexto de persistencia
                // para evitar que se actualice automáticamente
                if (cloudinaryResourceToDelete != null && cloudinaryResourceToDelete.getId() != null) {
                    cloudinaryResourceToDelete = cloudinaryResourceRepository.findById(
                            cloudinaryResourceToDelete.getId()).orElse(null);
                    log.info("Referencia a CloudinaryResource ID={} guardada para eliminación posterior", 
                           cloudinaryResourceToDelete.getId());
                }
            } 
            else if (elementoImagen.getPublicId() != null && !elementoImagen.getPublicId().isEmpty()) {
                publicIdToDelete = elementoImagen.getPublicId();
            }
            
            // Lo importante: primero eliminamos el ElementoImagen que contiene la FK
            log.info("Eliminando ElementoImagen ID={}", elementoImagen.getId());
            elementoImagenRepository.deleteById(elementoImagen.getId());
            
            // Ahora que el ElementoImagen ya no existe en la BD, eliminamos el recurso en Cloudinary
            if (cloudinaryResourceToDelete != null && cloudinaryResourceToDelete.getId() != null) {
                try {
                    log.info("Eliminando CloudinaryResource ID={}", cloudinaryResourceToDelete.getId());
                    // Usar directamente el servicio para no afectar la transacción principal
                    if (cloudinaryResourceToDelete.getPublicId() != null) {
                        cloudinaryImageProcessor.eliminarImagen(cloudinaryResourceToDelete.getPublicId());
                    }
                    cloudinaryResourceRepository.deleteById(cloudinaryResourceToDelete.getId());
                    log.info("CloudinaryResource ID={} eliminado exitosamente", cloudinaryResourceToDelete.getId());
                } catch (Exception ex) {
                    log.warn("No se pudo eliminar CloudinaryResource ID={}: {}", 
                            cloudinaryResourceToDelete.getId(), ex.getMessage());
                }
            } 
            else if (publicIdToDelete != null) {
                try {
                    log.info("Eliminando imagen por publicId: {}", publicIdToDelete);
                    cloudinaryImageProcessor.eliminarImagen(publicIdToDelete);
                } catch (Exception ex) {
                    log.warn("No se pudo eliminar imagen con publicId {}: {}", 
                            publicIdToDelete, ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error al limpiar recursos de ElementoImagen ID={}: {}", 
                      elementoImagen.getId(), e.getMessage());
            throw e; // Re-lanzamos la excepción para manejar correctamente la transacción
        }
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
    
    @Override
    @Transactional
    public DisenoPersonalizadoDTO actualizarEstadoDiseno(Long id, Long estadoId, String motivoRechazo, String notasModificacion) {
        log.info("Actualizando estado del diseño personalizado con id: {} a estadoId: {}", id, estadoId);
        
        try {
            // 1. Verificar si el diseño existe
            DisenoPersonalizado diseno = disenoPersonalizadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño personalizado no encontrado con ID: " + id));
            
            // 2. Verificar si el estado existe
            EstadoDiseno nuevoEstado = estadoDisenoRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado de diseño no encontrado con ID: " + estadoId));
            
            // 3. Actualizar el estado del diseño
            diseno.setEstado(nuevoEstado);
            
            // 4. Actualizar campos adicionales si se proporcionan
            if (motivoRechazo != null) {
                diseno.setMotivoRechazo(motivoRechazo);
            }
            
            if (notasModificacion != null) {
                diseno.setNotasModificacion(notasModificacion);
            }
            
            // 5. Guardar el diseño actualizado
            DisenoPersonalizado disenoActualizado = disenoPersonalizadoRepository.save(diseno);
            
            log.info("Estado del diseño personalizado con id: {} actualizado exitosamente a: {}", 
                    id, nuevoEstado.getNombre());
            
            // 6. Convertir y devolver el DTO actualizado
            return mapper.toDTO(disenoActualizado);
        } catch (Exception e) {
            log.error("Error al actualizar estado del diseño personalizado: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar estado del diseño personalizado: " + e.getMessage(), e);
        }
    }
}
