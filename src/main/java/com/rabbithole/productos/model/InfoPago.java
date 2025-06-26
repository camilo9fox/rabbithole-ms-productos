package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

/**
 * Entidad que representa la información de pago para una orden.
 * Mapea a la tabla "info_pago" en la base de datos.
 */
@Entity
@Table(name = "info_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoPago implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * ID del método de pago utilizado
     */
    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;
    
    /**
     * Últimos 4 dígitos de la tarjeta de crédito/débito
     */
    @Column(name = "ultimos_digitos")
    private String ultimosDigitos;
    
    /**
     * Nombre del titular de la tarjeta
     */
    @Column(name = "titular_tarjeta")
    private String titularTarjeta;
    
    /**
     * ID de transacción del procesador de pagos
     */
    @Column(name = "id_transaccion")
    private String idTransaccion;

    /**
     * Orden asociada a esta información de pago
     */
    @OneToOne
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonBackReference
    private Orden orden;
    
    public void setOrden(Orden orden) {
        this.orden = orden;
        if (orden != null && orden.getInfoPago() != this) {
            orden.setInfoPago(this);
        }
    }
}
