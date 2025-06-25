package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un carrito de compras en la base de datos.
 * Mapea a la tabla "carritos".
 */
@Entity
@Table(name = "carritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrito implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Usuario al que pertenece este carrito.
     * La relación es Many-to-One (muchos carritos pueden pertenecer a un usuario).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    /**
     * Fecha de creación del carrito.
     */
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    /**
     * Fecha de última actualización del carrito.
     */
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    /**
     * Ítems que contiene este carrito.
     * Definimos una relación One-to-Many con la entidad ItemCarrito.
     */
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemCarrito> items = new ArrayList<>();

    /**
     * Método callback que se ejecuta antes de la inserción de un nuevo registro.
     * Establece las fechas de creación y actualización.
     */
    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Método callback que se ejecuta antes de la actualización de un registro
     * existente.
     * Actualiza la fecha de última actualización.
     */
    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Método para agregar un ítem al carrito.
     * Establece la relación bidireccional entre ItemCarrito y Carrito.
     * 
     * @param item El ítem a agregar al carrito
     */
    public void addItem(ItemCarrito item) {
        items.add(item);
        item.setCarrito(this);
    }

    /**
     * Método para eliminar un ítem del carrito.
     * Elimina la relación bidireccional entre ItemCarrito y Carrito.
     * 
     * @param item El ítem a eliminar del carrito
     */
    public void removeItem(ItemCarrito item) {
        items.remove(item);
        item.setCarrito(null);
    }
}
