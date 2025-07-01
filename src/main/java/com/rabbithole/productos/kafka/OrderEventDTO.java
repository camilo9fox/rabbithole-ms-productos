package com.rabbithole.productos.kafka;

import java.math.BigDecimal;
import java.util.List;

import com.rabbithole.productos.dto.ItemOrdenDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDTO {
    /** CREATED, UPDATED, CANCELED, etc. */
    private String type;
    private String userEmail;
    private String userName;
    private BigDecimal total;
    private String currency;
    private List<ItemOrdenDTO> items;
    /** Nuevo estado, sólo para eventos de cambio de estado */
    private String newStatus;

    // Lombok genera getters, setters y constructores.
}
