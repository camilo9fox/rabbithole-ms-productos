package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para elementos de texto en un diseño personalizado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementoTextoDTO {
    
    private Long id;
    private Float posicionX;
    private Float posicionY;
    private Float ancho;
    private Float alto;
    private Float rotacion;
    private String texto;
    private String fontFamily;
    private Integer fontSize;
    private String color;
    private String fontWeight;
    private String fontStyle;
    private Float lineHeight;
    private String align;
    
}
