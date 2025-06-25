package com.rabbithole.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa los tipos de ítem en la base de datos
 */
@Entity
@Table(name = "tipos_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoItem implements Serializable {
    
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Transient
    private String descripcion;
    
    private static final long serialVersionUID = 1L;
}
