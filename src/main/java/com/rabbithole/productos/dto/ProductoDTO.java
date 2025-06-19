package com.rabbithole.productos.dto;

import com.rabbithole.productos.model.Producto;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar la información de un producto
 * con referencias simplificadas a relaciones
 */
@Data
public class ProductoDTO {
    private Long id;
    private DisenoPersonalizadoDTO disenoPersonalizado;
    private String nombre;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private Integer activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    
    /**
     * Convierte una entidad Producto a ProductoDTO
     * @param producto la entidad a convertir
     * @return un nuevo ProductoDTO con datos de la entidad
     */
    public static ProductoDTO fromEntity(Producto producto) {
        if (producto == null) {
            return null;
        }
        
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setActivo(producto.getActivo());
        dto.setCreadoEn(producto.getCreadoEn());
        dto.setActualizadoEn(producto.getActualizadoEn());
        
        // Manejar la relación con categoría
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }
        
        // No necesitamos hacer la conversión del DisenoPersonalizado a DTO aquí
        // ya que existe una clase de conversión específica para ello
        
        return dto;
    }
}
