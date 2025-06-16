package com.rabbithole.productos.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para operaciones con Cloudinary (subida y eliminación de imágenes)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    
    private static final String ELEMENTOS_FOLDER = "elementos_imagen";
    private static final String THUMBNAILS_FOLDER = "thumbnails";
    private static final String FOLDER_PARAM = "folder";
    private static final String RESOURCE_TYPE_PARAM = "resource_type";
    private static final String AUTO_PARAM = "auto";
    private static final String PUBLIC_ID_PARAM = "public_id";
    private static final String OVERWRITE_PARAM = "overwrite";

    /**
     * Sube un archivo de imagen a Cloudinary en la carpeta de elementos.
     *
     * @param file Archivo a subir
     * @return Map con los datos de la imagen subida (incluyendo URL)
     * @throws IOException Si hay problemas al procesar el archivo
     */
    public Map<String, Object> uploadElementoImagen(MultipartFile file) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> options = ObjectUtils.asMap(
            FOLDER_PARAM, ELEMENTOS_FOLDER,
            RESOURCE_TYPE_PARAM, AUTO_PARAM
        );
        return uploadFile(file, options);
    }
    
    /**
     * Sube una imagen de thumbnail para un ángulo de diseño.
     *
     * @param file Archivo a subir
     * @return Map con los datos de la imagen subida (incluyendo URL)
     * @throws IOException Si hay problemas al procesar el archivo
     */
    public Map<String, Object> uploadAnguloThumbnail(MultipartFile file) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> options = ObjectUtils.asMap(
            FOLDER_PARAM, THUMBNAILS_FOLDER,
            RESOURCE_TYPE_PARAM, AUTO_PARAM
        );
        return uploadFile(file, options);
    }

    /**
     * Sube un archivo a Cloudinary en una carpeta específica.
     *
     * @param file Archivo a subir
     * @param options Opciones de subida
     * @return Map con los datos de la imagen subida
     * @throws IOException Si hay problemas al procesar el archivo
     */
    private Map<String, Object> uploadFile(MultipartFile file, Map<String, Object> options) throws IOException {
        try {
            // Generar un ID único para el archivo
            String publicId = UUID.randomUUID().toString();
            
            // Configurar opciones de subida
            Map<String, Object> params = new HashMap<>();
            params.put(PUBLIC_ID_PARAM, publicId);
            params.put(OVERWRITE_PARAM, true);
            params.put(RESOURCE_TYPE_PARAM, AUTO_PARAM);
            // Agregar todas las opciones que recibimos
            params.putAll(options);
            
            // Subir archivo a Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            
            log.info("Archivo subido a Cloudinary con éxito. Public ID: {}", publicId);
            
            return result;
        } catch (IOException e) {
            log.error("Error al subir archivo a Cloudinary", e);
            throw e;
        }
    }

    /**
     * Alias para uploadFile - Compatibilidad con código existente
     *
     * @param file Archivo a subir
     * @param options Opciones de subida
     * @return Map con los datos de la imagen subida
     * @throws IOException Si hay problemas al procesar el archivo
     */
    public Map<String, Object> subirImagen(MultipartFile file, Map<String, Object> options) throws IOException {
        return uploadFile(file, options != null ? options : ObjectUtils.emptyMap());
    }
    
    /**
     * Genera una versión thumbnail de una imagen a partir de su URL
     *
     * @param imageUrl URL de la imagen original
     * @return URL del thumbnail generado
     */
    public String generarThumbnail(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Extraer el publicId de la URL
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId == null) {
            return imageUrl; // Si no podemos procesar la URL, devolvemos la misma
        }
        
        // Crear una URL de transformación para generar un thumbnail
        // con un tamaño máximo de 150x150, manteniendo la proporción
        String thumbnailUrl = imageUrl.replace("/upload/", "/upload/c_thumb,h_150,w_150/");
        
        log.info("Thumbnail generado para URL: {}", imageUrl);
        return thumbnailUrl;
    }
    
    /**
     * Elimina una imagen de Cloudinary utilizando su public_id.
     *
     * @param publicId ID público de la imagen en Cloudinary
     * @return Resultado de la operación
     * @throws IOException Si hay problemas en la comunicación
     */
    public Map<String, Object> eliminarImagen(String publicId) throws IOException {
        return deleteImage(publicId);
    }
    
    /**
     * Elimina una imagen de Cloudinary utilizando su public_id.
     * @deprecated Usar eliminarImagen en su lugar
     *
     * @param publicId ID público de la imagen en Cloudinary
     * @return Resultado de la operación
     * @throws IOException Si hay problemas en la comunicación
     */
    @Deprecated
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Imagen eliminada de Cloudinary. Public ID: {}", publicId);
            return result;
        } catch (IOException e) {
            log.error("Error al eliminar imagen de Cloudinary. Public ID: {}", publicId, e);
            throw e;
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     *
     * @param cloudinaryUrl URL completa de la imagen en Cloudinary
     * @return public_id de la imagen
     */
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }
        
        // Las URLs de Cloudinary tienen el formato: https://res.cloudinary.com/cloud-name/image/upload/v1234567890/public-id
        String[] parts = cloudinaryUrl.split("/upload/");
        if (parts.length < 2) {
            return null;
        }
        
        // Eliminar posibles parámetros de transformación
        String publicIdWithParams = parts[1];
        if (publicIdWithParams.contains("?")) {
            return publicIdWithParams.substring(0, publicIdWithParams.indexOf("?"));
        }
        
        return publicIdWithParams;
    }
}
