package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una respuesta de subida de imagen a Cloudinary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryResponse {
    private String publicId;
    private String url;
    private String secureUrl;
    private String format;
    private Integer width;
    private Integer height;
    private Long bytes;
    private String resourceType;
    private String assetId;
    private String type;
}
