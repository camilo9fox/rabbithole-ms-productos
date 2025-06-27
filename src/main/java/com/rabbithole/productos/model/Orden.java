package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una orden de compra en la base de datos.
 * Mapea a la tabla "ordenes".
 */
@Entity
@Table(name = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = false)
public class Orden implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Usuario que realizó la orden.
     * La relación es Many-to-One (muchas órdenes pueden ser de un usuario).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Estado actual de la orden.
     * La relación es Many-to-One (muchas órdenes pueden estar en el mismo estado).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private EstadoOrden estado;

    /**
     * Precio total de la orden.
     */
    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    /**
     * Código de seguimiento único para la orden.
     */
    @Column(name = "codigo_seguimiento", length = 50, unique = true)
    private String codigoSeguimiento;

    /**
     * Fecha de creación de la orden.
     */
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    /**
     * Fecha de última actualización de la orden.
     */
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    /**
     * Ítems que componen esta orden.
     * Definimos una relación One-to-Many con la entidad ItemOrden.
     */
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemOrden> items = new ArrayList<>();

    /**
     * Historial de estados por los que ha pasado la orden.
     */
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<HistorialEstadosOrden> historialEstados = new ArrayList<>();
    
    /**
     * Información de envío asociada a esta orden.
     * Relación One-to-One con InfoEnvio.
     */
    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private InfoEnvio infoEnvio;
    
    /**
     * Información de pago asociada a esta orden.
     * Relación One-to-One con InfoPago.
     */
    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private InfoPago infoPago;

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
     * Método callback que se ejecuta antes de la actualización de un registro existente.
     * Actualiza la fecha de última actualización.
     */
    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Método para agregar un ítem a la orden.
     * Establece la relación bidireccional entre ItemOrden y Orden.
     * 
     * @param item El ítem a agregar a la orden
     */
    public void addItem(ItemOrden item) {
        items.add(item);
        item.setOrden(this);
    }

    /**
     * Método para eliminar un ítem de la orden.
     * Elimina la relación bidireccional entre ItemOrden y Orden.
     * 
     * @param item El ítem a eliminar de la orden
     */
    public void removeItem(ItemOrden item) {
        items.remove(item);
        item.setOrden(null);
    }

    /**
     * Método para agregar un registro al historial de estados.
     * Establece la relación bidireccional entre HistorialEstadosOrden y Orden.
     * 
     * @param historial El registro de historial a agregar
     */
    public void addHistorialEstado(HistorialEstadosOrden historial) {
        historialEstados.add(historial);
        historial.setOrden(this);
    }
}
