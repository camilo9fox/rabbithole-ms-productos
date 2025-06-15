package com.rabbithole.productos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa las categorías de productos.
 */
@Entity
@Table(name = "CATEGORIAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Lob
    @Column(name = "DESCRIPCION")
    private String descripcion;

    // El campo imagenUrl se eliminó para coincidir con la estructura de la base de datos
    // El campo activo se eliminó para coincidir con la estructura de la base de datos
}
