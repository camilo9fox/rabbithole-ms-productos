package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Entidad que representa los estados posibles de una orden en la base de datos.
 * Mapea a la tabla "estados_orden".
 */
@Entity
@Table(name = "estados_orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "ordenes", "historialEstados" })
@ToString(exclude = { "ordenes", "historialEstados" })
public class EstadoOrden implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Código único que identifica el estado de la orden.
     * Ejemplos: PENDIENTE, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO.
     */
    @Column(name = "codigo", length = 30, nullable = false, unique = true)
    private String codigo;

    /**
     * Nombre descriptivo del estado.
     */
    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    /**
     * Descripción detallada del estado.
     */
    @Column(name = "descripcion", columnDefinition = "CLOB")
    private String descripcion;

    /**
     * Órdenes que actualmente se encuentran en este estado.
     * Esta es una relación One-to-Many (un estado puede tener muchas órdenes).
     */
    @OneToMany(mappedBy = "estado", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JsonManagedReference
    private Set<Orden> ordenes = new HashSet<>();

    /**
     * Registros de historial que utilizan este estado.
     * Esta es una relación One-to-Many (un estado puede estar en muchos registros
     * de historial).
     */
    @OneToMany(mappedBy = "estado")
    @JsonBackReference
    private List<HistorialEstadosOrden> historialRegistros = new ArrayList<>();
}
