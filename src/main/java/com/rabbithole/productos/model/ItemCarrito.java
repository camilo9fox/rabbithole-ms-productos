package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un ítem del carrito de compras en la base de datos.
 * Mapea a la tabla "items_carrito".
 */
@Entity
@Table(name = "items_carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrito implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Carrito al que pertenece este ítem.
     * La relación es Many-to-One (muchos ítems pueden pertenecer a un carrito).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    @JsonBackReference
    private Carrito carrito;

    /**
     * Producto asociado a este ítem.
     * La relación es Many-to-One (muchos ítems pueden ser del mismo producto).
     * Según la restricción CHECK, un ítem debe tener o un producto o un diseño
     * personalizado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /**
     * ID del producto, para mantener referencia explícita que se mapea a la columna
     * de la BD
     */
    @Column(name = "producto_id", insertable = false, updatable = false)
    private Long productoId;

    /**
     * Diseño personalizado asociado a este ítem.
     * La relación es Many-to-One (muchos ítems pueden ser del mismo diseño
     * personalizado).
     * Según la restricción CHECK, un ítem debe tener o un producto o un diseño
     * personalizado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diseno_personalizado_id")
    private DisenoPersonalizado disenoPersonalizado;

    /**
     * ID del diseño personalizado, para mantener referencia explícita que se mapea
     * a la columna de la BD
     */
    @Column(name = "diseno_personalizado_id", insertable = false, updatable = false)
    private Long disenoPersonalizadoId;

    /**
     * Cantidad de unidades de este ítem en el carrito.
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    /**
     * Precio unitario del ítem.
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Color del ítem.
     * La relación es Many-to-One (muchos ítems pueden ser del mismo color).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    /**
     * ID del color, para mantener referencia explícita que se mapea a la columna de
     * la BD
     */
    @Column(name = "color_id", insertable = false, updatable = false, nullable = false)
    private String colorId;

    /**
     * Talla del ítem.
     * La relación es Many-to-One (muchos ítems pueden ser de la misma talla).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talla_id")
    private Talla talla;

    /**
     * ID de la talla, para mantener referencia explícita que se mapea a la columna
     * de la BD
     */
    @Column(name = "talla_id", insertable = false, updatable = false, nullable = false)
    private String tallaId;

    /**
     * Tipo de ítem del producto.
     * La relación es Many-to-One (muchos ítems pueden ser del mismo tipo).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_item_id", nullable = false)
    private TipoItem tipoItem;

    /**
     * ID del tipo de ítem, para mantener referencia explícita que se mapea a la
     * columna de la BD
     */
    @Column(name = "tipo_item_id", insertable = false, updatable = false)
    private Long tipoItemId;

    /**
     * Lista de thumbnails asociados a este ítem del carrito.
     */
    @OneToMany(mappedBy = "itemCarrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ThumbnailItem> thumbnails;

    /**
     * Fecha de creación del ítem.
     * Marcada como transient porque no existe la columna en la base de datos.
     */
    @Transient
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización del ítem.
     * Marcada como transient porque no existe la columna en la base de datos.
     */
    @Transient
    private LocalDateTime ultimaActualizacion;

    /**
     * Método callback que se ejecuta antes de la inserción de un nuevo registro.
     * Establece las fechas de creación y actualización.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }

    /**
     * Método callback que se ejecuta antes de la actualización de un registro
     * existente.
     * Actualiza la fecha de última actualización.
     */
    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }

    /**
     * Calcula el subtotal de este ítem (precio unitario * cantidad).
     * 
     * @return El subtotal del ítem
     */
    @Transient
    public BigDecimal getSubtotal() {
        if (precioUnitario != null && cantidad != null) {
            return precioUnitario.multiply(new BigDecimal(cantidad));
        }
        return BigDecimal.ZERO;
    }
}
