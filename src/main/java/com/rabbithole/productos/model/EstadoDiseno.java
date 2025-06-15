package com.rabbithole.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa los diferentes estados posibles de un diseño personalizado.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "ESTADOS_DISENO")
public class EstadoDiseno implements Serializable {

    @Id
    @Column(name = "ID")
    private Long id;
    
    @NotBlank
    @Column(name = "CODIGO", length = 30, nullable = false, unique = true)
    private String codigo;
    
    @NotBlank
    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;
    
    @Lob
    @Column(name = "DESCRIPCION")
    private String descripcion;
    
    private static final long serialVersionUID = 1L;
}
