package com.rabbithole.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa los tipos de ángulos disponibles para visualizar diseños.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "TIPOS_ANGULO")
public class TipoAngulo implements Serializable {

    @Id
    @Column(name = "ID")
    private Long id;
    
    @NotBlank
    @Column(name = "CODIGO", length = 20, nullable = false, unique = true)
    private String codigo;
    
    @NotBlank
    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;
    
    private static final long serialVersionUID = 1L;
}
