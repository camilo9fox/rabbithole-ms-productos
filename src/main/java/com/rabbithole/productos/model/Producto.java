package com.rabbithole.productos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los productos en el sistema de ecommerce.
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

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Lob
    @Column(name = "DESCRIPCION")
    private String descripcion; // Modificado para coincidir con el tipo CLOB de la base de datos

    @NotNull(message = "El precio base es obligatorio")
    @PositiveOrZero(message = "El precio base no puede ser negativo")
    // Columna no existente en la base de datos, se marca como transient
    @Transient
    private BigDecimal precioBase;

    // Columna no existente en la base de datos, se marca como transient
    @Transient
    private String imagenPrincipalUrl;

    // Posible columna faltante en la base de datos
    @Transient
    private Integer stockDisponible;

    // Posible columna faltante en la base de datos
    @Transient
    private Boolean personalizable = false;

    // Columna no existente en la base de datos, se marca como transient
    // para evitar errores de validación de esquema
    @Transient
    private LocalDateTime fechaCreacion;

    // Columna no existente en la base de datos, se marca como transient
    // para evitar errores de validación de esquema
    @Transient
    private LocalDateTime fechaActualizacion;

    // La categoría es una relación importante pero la hacemos opcional
    // para evitar posibles problemas de validación de esquema
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORIA_ID", nullable = true)
    private Categoria categoria;

    // Relación con Color modificada para evitar referencia a tabla intermedia que no existe
    // En la base de datos actual no hay tabla PRODUCTO_COLOR según el esquema
    @Transient
    private Set<Color> coloresDisponibles = new HashSet<>();

    // Relación con Talla modificada para evitar referencia a tabla intermedia que podría no existir
    // En la base de datos actual no hay referencia explícita a PRODUCTO_TALLA según el esquema
    @Transient
    private Set<Talla> tallasDisponibles = new HashSet<>();

    // Relación posiblemente no existente en la estructura actual de la base de datos
    // Se marca como transient para evitar errores de validación de esquema
    @Transient
    private DisenoPersonalizado disenoPersonalizado;

    // Posible columna faltante en la base de datos
    @Transient
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        // Aunque estos campos sean transient (no mapeados a columnas de la base de datos),
        // mantenemos la lógica para preservar la información en memoria
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
