package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Clase abstracta base para elementos de diseño en un diseño personalizado.
 * Define los atributos comunes a todos los elementos.
 * Ya no mapea a ninguna tabla específica (no usa @Entity)
 * Se ha eliminado la referencia a AnguloDiseno ya que este modelo fue eliminado.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class ElementoDiseno implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "POSICION_ID")
    private Posicion posicion;
    
    // Note: La referencia a AnguloDiseno fue eliminada ya que ese modelo fue removido del proyecto
    
    // Marcador para herramientas de serialización
    // Todos los campos incluidos en esta clase son serializables
    
    /**
     * Método transitorio para obtener la posición X del elemento.
     * @return posición X
     */
    @Transient
    public Integer getPosicionX() {
        return posicion != null ? posicion.getPosicionX() : null;
    }
    
    /**
     * Método transitorio para obtener la posición Y del elemento.
     * @return posición Y
     */
    @Transient
    public Integer getPosicionY() {
        return posicion != null ? posicion.getPosicionY() : null;
    }
    
    /**
     * Método abstracto para obtener la anchura del elemento.
     * @return anchura
     */
    public abstract Integer getAnchura();
    
    /**
     * Método abstracto para obtener la altura del elemento.
     * @return altura
     */
    public abstract Integer getAltura();
}
