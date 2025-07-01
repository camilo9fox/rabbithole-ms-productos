package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un usuario en la base de datos.
 * Mapea a la tabla "usuarios".
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "ordenes", "carritos", "roles" })
@ToString(exclude = { "ordenes", "carritos", "roles" })
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Email del usuario. Debe ser único.
     */
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    /**
     * Nombre del usuario.
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Apellido del usuario. Puede ser nulo.
     */
    @Column(name = "apellido", length = 100)
    private String apellido;

    /**
     * Identificador único del usuario proveniente del sistema de autenticación.
     */
    @Column(name = "oid", nullable = false, length = 50, unique = true)
    private String oid;

    /**
     * Teléfono del usuario.
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Dirección del usuario.
     */
    @Column(name = "direccion", length = 200)
    private String direccion;

    /**
     * Ciudad del usuario.
     */
    @Column(name = "ciudad", length = 100)
    private String ciudad;

    /**
     * Estado o provincia del usuario.
     */
    @Column(name = "estado", length = 100)
    private String estado;

    /**
     * País del usuario.
     */
    @Column(name = "pais", length = 100)
    private String pais;

    /**
     * Código postal de la dirección del usuario.
     */
    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    /**
     * Relación inversa con los carritos del usuario.
     * Un usuario puede tener múltiples carritos.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Carrito> carritos = new ArrayList<>();

    /**
     * Método de conveniencia para obtener el nombre completo del usuario.
     * 
     * @return Nombre completo (nombre y apellido) del usuario
     */
    @Transient
    public String getNombreCompleto() {
        if (apellido != null && !apellido.isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }
}
