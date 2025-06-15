package com.rabbithole.productos.dto;

import java.util.Map;
import lombok.Data;

/**
 * DTO para representar un elemento (texto o imagen) en el diseño personalizado
 */
@Data
public class ElementoDTO {
    private String tipo; // "TEXTO" o "IMAGEN"
    private ElementoDisenoDTO propiedadesDiseno;
    private Map<String, Object> propiedadesElemento;
}
