package com.rabbithole.productos.dto.request;

import lombok.Data;

@Data
public class ThumbnailUploadRequestDTO {
    private Long itemCarritoId;
    private Long itemOrdenId;
    private Long tipoAnguloId;
    private String url;
    private String publicId;
}
