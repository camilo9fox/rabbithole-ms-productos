package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la información de pago en una orden
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoPagoDTO {
    
    /**
     * ID del método de pago utilizado
     */
    private Long metodoPagoId;
    
    /**
     * Últimos 4 dígitos de la tarjeta de crédito/débito
     */
    private String ultimosDigitos;
    
    /**
     * Nombre del titular de la tarjeta
     */
    private String titularTarjeta;
    
    /**
     * ID de transacción del procesador de pagos
     */
    private String idTransaccion;
}
