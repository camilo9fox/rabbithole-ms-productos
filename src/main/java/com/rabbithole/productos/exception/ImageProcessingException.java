package com.rabbithole.productos.exception;

/**
 * Excepción específica para errores de procesamiento de imágenes
 */
public class ImageProcessingException extends RuntimeException {
    
    public ImageProcessingException(String message) {
        super(message);
    }
    
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
