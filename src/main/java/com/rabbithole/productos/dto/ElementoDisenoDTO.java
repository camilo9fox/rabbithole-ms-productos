package com.rabbithole.productos.dto;

import lombok.Data;

/**
 * DTO para las propiedades comunes de elementos de diseño (posición, tamaño, etc.)
 */
@Data
public class ElementoDisenoDTO {
    private Float posicionX;
    private Float posicionY;
    private Float anchura;
    private Float altura;
    private Float rotacion;
    private Integer profundidad;
}
