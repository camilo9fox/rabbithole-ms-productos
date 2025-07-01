package com.rabbithole.productos.kafka;

import java.math.BigDecimal;

public class OrderItemDTO {
    private String nombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;

    public OrderItemDTO() {}

    public OrderItemDTO(String nombre, Integer cantidad, BigDecimal precioUnitario) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
