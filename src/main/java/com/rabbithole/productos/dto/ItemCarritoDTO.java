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
 * DTO para transferir información de un ítem de carrito entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long carritoId;
    
    // Información del producto
    private Long productoId;
    private String productoNombre;
    @jakarta.persistence.Transient
    private ProductoDTO producto;
    
    // Información del diseño personalizado
    private Long disenoPersonalizadoId;
    @jakarta.persistence.Transient
    private DisenoPersonalizadoDTO disenoPersonalizado;
    
    // Información del color
    private String colorId;
    private ColorDTO color;
    
    // Información de la talla
    private String tallaId;
    private TallaDTO talla;
    
    // Información del tipo de ítem
    private Long tipoId;
    private String tipoNombre;
    private TipoItemDTO tipo;
    
    // Cantidades y precios
    private Integer cantidad;
    private BigDecimal precioUnitario;
    
    // Fechas
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaActualizacion;
    
    // Miniaturas del ítem
    private List<ThumbnailItemDTO> thumbnails = new ArrayList<>();
    
    /**
     * Calcula el subtotal del ítem (precio unitario * cantidad).
     * 
     * @return El subtotal calculado o 0 si algún dato es nulo
     */
    public BigDecimal getSubtotal() {
        if (precioUnitario != null && cantidad != null) {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        return BigDecimal.ZERO;
    }
}
