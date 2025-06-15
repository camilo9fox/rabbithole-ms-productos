package com.rabbithole.productos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad que representa las tallas de los productos.
 */
@Entity
@Table(name = "TALLAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Talla implements Serializable {

    @Id
    @Column(name = "ID")
    private String id;

    @NotBlank(message = "El nombre de la talla es obligatorio")
    @Column(name = "NOMBRE", nullable = false, length = 20)
    private String nombre;
    
    // Columna no existente en la base de datos, se marca como transient
    @jakarta.persistence.Transient
    private String descripcion;

    @PositiveOrZero(message = "El precio adicional no puede ser negativo")
    // Posible columna faltante en la base de datos, se marca como transient
    @jakarta.persistence.Transient
    private BigDecimal precioAdicional;
    
    @jakarta.persistence.Transient // Marcado como transient para evitar el error de validación de esquema
    private Boolean activo = true; // La columna no existe en la base de datos
    
    private static final long serialVersionUID = 1L;
}
