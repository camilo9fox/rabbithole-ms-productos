package com.rabbithole.productos.dto;

import java.math.BigDecimal;

/**
 * DTO para representar un ítem de orden
 */
public class ItemOrdenDTO {
    private Long id;
    private String nombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String colorNombre;
    private String tallaNombre;
    private String tipoItem;
    private Long productoId;
    private ProductoDTO producto;
    private Long disenoPersonalizadoId;
    private DisenoPersonalizadoDTO disenoPersonalizado;
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public String getColorNombre() {
        return colorNombre;
    }
    
    public void setColorNombre(String colorNombre) {
        this.colorNombre = colorNombre;
    }
    
    public String getTallaNombre() {
        return tallaNombre;
    }
    
    public void setTallaNombre(String tallaNombre) {
        this.tallaNombre = tallaNombre;
    }
    
    public String getTipoItem() {
        return tipoItem;
    }
    
    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }
    
    public Long getProductoId() {
        return productoId;
    }
    
    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }
    
    public Long getDisenoPersonalizadoId() {
        return disenoPersonalizadoId;
    }
    
    public void setDisenoPersonalizadoId(Long disenoPersonalizadoId) {
        this.disenoPersonalizadoId = disenoPersonalizadoId;
    }
    
    public ProductoDTO getProducto() {
        return producto;
    }
    
    public void setProducto(ProductoDTO producto) {
        this.producto = producto;
    }
    
    public DisenoPersonalizadoDTO getDisenoPersonalizado() {
        return disenoPersonalizado;
    }
    
    public void setDisenoPersonalizado(DisenoPersonalizadoDTO disenoPersonalizado) {
        this.disenoPersonalizado = disenoPersonalizado;
    }
}
