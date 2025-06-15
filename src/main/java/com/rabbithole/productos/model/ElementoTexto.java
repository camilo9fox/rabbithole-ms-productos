package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que representa un elemento de texto en un diseño personalizado.
 */
@Entity
@Table(name = "elementos_texto")
@Getter
@Setter
@NoArgsConstructor
public class ElementoTexto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "POSICION_ID")
    private Posicion posicion;

    @NotBlank
    @Lob
    @Column(name = "contenido")
    private String contenido;
    
    @Transient // Esta columna no existe en la BD
    private Integer anchura;
    
    @Transient // Esta columna no existe en la BD
    private Integer altura;
    
    @Transient // Esta columna no existe en la BD
    private Float rotacion = 0.0f;

    @Transient // Esta columna no existe en la BD
    private Integer profundidad = 0;

    @ManyToOne
    @JoinColumn(name = "fuente_id", nullable = false)
    private Fuente fuente; // Fuente ya implementa Serializable

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = false)
    private Color colorTexto; // Color ya implementa Serializable

    @NotNull
    @Column(name = "tamanio", nullable = false)
    private Integer tamanoFuente = 30;
    
    @Transient // Esta columna no existe en la BD
    private String peso = "normal";
    
    @Transient // Esta columna no existe en la BD
    private String estilo = "normal";
    
    @Transient // Esta columna no existe en la BD
    private Float espaciadoLinea = 1.2f;
    
    @Transient // Esta columna no existe en la BD
    private String alineacion = "left";
    
    @Transient // Esta columna no existe en la BD
    private LocalDateTime creadoEn = LocalDateTime.now();

    /**
     * Método para establecer la posición X mediante la creación o actualización
     * de un objeto Posicion asociado
     * @param posicionX valor de coordenada X
     */
    public void setPosicionX(Integer posicionX) {
        if (getPosicion() == null) {
            setPosicion(new Posicion());
            getPosicion().setCreadoEn(LocalDateTime.now());
        }
        getPosicion().setPosicionX(posicionX);
    }
    
    /**
     * Método para establecer la posición Y mediante la creación o actualización
     * de un objeto Posicion asociado
     * @param posicionY valor de coordenada Y
     */
    public void setPosicionY(Integer posicionY) {
        if (getPosicion() == null) {
            setPosicion(new Posicion());
            getPosicion().setCreadoEn(LocalDateTime.now());
        }
        getPosicion().setPosicionY(posicionY);
    }
    
    public Integer getAnchura() {
        return this.anchura;
    }
    
    public Integer getAltura() {
        return this.altura;
    }
    
    /**
     * Método para obtener la posición X del elemento.
     * @return posición X
     */
    @Transient
    public Integer getPosicionX() {
        return posicion != null ? posicion.getPosicionX() : null;
    }
    
    /**
     * Método para obtener la posición Y del elemento.
     * @return posición Y
     */
    @Transient
    public Integer getPosicionY() {
        return posicion != null ? posicion.getPosicionY() : null;
    }
    
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
    
    public LocalDateTime getCreadoEn() {
        return this.creadoEn;
    }

    /**
     * Devuelve el tamaño de la fuente,
     * mantenido por compatibilidad con versiones anteriores.
     * 
     * @return tamaño de la fuente
     */
    public Integer getTamanioFuente() {
        return tamanoFuente;
    }

    /**
     * Obtiene el valor de rotación (mantenido por compatibilidad).
     * 
     * @return ángulo de rotación
     */
    public Float getRotacion() {
        return rotacion;
    }

    /**
     * Obtiene el valor de profundidad (mantenido por compatibilidad).
     * 
     * @return profundidad del elemento
     */
    public Integer getProfundidad() {
        return null;
    }

    /**
     * Método temporal para compatibilidad con mappers.
     * @param profundidad valor a establecer (ignorado)
     */
    public void setProfundidad(Integer profundidad) {
        // No implementado
    }
}
