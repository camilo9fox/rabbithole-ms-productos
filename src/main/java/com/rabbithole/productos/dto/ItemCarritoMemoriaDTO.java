package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar un ítem en el carrito que está en memoria del cliente.
 * Se usa para crear órdenes anónimas sin necesidad de tener el carrito en la base de datos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCarritoMemoriaDTO {
    
    /**
     * ID del producto, puede ser null si es un diseño personalizado
     */
    private Long productoId;
    
    /**
     * ID del diseño personalizado, puede ser null si es un producto regular
     */
    private Long disenoPersonalizadoId;
    
    /**
     * ID del tipo de ítem
     */
    private Long tipoItemId;
    
    /**
     * ID del color, opcional (String ya que Color usa String como ID)
     */
    private String colorId;
    
    /**
     * ID de la talla, opcional (String ya que Talla usa String como ID)
     */
    private String tallaId;
    
    /**
     * Cantidad del ítem
     */
    private Integer cantidad;
    
    /**
     * Precio unitario del ítem
     */
    private BigDecimal precioUnitario;
    
    /**
     * Nombre del producto o diseño
     */
    private String nombre;
}
