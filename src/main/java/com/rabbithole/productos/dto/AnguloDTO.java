package com.rabbithole.productos.dto;

import lombok.Data;

/**
 * DTO para representar un ángulo de un diseño personalizado
 */
@Data
public class AnguloDTO {
    private Long id;
    private Long tipoAnguloId;
    private String thumbnailUrl;
    private String thumbnailBase64;
    private String nombreAngulo;
    private ElementoDTO elemento;
}
