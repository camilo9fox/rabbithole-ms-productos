package com.rabbithole.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para recibir los datos necesarios para crear una orden sin usuario registrado.
 * El carrito para usuarios anónimos está en memoria del cliente, no en la base de datos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrearOrdenAnonimaDTO {
    
    /**
     * Lista de ítems del carrito en memoria
     */
    private List<ItemCarritoMemoriaDTO> items;
    
    /**
     * Información de envío para la orden
     */
    private InfoEnvioDTO infoEnvio;
    
    /**
     * Información de pago para la orden
     */
    private InfoPagoDTO infoPago;
    
    /**
     * (Opcional) ID del usuario si se desea asociar la orden anónima a un usuario existente.
     */
    private Long usuarioId;
}
