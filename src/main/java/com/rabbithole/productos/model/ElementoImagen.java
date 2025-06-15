package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que representa un elemento de imagen en un diseño personalizado.
 */
@Entity
@Table(name = "elementos_imagen")
@Getter
@Setter
@NoArgsConstructor
public class ElementoImagen implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "POSICION_ID")
    private Posicion posicion;

    @ManyToOne
    @JoinColumn(name = "cloudinary_resource_id")
    private CloudinaryResource cloudinaryResource; // CloudinaryResource ya implementa Serializable
    
    /**
     * Para compatibilidad con el nombre anterior del campo cloudinaryResource
     */
    public CloudinaryResource getResource() {
        return this.cloudinaryResource;
    }
    
    /**
     * Para compatibilidad con el nombre anterior del campo cloudinaryResource
     */
    public void setResource(CloudinaryResource resource) {
        this.cloudinaryResource = resource;
    }

    @Column(name = "alto")
    private Integer altura;

    @Column(name = "ancho")
    private Integer anchura;

    @Transient // Esta columna no existe en la BD
    private Float rotacion = 0.0f;

    @Transient // Esta columna no existe en la BD
    private Integer profundidad = 0;
    
    @Transient // Esta columna no existe en la BD, la marcamos como transient
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
    
    /**
     * Establece la fecha de creación
     * @param creadoEn fecha de creación
     */
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
    
    /**
     * Obtiene la fecha de creación
     * @return fecha de creación
     */
    public LocalDateTime getCreadoEn() {
        return this.creadoEn;
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
    
    /**
     * Para compatibilidad con el nombre usado en AnguloProcessor
     * @param ancho ancho de la imagen
     */
    public void setAncho(int ancho) {
        this.anchura = ancho;
    }
    
    /**
     * Para compatibilidad con el nombre usado en AnguloProcessor
     * @param alto alto de la imagen
     */
    public void setAlto(int alto) {
        this.altura = alto;
    }
    
    /**
     * Establece el ancho.
     * @param anchura el ancho a establecer
     */
    public void setAnchura(Integer anchura) {
        this.anchura = anchura;
    }
    
    /**
     * Establece el alto.
     * @param altura el alto a establecer
     */
    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    /**
     * Método temporal para compatibilidad con mappers.
     * @return valor de rotación (null en este caso)
     */
    public Float getRotacion() {
        return null;
    }

    /**
     * Método temporal para compatibilidad con mappers.
     * @param rotacion valor a establecer (ignorado)
     */
    public void setRotacion(Float rotacion) {
        // No implementado
    }

    /**
     * Método temporal para compatibilidad con mappers.
     * @return valor de profundidad (null en este caso)
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
    
    /**
     * Método auxiliar para obtener la URL de la imagen del recurso cloudinary
     * @return URL de la imagen
     */
    @Transient
    public String getUrlImagen() {
        return cloudinaryResource != null ? cloudinaryResource.getUrlImagen() : null;
    }
    
    /**
     * Método auxiliar para obtener el publicId de la imagen del recurso cloudinary
     * @return publicId de la imagen
     */
    @Transient
    public String getPublicId() {
        return cloudinaryResource != null ? cloudinaryResource.getPublicId() : null;
    }
    
    /**
     * Método auxiliar para obtener el tipo de la imagen del recurso cloudinary
     * @return tipo de la imagen
     */
    @Transient
    public String getTipoImagen() {
        return cloudinaryResource != null ? cloudinaryResource.getTipoImagen() : null;
    }
    
    /**
     * Método auxiliar para obtener el nombre del archivo de la imagen del recurso cloudinary
     * @return nombre del archivo de la imagen
     */
    @Transient
    public String getNombreArchivo() {
        return cloudinaryResource != null ? cloudinaryResource.getNombreArchivo() : null;
    }
    
    /**
     * Método auxiliar para obtener el tamaño del archivo de la imagen del recurso cloudinary
     * @return tamaño del archivo de la imagen
     */
    @Transient
    public Long getTamanoArchivo() {
        return cloudinaryResource != null ? cloudinaryResource.getTamanoArchivo() : null;
    }
}
