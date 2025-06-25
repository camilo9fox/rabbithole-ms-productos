package com.rabbithole.productos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO para transferir información de una Talla entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TallaDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String nombre;
    private String descripcion;
}
