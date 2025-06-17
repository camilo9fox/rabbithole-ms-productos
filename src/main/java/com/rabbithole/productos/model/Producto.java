package com.rabbithole.productos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entidad que representa los productos en el sistema de ecommerce.
 * Estructura actualizada según el esquema real de la base de datos.
 */
@Entity
@Table(name = "PRODUCTOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISENO_PERSONALIZADO_ID")
    private DisenoPersonalizado disenoPersonalizado;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no puede tener más de 255 caracteres")
    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Lob
    @Column(name = "DESCRIPCION")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private Categoria categoria;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo = 1; // 1 = activo, 0 = inactivo
    
    @Column(name = "CREADO_EN")
    private LocalDateTime creadoEn;
    
    @Column(name = "ACTUALIZADO_EN")
    private LocalDateTime actualizadoEn;
    
    // Métodos auxiliares para conversión de Integer a Boolean y viceversa
    @Transient
    public Boolean isActivo() {
        return this.activo != null && this.activo == 1;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo ? 1 : 0;
    }

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
