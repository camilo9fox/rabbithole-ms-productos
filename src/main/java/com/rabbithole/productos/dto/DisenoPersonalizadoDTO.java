package com.rabbithole.productos.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar un Diseño Personalizado completo con todos sus componentes relacionados
 */
@Data
public class DisenoPersonalizadoDTO {
    private Long id;
    private Long usuarioId;
    private String detalle;
    private String colorId; // Cambio a String para coincidir con el tipo de ID en Color
    private String tallaId; // Cambio a String para coincidir con el tipo de ID en Talla
    private BigDecimal precio;
    private Long estadoId;
    private String motivoRechazo;
    private String notasModificacion;
    private Boolean creadoPorAdmin;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private List<AnguloDTO> angulos;
}
