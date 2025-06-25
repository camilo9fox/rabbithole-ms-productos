package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de cambios de estado de una orden en la base de datos.
 * Mapea a la tabla "historial_estados_orden".
 */
@Entity
@Table(name = "historial_estados_orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadosOrden implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Orden a la que pertenece este registro de historial.
     * La relación es Many-to-One (muchos registros de historial pueden pertenecer a una orden).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonBackReference
    private Orden orden;

    /**
     * Estado al que cambió la orden.
     * La relación es Many-to-One (muchos registros de historial pueden tener el mismo estado).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoOrden estado;

    /**
     * Fecha y hora en la que se registró el cambio de estado.
     */
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Nota o comentario adicional sobre el cambio de estado.
     */
    @Column(name = "nota", columnDefinition = "CLOB")
    private String nota;

    /**
     * Método callback que se ejecuta antes de la inserción de un nuevo registro.
     * Establece la fecha de cambio si no se ha especificado.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
