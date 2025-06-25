package com.rabbithole.productos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO para transferir información de una miniatura (thumbnail) de un ítem entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailItemDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long itemCarritoId;
    private Long itemOrdenId;
    private Long tipoAnguloId;
    private String nombreAngulo;
    private String url;
    private CloudinaryResourceDTO cloudinaryResource;
}
