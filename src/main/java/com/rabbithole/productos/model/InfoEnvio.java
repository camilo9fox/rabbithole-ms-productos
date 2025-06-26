package com.rabbithole.productos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

/**
 * Entidad que representa la información de envío para una orden.
 * Mapea a la tabla "info_envio" en la base de datos.
 */
@Entity
@Table(name = "info_envio")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = false)
public class InfoEnvio implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Nombre completo del destinatario
     */
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;
    
    /**
     * Dirección completa de entrega
     */
    @Column(name = "direccion", nullable = false, columnDefinition = "CLOB")
    private String direccion;
    
    /**
     * Ciudad de entrega
     */
    @Column(name = "ciudad", nullable = false)
    private String ciudad;
    
    /**
     * Estado o provincia
     */
    @Column(name = "estado", nullable = false)
    private String estado;
    
    /**
     * Código postal
     */
    @Column(name = "codigo_postal", nullable = false)
    private String codigoPostal;
    
    /**
     * País de entrega, por defecto "Chile"
     */
    @Column(name = "pais", nullable = false)
    private String pais;
    
    /**
     * Teléfono de contacto
     */
    @Column(name = "telefono", nullable = false)
    private String telefono;
    
    /**
     * Correo electrónico del destinatario
     */
    @Column(name = "email", nullable = false)
    private String email;
    
    /**
     * Orden asociada a esta información de envío
     */
    @OneToOne
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Orden orden;
    
    @PrePersist
    public void prePersist() {
        // Valores por defecto
        if (pais == null) {
            pais = "Chile";
        }
    }
    
    public void setOrden(Orden orden) {
        this.orden = orden;
        if (orden != null && orden.getInfoEnvio() != this) {
            orden.setInfoEnvio(this);
        }
    }
}
