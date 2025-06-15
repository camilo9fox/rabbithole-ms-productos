package com.rabbithole.productos.service.impl;

import com.cloudinary.utils.ObjectUtils;
import com.rabbithole.productos.model.CloudinaryResource;
import com.rabbithole.productos.model.ElementoImagen;
import com.rabbithole.productos.service.CloudinaryResourceService;
import com.rabbithole.productos.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import com.rabbithole.productos.exception.ImageProcessingException;
import com.rabbithole.productos.util.Base64ImageUtil;

/**
 * Clase para procesar imágenes y subirlas a Cloudinary
 */
@Component
public class CloudinaryImageProcessor {

    private final CloudinaryService cloudinaryService;
    private final CloudinaryResourceService cloudinaryResourceService;

    @Autowired
    public CloudinaryImageProcessor(CloudinaryService cloudinaryService, CloudinaryResourceService cloudinaryResourceService) {
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryResourceService = cloudinaryResourceService;
    }

    /**
     * Procesa una imagen base64 y la sube a Cloudinary
     * @param base64Image Imagen en formato base64
     * @return Mapa con los datos de la imagen subida (url, publicId, etc), o map vacío si no hay imagen
     * @throws ImageProcessingException Si ocurre un error al subir la imagen
     */
    public Map<String, Object> procesarYSubirImagenBase64(String base64Image) throws ImageProcessingException {
        if (base64Image == null || base64Image.isEmpty()) {
            return ObjectUtils.emptyMap();
        }
        
        // Si no es una imagen base64 (ya es una URL de Cloudinary), retornamos
        if (!base64Image.startsWith("data:") && !base64Image.startsWith("/9j/")) {
            return ObjectUtils.emptyMap();
        }
        
        try {
            // Convertir base64 a MultipartFile
            MultipartFile multipartFile = Base64ImageUtil.createMultipartFileFromBase64(base64Image);
            
            // Subir a Cloudinary
            return cloudinaryService.uploadElementoImagen(multipartFile);
        } catch (IOException e) {
            throw new ImageProcessingException("Error al procesar y subir imagen base64", e);
        }
    }
    
    /**
     * Procesa una imagen para un elemento, subiéndola a Cloudinary y creando un recurso CloudinaryResource.
     *
     * @param file Archivo multipart de la imagen a procesar
     * @param elemento Elemento de imagen a actualizar con el recurso Cloudinary creado
     * @return El elemento de imagen actualizado
     * @throws ImageProcessingException Si hay problemas al leer el archivo
     */
    public ElementoImagen procesarImagen(MultipartFile file, ElementoImagen elemento) throws ImageProcessingException {
        try {
            Map<String, Object> uploadOptions = ObjectUtils.emptyMap();
            Map<String, Object> result = cloudinaryService.subirImagen(file, uploadOptions);
            
            // Crear un CloudinaryResource con los datos de la imagen
            CloudinaryResource cloudinaryResource = cloudinaryResourceService.crearRecurso(
                    (String) result.get("url"),
                    (String) result.get("public_id"),
                    file.getContentType(),
                    file.getOriginalFilename(),
                    file.getSize());
        
            // Asociar el CloudinaryResource con ElementoImagen
            elemento.setCloudinaryResource(cloudinaryResource);
            
            return elemento;
        } catch (IOException e) {
            throw new ImageProcessingException("Error al procesar y subir imagen", e);
        }
    }
    
    /**
     * Crea un ElementoImagen a partir de los datos de Cloudinary
     * @param cloudinaryResult Resultado de la subida a Cloudinary
     * @return ElementoImagen con los datos de la imagen
     */
    public ElementoImagen createElementoImagen(Map<String, Object> cloudinaryResult) {
        if (cloudinaryResult == null) {
            return null;
        }
        
        // Extraer información del resultado de Cloudinary
        String secureUrl = (String) cloudinaryResult.get("secure_url");
        String publicId = (String) cloudinaryResult.get("public_id");
        String resourceType = (String) cloudinaryResult.get("resource_type");
        String originalFilename = (String) cloudinaryResult.get("original_filename");
        Long fileSize = null;
        
        // Obtener el tamaño del archivo si está disponible
        if (cloudinaryResult.containsKey("bytes")) {
            Object bytes = cloudinaryResult.get("bytes");
            // En Java 8 no existe pattern matching con instanceof
            // Mantenemos la forma estándar compatible
            if (bytes instanceof Number) {
                Number number = (Number) bytes;
                fileSize = number.longValue();
            }
        }
        
        // Crear y configurar el objeto CloudinaryResource
        CloudinaryResource cloudinaryResource = new CloudinaryResource();
        cloudinaryResource.setUrlImagen(secureUrl);
        cloudinaryResource.setPublicId(publicId);
        cloudinaryResource.setTipoImagen(resourceType);
        cloudinaryResource.setNombreArchivo(originalFilename);
        cloudinaryResource.setTamanoArchivo(fileSize);
        
        // Crear el ElementoImagen y asociarlo con CloudinaryResource
        ElementoImagen elementoImagen = new ElementoImagen();
        elementoImagen.setCloudinaryResource(cloudinaryResource);
        
        return elementoImagen;
    }
    
    /**
     * Elimina una imagen de Cloudinary por su publicId
     * @param publicId ID público de la imagen en Cloudinary
     * @throws ImageProcessingException Si ocurre un error al eliminar la imagen
     */
    public void eliminarImagen(String publicId) throws ImageProcessingException {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }
        
        try {
            cloudinaryService.eliminarImagen(publicId);
        } catch (IOException e) {
            throw new ImageProcessingException("Error al eliminar imagen de Cloudinary", e);
        }
    }
}
