package com.rabbithole.productos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO para transferir información de un Tipo de Ítem entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoItemDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nombre;
    private String descripcion;
}
