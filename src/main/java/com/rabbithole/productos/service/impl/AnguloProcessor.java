package com.rabbithole.productos.service.impl;

import com.cloudinary.utils.ObjectUtils;
import com.rabbithole.productos.dto.AnguloDTO;
import com.rabbithole.productos.dto.ElementoDTO;
import com.rabbithole.productos.dto.ElementoDisenoDTO;
import com.rabbithole.productos.exception.ImageProcessingException;
import com.rabbithole.productos.exception.ResourceNotFoundException;
import com.rabbithole.productos.model.*;
import com.rabbithole.productos.repository.*;
import com.rabbithole.productos.service.CloudinaryResourceService;
import com.rabbithole.productos.service.CloudinaryService;
import com.rabbithole.productos.util.Base64ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Clase para procesar los ángulos de un diseño personalizado
 */
@Service
@Slf4j
public class AnguloProcessor {
    
    /**
     * Busca recursivamente una imagen base64 en un mapa anidado o en arrays
     * @param data El mapa o lista que puede contener la imagen
     * @param posiblesKeys Conjunto de claves posibles donde puede estar la imagen
     * @return La primera imagen base64 encontrada o null si no hay ninguna
     */
    private String buscarImagenRecursivamente(Map<String, Object> data, String[] posiblesKeys) {
        if (data == null) {
            return null;
        }
        
        // 1. Primero buscar en las claves principales
        for (String key : posiblesKeys) {
            if (data.containsKey(key) && data.get(key) instanceof String valor && esImagenBase64(valor)) {
                log.info("Imagen base64 encontrada en la clave: {}", key);
                return valor;
            }
        }
        
        // 2. Si no se encontró, buscar recursivamente en subobjetos
        for (Object valor : data.values()) {
            // Si es un mapa anidado, buscar recursivamente
            if (valor instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) valor;
                String resultado = buscarImagenRecursivamente(subMap, posiblesKeys);
                if (resultado != null) {
                    return resultado;
                }
            } 
            // Si es una lista, buscar en cada elemento de la lista
            else if (valor instanceof List) {
                List<?> lista = (List<?>) valor;
                for (Object item : lista) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> subMap = (Map<String, Object>) item;
                        String resultado = buscarImagenRecursivamente(subMap, posiblesKeys);
                        if (resultado != null) {
                            return resultado;
                        }
                    } else if (item instanceof String string && esImagenBase64(string)) {
                        return string;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Determina si una cadena parece ser una imagen en formato base64
     * @param texto La cadena a evaluar
     * @return true si parece una imagen base64, false en caso contrario
     */
    private boolean esImagenBase64(String texto) {
        if (texto == null || texto.length() < 50) {
            return false;
        }
        
        // Verificar si comienza con algún prefijo común de base64 para imágenes
        if (texto.startsWith(DATA_PREFIX)) {
            return texto.contains("image/") && texto.contains("base64,");
        }
        
        // Si no tiene prefijo, verificar si tiene una longitud razonable y solo caracteres válidos de base64
        // Base64 suele tener longitud múltiplo de 4 y usar solo ciertos caracteres
        if (texto.length() % 4 == 0 || texto.length() % 4 == 3) { // Permitimos algo de flexibilidad
            // Verificar si solo contiene caracteres válidos de base64
            return texto.matches("^[A-Za-z0-9+/=]+$");
        }
        
        return false;
    }

    // Constantes para tipos de medios
    private static final String IMAGE_JPEG = "image/jpeg";
    // Constantes inmutables
    private static final String TIPO_TEXTO = "texto";
    private static final String TIPO_IMAGEN = "imagen";
    private static final String IMAGEN_BASE64 = "imagenBase64";
    private static final String IMAGEN_URL = "imagenUrl";
    private static final String IMAGEN_ID = "imagenId";
    private static final String THUMBNAIL_BASE64 = "thumbnailBase64";
    private static final String DATA_PREFIX = "data:";
    private static final String DEFAULT_FILENAME = "thumbnail.jpg";
    private static final String CONTENIDO = "contenido";
    private static final String POSICION_X = "posicionX";
    private static final String POSICION_Y = "posicionY";
    private static final String ROTACION = "rotacion";
    private static final String TAMANO = "tamano";
    
    // Constantes para colores y fuentes por defecto
    private static final String DEFAULT_COLOR_ID = "black";
    private static final Long DEFAULT_FONT_ID = 1L;
    
    // Repositorios y servicios
    private final TipoAnguloRepository tipoAnguloRepository;
    private final ElementoTextoRepository elementoTextoRepository;
    private final ElementoImagenRepository elementoImagenRepository;
    private final ColorRepository colorRepository;
    private final FuenteRepository fuenteRepository;
    private final PosicionRepository posicionRepository;
    private final CloudinaryResourceService cloudinaryResourceService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public AnguloProcessor(
            TipoAnguloRepository tipoAnguloRepository,
            ElementoTextoRepository elementoTextoRepository,
            ElementoImagenRepository elementoImagenRepository,
            ColorRepository colorRepository,
            FuenteRepository fuenteRepository,
            PosicionRepository posicionRepository,
            CloudinaryResourceService cloudinaryResourceService,
            CloudinaryService cloudinaryService) {
        this.tipoAnguloRepository = tipoAnguloRepository;
        this.elementoTextoRepository = elementoTextoRepository;
        this.elementoImagenRepository = elementoImagenRepository;
        this.colorRepository = colorRepository;
        this.fuenteRepository = fuenteRepository;
        this.posicionRepository = posicionRepository;
        this.cloudinaryResourceService = cloudinaryResourceService;
        this.cloudinaryService = cloudinaryService;
    }
    
    /**
     * Procesa un ángulo de diseño a partir de un DTO
     * @param anguloDTO El DTO del ángulo a procesar
     * @param disenoPersonalizado El diseño personalizado al que pertenece el ángulo
     * @return El ángulo procesado y listo para guardar
     * @throws ImageProcessingException Si ocurre un error al procesar alguna imagen
     */
    public AnguloDiseno procesarAngulo(AnguloDTO anguloDTO, DisenoPersonalizado disenoPersonalizado) throws ImageProcessingException {
        log.info("Procesando ángulo: {}", anguloDTO.getTipoAnguloId());
        
        // Crear nuevo ánguloDiseno
        AnguloDiseno anguloDiseno = new AnguloDiseno();
        
        // Establecer valores básicos
        TipoAngulo tipoAngulo = tipoAnguloRepository.findById(anguloDTO.getTipoAnguloId())
                .orElseThrow(() -> new ResourceNotFoundException("TipoAngulo no encontrado con ID: " + anguloDTO.getTipoAnguloId()));
        anguloDiseno.setTipoAngulo(tipoAngulo);
        
        // Asignar diseño personalizado
        anguloDiseno.setDisenoPersonalizado(disenoPersonalizado);
        
        // Procesar la imagen del thumbnail si existe
        String thumbnailBase64 = anguloDTO.getThumbnailBase64();
        String thumbnailUrl = anguloDTO.getThumbnailUrl();
        
        if (thumbnailBase64 != null && !thumbnailBase64.isEmpty()) {
            procesarThumbnailBase64(anguloDiseno, thumbnailBase64);
        } else if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            procesarThumbnailUrl(anguloDiseno, thumbnailUrl);
        }
        
        // Procesar el elemento (texto o imagen) si existe
        if (anguloDTO.getElemento() != null) {
            procesarElemento(anguloDiseno, anguloDTO.getElemento());
        }
        
        return anguloDiseno;
    }

    /**
     * Procesa un elemento (texto o imagen) de un ángulo
     * @param anguloDiseno El ángulo al que asignar el elemento
     * @param elementoDTO El DTO del elemento
     * @throws ImageProcessingException Si ocurre un error al procesar una imagen
     */
    private void procesarElemento(AnguloDiseno anguloDiseno, Object elementoDTO) throws ImageProcessingException {
        try {
            log.info("Procesando elemento: {}", elementoDTO.getClass().getName());
            
            // Manejar el caso de un ElementoDTO específico (estructura conocida)
            if (elementoDTO instanceof ElementoDTO elemento) {
                log.info("Procesando ElementoDTO");
                String tipo = elemento.getTipo();
                log.info("Tipo: {}", tipo);
                log.info("PropiedadesDiseno: {}", elemento.getPropiedadesDiseno());
                log.info("PropiedadesElemento keys: {}", elemento.getPropiedadesElemento() != null ? 
                        elemento.getPropiedadesElemento().keySet() : "null");
                
                Map<String, Object> propiedades = elemento.getPropiedadesElemento();
                if (propiedades == null) {
                    log.warn("El elemento no contiene propiedades");
                    return;
                }
                
                // Buscar thumbnailBase64 en las propiedades del elemento
                if (propiedades.containsKey(THUMBNAIL_BASE64)) {
                    Object value = propiedades.get(THUMBNAIL_BASE64);
                    if (value instanceof String string && esImagenBase64(string)) {
                        log.info("Se encontró thumbnailBase64 en las propiedades del elemento");
                        // No es necesario hacer nada más ya que está en el mapa de propiedades
                    }
                }
                
                // Verificar si hay propiedades de diseño y transferirlas a propiedades del elemento
                ElementoDisenoDTO propiedadesDiseno = elemento.getPropiedadesDiseno();
                if (propiedadesDiseno != null) {
                    log.info("PropiedadesDiseno: {}", propiedadesDiseno);
                    // Transferir posiciones desde propiedadesDiseno a propiedades
                    if (propiedadesDiseno.getPosicionX() != null) {
                        propiedades.put(POSICION_X, propiedadesDiseno.getPosicionX());
                        log.info("Transfiriendo posicionX: {} a elementoData", propiedadesDiseno.getPosicionX());
                    }
                    
                    if (propiedadesDiseno.getPosicionY() != null) {
                        propiedades.put(POSICION_Y, propiedadesDiseno.getPosicionY());
                        log.info("Transfiriendo posicionY: {} a elementoData", propiedadesDiseno.getPosicionY());
                    }
                    
                    // También transferir otros valores que puedan ser útiles
                    if (propiedadesDiseno.getRotacion() != null) {
                        propiedades.put(ROTACION, propiedadesDiseno.getRotacion());
                    }
                    if (propiedadesDiseno.getAnchura() != null) {
                        propiedades.put("ancho", propiedadesDiseno.getAnchura());
                    }
                    if (propiedadesDiseno.getAltura() != null) {
                        propiedades.put("alto", propiedadesDiseno.getAltura());
                    }
                }
                
                // Procesar según el tipo de elemento
                if (tipo.equalsIgnoreCase(TIPO_TEXTO)) {
                    procesarElementoTexto(anguloDiseno, propiedades);
                } else if (tipo.equalsIgnoreCase(TIPO_IMAGEN)) {
                    procesarElementoImagen(anguloDiseno, propiedades);
                } else {
                    log.warn("Tipo de elemento desconocido: {}", tipo);
                }
            } 
            // Manejar el caso de un Map genérico (formato JSON)
            else if (elementoDTO instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> elementoMap = (Map<String, Object>) elementoDTO;
                log.info("Procesando elemento como Map. Claves disponibles: {}", elementoMap.keySet());
                
                // Verificar si el mapa tiene un tipo
                Object tipoObj = elementoMap.get("tipo");
                if (tipoObj == null) {
                    log.error("El elemento no tiene un tipo definido");
                    return;
                }
                
                String tipo = tipoObj.toString();
                log.info("Tipo de elemento en Map: {}", tipo);
                
                // Procesar según el tipo de elemento
                if (tipo.equalsIgnoreCase(TIPO_TEXTO)) {
                    procesarElementoTexto(anguloDiseno, elementoMap);
                } else if (tipo.equalsIgnoreCase(TIPO_IMAGEN)) {
                    procesarElementoImagen(anguloDiseno, elementoMap);
                } else {
                    log.warn("Tipo de elemento desconocido: {}", tipo);
                }
            } else {
                log.error("Tipo de elemento no soportado: {}", elementoDTO.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error al procesar elemento: {}", e.getMessage(), e);
            throw new ImageProcessingException("Error al procesar elemento", e);
        }
    }
    


    /**
     * Procesa la imagen thumbnail de un ángulo desde un string base64
     * @param anguloDiseno El ángulo al que asignar el thumbnail
     * @param base64Image String en formato base64 con la imagen
     * @throws ImageProcessingException Si ocurre un error al procesar la imagen
     */
    private void procesarThumbnailBase64(AnguloDiseno anguloDiseno, String base64Image) throws ImageProcessingException {
        try {
            // Convertir base64 a MultipartFile
            MultipartFile thumbnailFile = Base64ImageUtil.createMultipartFileFromBase64(base64Image);
            
            // Subir imagen a Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadOptions = ObjectUtils.asMap("folder", "thumbnails");
            Map<String, Object> result = cloudinaryService.subirImagen(thumbnailFile, uploadOptions);

            // Crear un CloudinaryResource con la información
            CloudinaryResource thumbnailResource = cloudinaryResourceService.crearRecurso(
                (String) result.get("url"),
                (String) result.get("public_id"),
                thumbnailFile.getContentType() != null ? thumbnailFile.getContentType() : IMAGE_JPEG,
                thumbnailFile.getOriginalFilename() != null ? thumbnailFile.getOriginalFilename() : DEFAULT_FILENAME,
                thumbnailFile.getSize()
            );

            // Asignar el recurso thumbnail al ángulo
            anguloDiseno.setThumbnailResource(thumbnailResource);
        } catch (Exception e) {
            log.error("Error al subir thumbnail: {}", e.getMessage());
            throw new ImageProcessingException("Error al subir imagen de thumbnail", e);
        }
    }

/**
 * Procesa la imagen thumbnail de un ángulo desde una URL
 * @param anguloDiseno El ángulo al que asignar el thumbnail
 * @param thumbnailUrl URL de la imagen thumbnail
 * @throws ImageProcessingException Si ocurre un error al procesar la imagen
 */
private void procesarThumbnailUrl(AnguloDiseno anguloDiseno, String thumbnailUrl) throws ImageProcessingException {
    String publicId = null;

    // Intentar extraer publicId si es una URL de Cloudinary
    try {
        publicId = cloudinaryService.extractPublicIdFromUrl(thumbnailUrl);
    } catch (Exception e) {
        log.error("Error al extraer publicId de {}", thumbnailUrl, e);
        throw new ImageProcessingException("Error al procesar imagen de thumbnail", e);
    }
    
    if (publicId == null) {
        publicId = "external-thumbnail-" + System.currentTimeMillis();
    }

    // Crear el recurso con la URL existente
    CloudinaryResource thumbnailResource = cloudinaryResourceService.crearRecurso(
        thumbnailUrl,
        publicId,
        IMAGE_JPEG,  // Asumimos un tipo predeterminado
        DEFAULT_FILENAME,
        null
    );

    // Asignar el recurso thumbnail al ángulo
    anguloDiseno.setThumbnailResource(thumbnailResource);
}

/**
 * Procesa un elemento de texto para un ángulo
 * @param anguloDiseno El ángulo al que asignar el elemento
 * @param elementoData Datos del elemento de texto
 */
private void procesarElementoTexto(AnguloDiseno anguloDiseno, Map<String, Object> elementoData) {
    // Verificar que el contenido no sea nulo o vacío
    String contenido = null;
    if (elementoData.get(CONTENIDO) != null) {
        contenido = String.valueOf(elementoData.get(CONTENIDO));
    } else if (elementoData.get(TIPO_TEXTO) != null) {
        // Soporte alternativo para la propiedad TIPO_TEXTO si "contenido" no está presente
        contenido = String.valueOf(elementoData.get(TIPO_TEXTO));
        log.info("Usando propiedad '{}' en lugar de '{}' para compatibilidad", TIPO_TEXTO, CONTENIDO);
    }
    
    if (contenido == null || contenido.trim().isEmpty()) {
        contenido = "Texto por defecto"; // Valor por defecto si está vacío
        log.warn("El campo contenido es obligatorio pero está vacío. Se usará un valor por defecto.");
    }

    // Procesar elemento de texto
    ElementoTexto elementoTexto = new ElementoTexto();
    elementoTexto.setContenido(contenido);
    
    // Obtener color del texto - Los color_id son strings, no números
    Color color = null;
    String colorPayloadId = null;
    
    if (elementoData.get("colorId") != null) {
        colorPayloadId = String.valueOf(elementoData.get("colorId"));
        if (colorPayloadId != null && !colorPayloadId.trim().isEmpty()) {
            log.info("Buscando color con ID: {}", colorPayloadId);
            color = colorRepository.findById(colorPayloadId)
                    .orElse(null);
            
            if (color == null) {
                // Si el color no existe en la base de datos pero se proporcionó en el payload,
                // utilizamos el valor del payload directamente
                log.warn("Color no encontrado con id: {}. Intentando usar este valor directamente.", colorPayloadId);
                
                // Verificamos si podemos crear un color temporal
                try {
                    // Crear una instancia temporal de color con todos los campos requeridos
                    Color nuevoColor = new Color();
                    nuevoColor.setId(colorPayloadId);
                    nuevoColor.setNombre(colorPayloadId);
                    nuevoColor.setCodigoHex(colorPayloadId.equals("white") ? "#FFFFFF" : "#000000");
                    nuevoColor.setTextoPreview("Texto de prueba");
                    nuevoColor.setPrecioAdicional(0);
                    
                    color = colorRepository.save(nuevoColor);
                    log.info("Nuevo color creado con id: {}", colorPayloadId);
                } catch (Exception e) {
                    log.error("Error al crear nuevo color: {}", e.getMessage(), e);
                    log.warn("Usando color por defecto en su lugar");
                    color = obtenerColorPorDefecto();
                }
            }
        }
    }
    
    // Si no se proporcionó un color o el ID no era válido, usar color por defecto
    if (color == null) {
        color = obtenerColorPorDefecto();
        log.info("Usando color por defecto: {}", color.getId());
    } else {
        log.info("Usando color asignado: {}", color.getId());
    }
    
    log.debug("Color final a asignar: ID={}, Nombre={}, HexCode={}", 
            color.getId(), color.getNombre(), color.getCodigoHex());
    elementoTexto.setColorTexto(color);
    
    // Obtener fuente del texto
    Fuente fuente = null;
    if (elementoData.get("fuenteId") != null) {
        Long fuenteId = parseLong(elementoData.get("fuenteId"));
        if (fuenteId != null) {
            fuente = fuenteRepository.findById(fuenteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Fuente no encontrada con id: " + fuenteId));
        }
    }
    
    // Si no se proporcionó una fuente o el ID no era válido, usar fuente por defecto
    if (fuente == null) {
        fuente = obtenerFuentePorDefecto();
        log.info("Usando fuente por defecto: {}", fuente.getId());
    }
    
    elementoTexto.setFuente(fuente);
    
    // Configurar posición y tamaño
    Float posX = parseFloat(elementoData.get(POSICION_X), 250.0f);
    Float posY = parseFloat(elementoData.get(POSICION_Y), 250.0f);
    
    log.info("Valores de posición recibidos en el payload - X: {}, Y: {}", 
            elementoData.get(POSICION_X), elementoData.get(POSICION_Y));
    log.info("Valores de posición después de parseFloat - X: {}, Y: {}", posX, posY);
    
    // Crear y guardar el objeto Posicion ANTES de asignarlo
    Posicion posicion = new Posicion();
    posicion.setPosicionX(posX.intValue());
    posicion.setPosicionY(posY.intValue());
    posicion.setCreadoEn(LocalDateTime.now());
    
    log.debug("Posicion antes de guardar - X: {}, Y: {}", 
            posicion.getPosicionX(), posicion.getPosicionY());
    
    try {
        // Guardar la posición primero para obtener un ID válido
        posicion = posicionRepository.save(posicion);
        log.info("Posición guardada correctamente con ID: {}, X: {}, Y: {}", 
                posicion.getId(), posicion.getPosicionX(), posicion.getPosicionY());
        
        // Asignar la posición guardada al elemento de texto
        elementoTexto.setPosicion(posicion);
    } catch (Exception e) {
        log.error("Error al guardar la posición: {}", e.getMessage());
        throw new IllegalArgumentException("Error al guardar la posición: " + e.getMessage(), e);
    }
    
    // Establecer el tamaño de fuente
    if (elementoData.get(TAMANO) != null) {
        Float tamano = parseFloat(elementoData.get(TAMANO), 12.0f); // Valor por defecto 12 si hay error
        elementoTexto.setTamanoFuente(tamano.intValue());
    } else {
        elementoTexto.setTamanoFuente(12); // Valor por defecto si no se proporciona
    }
    
    // Guardar y asociar al ángulo
    ElementoTexto textoGuardado = elementoTextoRepository.save(elementoTexto);
    anguloDiseno.setElementoTexto(textoGuardado);
}

/**
 * Configura los atributos visuales del elemento imagen como posición, escala, etc.
 * @param elementoImagen El elemento de imagen a configurar
 * @param elementoData Datos del elemento
 */
private void configurarAtributosElementoImagen(ElementoImagen elementoImagen, Map<String, Object> elementoData) {
    // Posición X
    if (elementoData.containsKey(POSICION_X)) {
        Float posX = parseFloat(elementoData.get(POSICION_X), 0.0f);
        elementoImagen.setPosicionX(posX.intValue());
    } else {
        elementoImagen.setPosicionX(0); // Valor por defecto
    }
    
    // Posición Y
    if (elementoData.containsKey(POSICION_Y)) {
        Float posY = parseFloat(elementoData.get(POSICION_Y), 0.0f);
        elementoImagen.setPosicionY(posY.intValue());
    } else {
        elementoImagen.setPosicionY(0); // Valor por defecto
    }
    
    // Rotación
    if (elementoData.containsKey(ROTACION)) {
        Float rotacion = parseFloat(elementoData.get(ROTACION), 0.0f);
        elementoImagen.setRotacion(rotacion);
    } else {
        elementoImagen.setRotacion(0.0f); // Valor por defecto
    }
    
    // Ancho (obligatorio)
    int anchoDefault = 200; // Valor predeterminado
    if (elementoData.containsKey("ancho")) {
        Integer ancho = parseInt(elementoData.get("ancho"), anchoDefault);
        elementoImagen.setAncho(ancho);
    } else if (elementoData.containsKey("width")) {
        Integer ancho = parseInt(elementoData.get("width"), anchoDefault);
        elementoImagen.setAncho(ancho);
    } else {
        log.info("Estableciendo ancho por defecto: {}", anchoDefault);
        elementoImagen.setAncho(anchoDefault);
    }
    
    // Alto (obligatorio)
    int altoDefault = 200; // Valor predeterminado
    if (elementoData.containsKey("alto")) {
        Integer alto = parseInt(elementoData.get("alto"), altoDefault);
        elementoImagen.setAlto(alto);
    } else if (elementoData.containsKey("height")) {
        Integer alto = parseInt(elementoData.get("height"), altoDefault);
        elementoImagen.setAlto(alto);
    } else {
        log.info("Estableciendo alto por defecto: {}", altoDefault);
        elementoImagen.setAlto(altoDefault);
    }
}

/**
 * Procesa un elemento de imagen para un ángulo
 * @param anguloDiseno El ángulo al que asignar el elemento
 * @param elementoData Datos del elemento de imagen
 * @throws ImageProcessingException Si ocurre un error al procesar la imagen
 */
private void procesarElementoImagen(AnguloDiseno anguloDiseno, Map<String, Object> elementoData) throws ImageProcessingException {
    try {
        ElementoImagen elementoImagen = new ElementoImagen();
        
        // Procesar la imagen (base64, url o id)
        procesarImagenElemento(elementoImagen, elementoData);
        
        // Configurar otros atributos del elemento como posición, rotación, etc.
        configurarAtributosElementoImagen(elementoImagen, elementoData);
        
        // Crear y guardar la Posicion primero
        Posicion posicion;
        if (elementoImagen.getPosicion() == null) {
            posicion = new Posicion();
            elementoImagen.setPosicion(posicion);
        } else {
            posicion = elementoImagen.getPosicion();
        }
        
        // IMPORTANTE: Transferir explícitamente los valores de posicionX y posicionY al objeto Posicion
        Integer posX = elementoImagen.getPosicionX();
        if (posX == null) {
            posX = 250; // Valor por defecto centrado
            elementoImagen.setPosicionX(posX);
        }
        posicion.setPosicionX(posX);
        
        Integer posY = elementoImagen.getPosicionY();
        if (posY == null) {
            posY = 250; // Valor por defecto centrado
            elementoImagen.setPosicionY(posY);
        }
        posicion.setPosicionY(posY);
        
        // Añadir logging para debug
        log.info("Guardando Posicion con X: {}, Y: {}", posicion.getPosicionX(), posicion.getPosicionY());
        
        // Guardar la posición primero
        Posicion posicionGuardada = posicionRepository.save(posicion);
        elementoImagen.setPosicion(posicionGuardada);
        
        // Ahora guardar la imagen y establecer el elemento en el ángulo
        ElementoImagen imagenGuardada = elementoImagenRepository.save(elementoImagen);
        anguloDiseno.setElementoImagen(imagenGuardada);
        
        log.info("Elemento de imagen guardado con ID: {} y posición ID: {}", 
                imagenGuardada.getId(), 
                imagenGuardada.getPosicion() != null ? imagenGuardada.getPosicion().getId() : "null");
    } catch (Exception e) {
        log.error("Error al procesar elemento de imagen: {}", e.getMessage(), e);
        throw new ImageProcessingException("Error al procesar elemento de imagen", e);
    }
}

/**
 * Procesa la imagen de un elemento de imagen
 * @param elementoImagen Elemento de imagen a procesar
 * @param elementoData Datos del elemento
 * @throws ImageProcessingException Si ocurre un error al procesar la imagen
 */
private void procesarImagenElemento(ElementoImagen elementoImagen, Map<String, Object> elementoData) throws ImageProcessingException {
    final String[] posiblesKeysBase64 = { IMAGEN_BASE64, THUMBNAIL_BASE64, TIPO_IMAGEN, "src", "source", "imageData", "thumbnailData", "url", "uri", "imageSource" };
    final String[] posiblesKeysUrl = { IMAGEN_URL, "url", "src", "source", "imageUrl", "thumbnailUrl", "uri", "link", "imageSource" };
    final String[] posiblesKeysId = { IMAGEN_ID, "id", "imageId", "publicId", "cloudinaryId", "resourceId", "imgId" };

    log.info("Procesando elemento imagen con {} propiedades", elementoData.size());
    log.info("Claves disponibles en elementoData: {}", elementoData.keySet());

    String base64Image = buscarImagenRecursivamente(elementoData, posiblesKeysBase64);
    String imageUrl = null;
    Long imageId = null;

    if (base64Image == null) {
        for (String key : posiblesKeysUrl) {
            if (elementoData.containsKey(key) && elementoData.get(key) instanceof String strUrl) {
                imageUrl = strUrl;
                log.info("URL de imagen encontrada en clave: {}", key);
                break;
            }
        }
    }

    
    // Si no hay URL ni base64, buscar ID
    if (base64Image == null && imageUrl == null) {
        for (String key : posiblesKeysId) {
            if (elementoData.containsKey(key)) {
                if (elementoData.get(key) instanceof Number number) {
                    imageId = number.longValue();
                    log.info("ID numérico de imagen encontrado en clave: {}", key);
                } else if (elementoData.get(key) instanceof String strId) {
                    try {
                        imageId = Long.parseLong(strId);
                        log.info("ID string de imagen encontrado en clave: {}", key);
                    } catch (NumberFormatException e) {
                        log.warn("No se pudo convertir '{}' a Long", strId);
                    }
                }
                break;
            }
        }
    }
    
    // Procesar imagen
    if (base64Image != null) {
        procesarImagenBase64(elementoImagen, base64Image);
    } else if (imageUrl != null) {
        procesarImagenUrl(elementoImagen, imageUrl);
    } else if (imageId != null) {
        procesarImagenPorId(elementoImagen, imageId);
    } else {
        throw new ImageProcessingException("No se encontró imagen en las propiedades del elemento");
    }
}

/**
 * Procesa una imagen base64 y la asocia al elemento imagen
 * @param elementoImagen El elemento de imagen a actualizar
 * @param base64Image Cadena de imagen en formato base64
 * @throws ImageProcessingException Si ocurre un error al procesar la imagen
 */
private void procesarImagenBase64(ElementoImagen elementoImagen, String base64Image) throws ImageProcessingException {
    try {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new IllegalArgumentException("La imagen base64 no puede ser nula o vacía");
        }
        
        // Convertir base64 a MultipartFile
        MultipartFile imageFile = Base64ImageUtil.createMultipartFileFromBase64(base64Image);
        log.info("Imagen convertida a MultipartFile: {}, tamaño: {}", imageFile.getName(), imageFile.getSize());
        
        // Subir a Cloudinary
        Map<String, Object> uploadResult = cloudinaryService.uploadElementoImagen(imageFile);
        
        // Crear el recurso Cloudinary
        CloudinaryResource resource = cloudinaryResourceService.crearRecurso(
            (String) uploadResult.get("url"),
            (String) uploadResult.get("public_id"),
            (String) uploadResult.get("format"),
            (String) uploadResult.get("original_filename"),
            null
        );
        
        log.info("Imagen subida a Cloudinary con ID: {}", resource.getId());
        
        // Asociar recurso a elemento imagen
        elementoImagen.setCloudinaryResource(resource);
    } catch (Exception e) {
        log.error("Error al procesar imagen base64: {}", e.getMessage(), e);
        throw new ImageProcessingException("Error al procesar imagen", e);
    }
}

/**
 * Procesa una imagen a partir de una URL
 * @param elementoImagen El elemento de imagen a actualizar
 * @param imageUrl URL de la imagen
 * @throws ImageProcessingException Si ocurre un error al procesar la imagen
 */
private void procesarImagenUrl(ElementoImagen elementoImagen, String imageUrl) {
    try {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de imagen no puede ser nula o vacía");
        }
        
        String publicId = cloudinaryService.extractPublicIdFromUrl(imageUrl);
        if (publicId == null) {
            publicId = "external-image-" + System.currentTimeMillis();
        }
        
        // Crear recurso para URL existente
        CloudinaryResource cloudinaryResource = cloudinaryResourceService.obtenerRecursoPorPublicId(publicId);
        
        // Si no existe, crearlo
        if (cloudinaryResource == null) {
            cloudinaryResource = cloudinaryResourceService.crearRecurso(
                imageUrl,
                publicId,
                IMAGE_JPEG,  // Tipo predeterminado
                "elemento-" + System.currentTimeMillis() + ".jpg",
                null
            );
        }
        
        // Asociar CloudinaryResource con el ElementoImagen
        elementoImagen.setCloudinaryResource(cloudinaryResource);
    } catch (Exception e) {
            log.error("Error al procesar imagen URL: {}", e.getMessage(), e);
            throw new ImageProcessingException("Error al procesar imagen desde URL", e);
        }
    }
    
    /**
     * Procesa una imagen a partir de su ID
     * @param elementoImagen El elemento de imagen a actualizar
     * @param imageId ID de la imagen
     */
    private void procesarImagenPorId(ElementoImagen elementoImagen, Long imageId) {
        // Usar un recurso existente por ID
        CloudinaryResource cloudinaryResource = cloudinaryResourceService.obtenerRecursoPorId(imageId);
        if (cloudinaryResource == null) {
            throw new ResourceNotFoundException("Recurso de imagen no encontrado con ID: " + imageId);
        }
        
        elementoImagen.setCloudinaryResource(cloudinaryResource);
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long longValue) return longValue;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Convierte un objeto a Integer con un valor por defecto si hay error
     * @param value El objeto a convertir
     * @param defaultValue Valor por defecto si hay error
     * @return Valor Integer convertido o el valor por defecto
     */
    private Integer parseInt(Object value, Integer defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Integer intValue) return intValue;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.debug("Error al convertir '{}' a Integer: {}", value, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Convierte un objeto a Float de forma segura
     * @param value El valor a convertir
     * @param defaultValue Valor por defecto si no se puede convertir
     * @return El valor Float o el valor por defecto
     */
    private Float parseFloat(Object value, Float defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Float floatValue) return floatValue;
        if (value instanceof Number number) return number.floatValue();
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Obtiene un color por defecto para cuando no se especifica uno
     * @return El color por defecto
     */
    private Color obtenerColorPorDefecto() {
        Color color = colorRepository.findById(DEFAULT_COLOR_ID).orElse(null);
        if (color == null) {
            // Si el color por defecto no existe, intentar obtener el primero disponible
            log.warn("Color por defecto {} no encontrado. Buscando alternativa.", DEFAULT_COLOR_ID);
            color = colorRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No hay colores disponibles en la base de datos"));
        }
        return color;
    }
    
    /**
     * Obtiene una fuente por defecto para cuando no se especifica una
     * @return La fuente por defecto
     */
    private Fuente obtenerFuentePorDefecto() {
        Fuente fuente = fuenteRepository.findById(DEFAULT_FONT_ID).orElse(null);
        if (fuente == null) {
            // Si la fuente por defecto no existe, intentar obtener la primera disponible
            log.warn("Fuente por defecto con ID {} no encontrada. Buscando alternativa.", DEFAULT_FONT_ID);
            fuente = fuenteRepository.findAll()
                     .stream()
                     .findFirst()
                     .orElseThrow(() -> new IllegalStateException("No hay fuentes disponibles en la base de datos"));
        }
        return fuente;
    }
}
