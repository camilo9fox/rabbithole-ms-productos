package com.rabbithole.productos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para transferir información de Carrito entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private List<ItemCarritoDTO> items = new ArrayList<>();
    
    /**
     * Calcula la cantidad total de ítems en el carrito.
     * 
     * @return Cantidad total
     */
    public Integer getCantidadTotal() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        return items.stream()
                .mapToInt(ItemCarritoDTO::getCantidad)
                .sum();
    }
    
    /**
     * Calcula el subtotal del carrito sumando los subtotales de sus ítems.
     * 
     * @return Subtotal del carrito
     */
    public BigDecimal getSubtotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
