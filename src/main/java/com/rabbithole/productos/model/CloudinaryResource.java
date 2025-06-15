package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que centraliza los recursos de imágenes en Cloudinary.
 * Esta tabla almacena toda la información relacionada con imágenes subidas a Cloudinary.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CLOUDINARY_RESOURCES")
public class CloudinaryResource implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URL_IMAGEN", nullable = false)
    @Lob
    private String urlImagen;

    @Column(name = "PUBLIC_ID", nullable = false)
    private String publicId;

    @Column(name = "TIPO_IMAGEN")
    private String tipoImagen;

    @Column(name = "NOMBRE_ARCHIVO")
    private String nombreArchivo;

    @Column(name = "TAMANO_ARCHIVO")
    private Long tamanoArchivo;

    @Column(name = "CREADO_EN", nullable = false)
    private LocalDateTime creadoEn;

    private static final long serialVersionUID = 1L;
    
    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
    }
}
