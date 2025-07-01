package com.rabbithole.productos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar una orden
 */
public class OrdenDTO {
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private LocalDateTime creadaEn;
    private BigDecimal total;
    private String estado;
    private String direccionEntrega;
    private String metodoPago;
    private InfoEnvioDTO infoEnvio;
    private InfoPagoDTO infoPago;
    private List<ItemOrdenDTO> items;
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
    
    public void setCreadaEn(LocalDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getDireccionEntrega() {
        return direccionEntrega;
    }
    
    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public InfoEnvioDTO getInfoEnvio() {
        return infoEnvio;
    }
    
    public void setInfoEnvio(InfoEnvioDTO infoEnvio) {
        this.infoEnvio = infoEnvio;
    }
    
    public InfoPagoDTO getInfoPago() {
        return infoPago;
    }
    
    public void setInfoPago(InfoPagoDTO infoPago) {
        this.infoPago = infoPago;
    }
    
    public List<ItemOrdenDTO> getItems() {
        return items;
    }
    
    public void setItems(List<ItemOrdenDTO> items) {
        this.items = items;
    }
}
