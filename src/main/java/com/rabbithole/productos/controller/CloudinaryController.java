package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.CloudinaryResponse;
import com.rabbithole.productos.service.CloudinaryService;
import com.rabbithole.productos.util.Base64MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Controlador para operaciones relacionadas con subida de imágenes a Cloudinary.
 */
@RestController
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;
    
    /**
     * Sube una imagen a Cloudinary directamente (sin asociar a ninguna entidad).
     * Útil para pruebas o para subir imágenes temporales.
     * 
     * @param file Archivo a subir
     * @return Datos de la imagen subida
     */
    @PostMapping("/upload")
    public ResponseEntity<Object> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Archivo vacío");
            }
            
            Map<String, Object> result = cloudinaryService.uploadElementoImagen(file);
            
            CloudinaryResponse response = new CloudinaryResponse(
                (String) result.get("public_id"),
                (String) result.get("url"),
                (String) result.get("secure_url"),
                (String) result.get("format"),
                (Integer) result.get("width"),
                (Integer) result.get("height"),
                ((Number) result.get("bytes")).longValue(),
                (String) result.get("resource_type"),
                (String) result.get("asset_id"),
                (String) result.get("type")
            );
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error al subir imagen", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir imagen: " + e.getMessage());
        }
        
    }
    
    /**
     * Elimina una imagen de Cloudinary por su public_id.
     * 
     * @param publicId ID público de la imagen en Cloudinary
     * @return Resultado de la eliminación
     */
    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<Object> deleteImage(@PathVariable String publicId) {
        try {
            Map<String, Object> result = cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error al eliminar imagen", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar imagen: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una imagen de Cloudinary usando un publicId enviado en el cuerpo de la solicitud.
     * Útil cuando el publicId contiene caracteres especiales como '/'.
     * 
     * @param request Mapa que contiene el public_id con la clave "publicId"
     * @return Resultado de la eliminación
     */
    @PostMapping("/delete")
    public ResponseEntity<Object> deleteImageByBody(@RequestBody Map<String, String> request) {
        try {
            String publicId = request.get("publicId");
            
            if (publicId == null || publicId.isEmpty()) {
                return ResponseEntity.badRequest().body("No se proporcionó un publicId");
            }
            
            Map<String, Object> result = cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error al eliminar imagen", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar imagen: " + e.getMessage());
        }
    }
    
    /**
     * Sube una imagen en formato base64 a Cloudinary.
     * 
     * @param request Mapa que contiene la imagen en formato base64 con la clave "image"
     * @return Datos de la imagen subida
     */
    @PostMapping("/upload-base64")
    public ResponseEntity<Object> uploadBase64Image(@RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body("No se proporcionó una imagen en base64");
            }
            
            // Eliminar el prefijo (ej. "data:image/jpeg;base64,") si existe
            String imageType = "image/jpeg"; // Valor por defecto
            if (base64Image.contains(";")) {
                String[] parts = base64Image.split(";");
                if (parts[0].contains(":")) {
                    imageType = parts[0].split(":")[1];
                }
            }
            
            if (base64Image.contains(",")) {
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }
            
            // Decodificar base64 a bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            
            // Crear un MultipartFile a partir de los bytes
            String filename = "image." + imageType.substring(imageType.indexOf("/") + 1);
            MultipartFile multipartFile = new Base64MultipartFile(
                imageBytes,
                "file", 
                filename, 
                imageType
            );
            
            // Usar el servicio existente
            Map<String, Object> result = cloudinaryService.uploadElementoImagen(multipartFile);
            
            CloudinaryResponse response = new CloudinaryResponse(
                (String) result.get("public_id"),
                (String) result.get("url"),
                (String) result.get("secure_url"),
                (String) result.get("format"),
                (Integer) result.get("width"),
                (Integer) result.get("height"),
                ((Number) result.get("bytes")).longValue(),
                (String) result.get("resource_type"),
                (String) result.get("asset_id"),
                (String) result.get("type")
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al subir imagen base64", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir imagen base64: " + e.getMessage());
        }
    }
}
