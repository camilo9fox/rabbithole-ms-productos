package com.rabbithole.productos.model;

import java.io.Serializable;

/**
 * Interfaz base para elementos de diseño.
 * Reemplaza la antigua clase ElementoDiseno.
 */
public interface IElementoDiseno extends Serializable {
    
    /**
     * Obtiene el ID único del elemento.
     * @return ID del elemento
     */
    Long getId();
    
    /**
     * Establece el ID único del elemento.
     * @param id ID del elemento
     */
    void setId(Long id);
    
    /**
     * Obtiene el ángulo asociado al elemento.
     * @return ángulo asociado
     */
    AnguloDiseno getAngulo();
    
    /**
     * Establece el ángulo asociado al elemento.
     * @param angulo ángulo a asociar
     */
    void setAngulo(AnguloDiseno angulo);
    
    /**
     * Obtiene la posición del elemento.
     * @return posición del elemento
     */
    Posicion getPosicion();
    
    /**
     * Establece la posición del elemento.
     * @param posicion posición a establecer
     */
    void setPosicion(Posicion posicion);
    
    /**
     * Indica si el elemento está activo.
     * @return true si está activo, false en caso contrario
     */
    Boolean getActivo();
    
    /**
     * Establece si el elemento está activo.
     * @param activo estado de activación
     */
    void setActivo(Boolean activo);
    
    /**
     * Obtiene la anchura del elemento.
     * @return anchura del elemento
     */
    Integer getAnchura();
    
    /**
     * Obtiene la altura del elemento.
     * @return altura del elemento
     */
    Integer getAltura();
}
