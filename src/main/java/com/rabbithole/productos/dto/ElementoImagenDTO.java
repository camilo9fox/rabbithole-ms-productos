package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para elementos de imagen en un diseño personalizado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementoImagenDTO {
    
    private Long id;
    private Float posicionX;
    private Float posicionY;
    private Float ancho;
    private Float alto;
    private Float rotacion;
    private String urlImagen;
    private String publicId;
    private String tipoImagen;
    private String nombreArchivo;
    private Long tamanoArchivo;
    
}
