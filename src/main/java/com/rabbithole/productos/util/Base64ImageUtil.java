package com.rabbithole.productos.util;

import com.rabbithole.productos.dto.CloudinaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Clase para manejar conversiones de imágenes en formato base64
 */
@Slf4j
public class Base64ImageUtil {
    
    /**
     * Convierte una imagen en formato base64 a un MultipartFile
     */
    public static class Base64MultipartFile implements MultipartFile {
        private final byte[] fileContent;
        private final String fileName;
        private final String contentType;
        private final String originalFilename;

        public Base64MultipartFile(byte[] fileContent, String name, String originalFilename, String contentType) {
            this.fileContent = fileContent;
            this.fileName = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return fileContent == null || fileContent.length == 0;
        }

        @Override
        public long getSize() {
            return fileContent.length;
        }

        @Override
        public byte[] getBytes() {
            return fileContent;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(fileContent);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
    
    /**
     * Procesa una imagen base64 y extrae su tipo y datos
     * @param base64Image La imagen en formato base64 (con o sin prefijo data:)
     * @return Un array donde [0] es el tipo de imagen y [1] son los datos base64 limpios
     */
    public static String[] processBase64Image(String base64Image) {
        String imageType = "image/jpeg"; // Tipo por defecto
        String cleanBase64 = base64Image;
        
        // Extraer tipo de imagen si hay prefijo
        if (base64Image.contains(";")) {
            String[] parts = base64Image.split(";");
            if (parts[0].contains(":")) {
                imageType = parts[0].split(":")[1];
            }
        }
        
        // Eliminar prefijo si existe
        if (base64Image.contains(",")) {
            cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
        }
        
        return new String[]{imageType, cleanBase64};
    }
    
    /**
     * Crea un MultipartFile a partir de una imagen base64
     */
    public static MultipartFile createMultipartFileFromBase64(String base64Image) {
        String[] processedImage = processBase64Image(base64Image);
        String imageType = processedImage[0];
        String cleanBase64 = processedImage[1];
        
        // Decodificar base64 a bytes
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
        
        // Crear nombre de archivo basado en el tipo
        String extension = imageType.substring(imageType.indexOf("/") + 1);
        String filename = "image." + extension;
        
        return new Base64MultipartFile(imageBytes, "file", filename, imageType);
    }


        public static boolean isValidBase64(String str) {
            if (str == null || str.length() % 4 != 0) { // Longitud debe ser múltiplo de 4
                return false;
            }
            
            try {
                Base64.getDecoder().decode(str);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
}
