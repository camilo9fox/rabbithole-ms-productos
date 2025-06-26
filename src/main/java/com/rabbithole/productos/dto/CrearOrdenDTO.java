package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una nueva orden para un usuario registrado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearOrdenDTO {
    /**
     * ID del usuario que crea la orden
     */
    private Long usuarioId;
    
    /**
     * ID del carrito desde el que se creará la orden
     */
    private Long carritoId;
    
    /**
     * Información de envío para la orden
     */
    private InfoEnvioDTO infoEnvio;
    
    /**
     * Información de pago para la orden
     */
    private InfoPagoDTO infoPago;
    

}
