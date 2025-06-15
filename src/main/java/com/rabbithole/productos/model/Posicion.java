package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que representa una posición en un diseño personalizado.
 * Contiene las coordenadas x e y del elemento.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "POSICIONES")
public class Posicion implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "POSICION_X", nullable = false)
    private Integer posicionX;
    
    @Column(name = "POSICION_Y", nullable = false)
    private Integer posicionY;
    
    @Column(name = "CREADO_EN", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
    
    private static final long serialVersionUID = 1L;
    
    @PrePersist
    public void prePersist() {
        creadoEn = LocalDateTime.now();
        
        // Establecer valores predeterminados si son null
        if (posicionX == null) {
            posicionX = 250;
        }
        
        if (posicionY == null) {
            posicionY = 250;
        }
    }
}
