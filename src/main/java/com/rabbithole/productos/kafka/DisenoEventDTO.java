package com.rabbithole.productos.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisenoEventDTO {
    private String type; // STATUS_CHANGED | UPDATED
    private String userEmail;
    private String userName;
    private String designName;
    private String newStatus;
    private String previewUrl;

    // Lombok genera getters, setters y constructores.
}
