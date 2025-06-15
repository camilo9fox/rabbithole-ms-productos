package com.rabbithole.productos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa una fuente tipográfica disponible para los elementos de texto.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "FUENTES")
public class Fuente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @NotBlank
    @Column(name = "NOMBRE", length = 50, nullable = false, unique = true)
    private String nombre;
    
    @Column(name = "URL")
    private String url;
    
    private static final long serialVersionUID = 1L;
}
