package com.rabbithole.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa la tabla de colores en la base de datos.
 * Esta clase se usa para probar la conexión a la base de datos Oracle.
 */
@Entity
@Table(name = "colores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color implements Serializable {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Column(name = "codigo_hex", nullable = false)
    private String codigoHex;
    
    @Column(name = "texto_preview", nullable = false)
    private String textoPreview;
    
    @Column(name = "precio_adicional", nullable = false)
    private Integer precioAdicional;
    
    private static final long serialVersionUID = 1L;
}
