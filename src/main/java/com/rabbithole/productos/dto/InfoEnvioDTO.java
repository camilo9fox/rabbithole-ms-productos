package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la información de envío en una orden
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoEnvioDTO {
    
    /**
     * Nombre completo del destinatario
     */
    private String nombreCompleto;
    
    /**
     * Nombre para compatibilidad con el cliente
     * @return el valor de nombreCompleto
     */
    public String getNombre() {
        return nombreCompleto;
    }

    /**
     * Establece el nombre (alias de setNombreCompleto)
     * @param nombre el nombre a establecer
     */
    public void setNombre(String nombre) {
        this.nombreCompleto = nombre;
    }
    
    /**
     * Dirección completa de entrega
     */
    private String direccion;
    
    /**
     * Ciudad de entrega
     */
    private String ciudad;
    
    /**
     * Estado o provincia
     */
    private String estado;
    
    /**
     * Código postal
     */
    private String codigoPostal;
    
    /**
     * País de entrega (por defecto "Chile")
     */
    private String pais;
    
    /**
     * Teléfono de contacto
     */
    private String telefono;
    
    /**
     * Correo electrónico del destinatario
     */
    private String email;
}
