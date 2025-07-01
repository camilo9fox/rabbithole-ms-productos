package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

/**
 * Entidad que representa una miniatura (thumbnail) de un ítem en la base de
 * datos.
 * Mapea a la tabla "thumbnails_item".
 */
@Entity
@Table(name = "thumbnails_item", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_carrito_angulo", columnNames = { "item_carrito_id", "tipo_angulo_id" }),
        @UniqueConstraint(name = "uk_item_orden_angulo", columnNames = { "item_orden_id", "tipo_angulo_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "itemCarrito", "itemOrden", "cloudinaryResource" })
@ToString(exclude = { "itemCarrito", "itemOrden", "cloudinaryResource" })
public class ThumbnailItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Ítem del carrito al que pertenece esta miniatura.
     * La relación es Many-to-One (muchas miniaturas pueden pertenecer a un ítem del
     * carrito).
     * Según la restricción CHECK, una miniatura debe tener o un ítem de carrito o
     * un ítem de orden.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_carrito_id")
    @JsonBackReference
    private ItemCarrito itemCarrito;

    /**
     * ID del ítem del carrito, para mantener referencia explícita que se mapea a la
     * columna de la BD
     */
    @Column(name = "item_carrito_id", insertable = false, updatable = false)
    private Long itemCarritoId;

    /**
     * Ítem de la orden al que pertenece esta miniatura.
     * La relación es Many-to-One (muchas miniaturas pueden pertenecer a un ítem de
     * la orden).
     * Según la restricción CHECK, una miniatura debe tener o un ítem de carrito o
     * un ítem de orden.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_orden_id")
    @JsonBackReference
    private ItemOrden itemOrden;

    /**
     * ID del ítem de la orden, para mantener referencia explícita que se mapea a la
     * columna de la BD
     */
    @Column(name = "item_orden_id", insertable = false, updatable = false)
    private Long itemOrdenId;

    /**
     * Tipo de ángulo de la miniatura.
     * La relación es Many-to-One (muchas miniaturas pueden tener el mismo tipo de
     * ángulo).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_angulo_id", nullable = false)
    private TipoAngulo tipoAngulo;

    /**
     * ID del tipo de ángulo, para mantener referencia explícita que se mapea a la
     * columna de la BD
     */
    @Column(name = "tipo_angulo_id", insertable = false, updatable = false, nullable = false)
    private Long tipoAnguloId;

    /**
     * Recurso en Cloudinary que representa la imagen de la miniatura.
     * La relación es Many-to-One (muchas miniaturas pueden apuntar al mismo
     * recurso).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloudinary_resource_id", nullable = false)
    private CloudinaryResource cloudinaryResource;

    /**
     * ID del recurso en Cloudinary, para mantener referencia explícita que se mapea
     * a la columna de la BD
     */
    @Column(name = "cloudinary_resource_id", insertable = false, updatable = false, nullable = false)
    private Long cloudinaryResourceId;

    /**
     * Verifica que el ítem cumple con la restricción de tener solo uno de los dos
     * tipos de ítem asociado.
     */
    @PrePersist
    @PreUpdate
    protected void validateOnlyOneItem() {
        boolean hasItemCarrito = itemCarritoId != null || itemCarrito != null;
        boolean hasItemOrden = itemOrdenId != null || itemOrden != null;

        if (!(hasItemCarrito ^ hasItemOrden)) { // XOR operation - exactly one must be true
            throw new IllegalStateException(
                    "Un ThumbnailItem debe estar asociado exactamente a un ItemCarrito O un ItemOrden, pero no a ambos ni a ninguno.");
        }
    }
}
