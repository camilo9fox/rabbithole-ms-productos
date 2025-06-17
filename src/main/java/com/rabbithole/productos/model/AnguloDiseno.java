package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Entidad que representa un ángulo específico de un diseño personalizado.
 * Puede contener un elemento de texto o un elemento de imagen, pero no ambos.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ANGULOS_DISENO")
public class AnguloDiseno implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "TIPO_ANGULO_ID", nullable = false)
    private TipoAngulo tipoAngulo;
    
    @JsonBackReference
    @NotNull
    @ManyToOne
    @JoinColumn(name = "DISENO_PERSONALIZADO_ID", nullable = false)
    private DisenoPersonalizado disenoPersonalizado;
    
    @ManyToOne
    @JoinColumn(name = "ELEMENTO_TEXTO_ID")
    private ElementoTexto elementoTexto;
    
    @ManyToOne
    @JoinColumn(name = "ELEMENTO_IMAGEN_ID")
    private ElementoImagen elementoImagen;
    
    @Lob
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "THUMBNAIL_RESOURCE_ID")
    private CloudinaryResource thumbnailResource;
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Método auxiliar para obtener la URL del thumbnail
     * @return URL del thumbnail
     */
    @Transient
    public String getThumbnailUrl() {
        return thumbnailResource != null ? thumbnailResource.getUrlImagen() : null;
    }
    
    /**
     * Valida que no se asignen simultáneamente un elemento de texto y un elemento de imagen.
     */
    @PrePersist
    @PreUpdate
    public void validarElementos() {
        if (elementoTexto != null && elementoImagen != null) {
            throw new IllegalStateException("Un ángulo de diseño no puede tener simultáneamente un elemento de texto y un elemento de imagen");
        }
    }
}
