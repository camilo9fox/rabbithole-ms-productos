package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un ítem de una orden en la base de datos.
 * Mapea a la tabla "items_orden".
 */
@Entity
@Table(name = "items_orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrden implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Orden a la que pertenece este ítem.
     * La relación es Many-to-One (muchos ítems pueden pertenecer a una orden).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonBackReference
    private Orden orden;

    /**
     * Producto asociado a este ítem.
     * La relación es Many-to-One (muchos ítems pueden ser del mismo producto).
     * Según la restricción CHECK, un ítem debe tener o un producto o un diseño
     * personalizado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    @JsonBackReference
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
     * Tipo de ítem.
     * La relación es Many-to-One (muchos ítems pueden ser del mismo tipo).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_item_id", nullable = false)
    @JsonBackReference
    private TipoItem tipoItem;

    /**
     * ID del tipo de ítem, para mantener referencia explícita que se mapea a la
     * columna de la BD
     */
    @Column(name = "tipo_item_id", insertable = false, updatable = false)
    private Long tipoItemId;

    /**
     * Nombre descriptivo del ítem.
     */
    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

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
     * Cantidad de unidades de este ítem en la orden.
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario del ítem.
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Precio total del ítem (cantidad * precio_unitario).
     */
    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    /**
     * Lista de miniaturas del ítem (usualmente para diseños personalizados o
     * variantes).
     */
    @OneToMany(mappedBy = "itemOrden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThumbnailItem> thumbnail = new ArrayList<>();

    /**
     * Calcula el precio total del ítem (cantidad * precio unitario).
     * Este método es llamado automáticamente antes de guardar en la base de datos.
     */
    @PrePersist
    @PreUpdate
    protected void calcularPrecioTotal() {
        if (precioUnitario != null && cantidad != null) {
            this.precioTotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }

    /**
     * Método de conveniencia para obtener el subtotal.
     * Es un alias para getPrecioTotal().
     *
     * @return El precio total del ítem
     */
    @Transient
    public BigDecimal getSubtotal() {
        return this.precioTotal;
    }
}
