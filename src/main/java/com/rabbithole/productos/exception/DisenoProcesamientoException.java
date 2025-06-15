package com.rabbithole.productos.exception;

/**
 * Excepción personalizada para errores durante el procesamiento de diseños personalizados
 */
public class DisenoProcesamientoException extends RuntimeException {
    
    public DisenoProcesamientoException(String message) {
        super(message);
    }
    
    public DisenoProcesamientoException(String message, Throwable cause) {
        super(message, cause);
    }
}
