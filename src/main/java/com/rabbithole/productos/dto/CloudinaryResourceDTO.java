package com.rabbithole.productos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO para transferir información de un recurso almacenado en Cloudinary entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryResourceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String publicId;
    private String urlImagen;
    private Integer anchura;
    private Integer altura;
    private String formato;
    private Long tamañoBytes;
}
