package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.OrdenDTO;
import com.rabbithole.productos.dto.CrearOrdenDTO;
import com.rabbithole.productos.dto.CrearOrdenAnonimaDTO;
import com.rabbithole.productos.service.OrdenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones relacionadas con órdenes.
 */
@RestController
@RequestMapping("/ordenes")
@CrossOrigin(origins = "*")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    /**
     * Obtiene todas las órdenes de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de órdenes del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<OrdenDTO>> obtenerOrdenesPorUsuario(@PathVariable Long usuarioId) {
        List<OrdenDTO> ordenes = ordenService.obtenerOrdenesPorUsuario(usuarioId);
        return new ResponseEntity<>(ordenes, HttpStatus.OK);
    }

    /**
     * Obtiene todas las órdenes del sistema.
     * 
     * @return Lista de todas las órdenes
     */
    @GetMapping
    public ResponseEntity<List<OrdenDTO>> obtenerTodasLasOrdenes() {
        List<OrdenDTO> ordenes = ordenService.obtenerTodasLasOrdenes();
        return new ResponseEntity<>(ordenes, HttpStatus.OK);
    }

    /**
     * Obtiene una orden específica por su ID.
     * 
     * @param id ID de la orden
     * @return La orden encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdenDTO> obtenerOrdenPorId(@PathVariable Long id) {
        OrdenDTO orden = ordenService.obtenerOrdenPorId(id);
        return new ResponseEntity<>(orden, HttpStatus.OK);
    }

    /**
     * Crea una nueva orden a partir de un carrito.
     * 
     * @param crearOrdenDTO DTO con datos para crear la orden
     * @return La orden creada
     */
    @PostMapping
    public ResponseEntity<OrdenDTO> crearOrden(@RequestBody CrearOrdenDTO crearOrdenDTO) {
        OrdenDTO nuevaOrden = ordenService.crearOrden(crearOrdenDTO);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    /**
     * Actualiza el estado de una orden.
     * 
     * @param id        ID de la orden
     * @param estadoId  ID del nuevo estado (puede venir como parámetro de consulta o en el cuerpo)
     * @param requestBody Cuerpo de la solicitud que puede contener el estadoId
     * @return La orden actualizada
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<OrdenDTO> actualizarEstadoOrden(
            @PathVariable Long id,
            @RequestParam(required = false) Long estadoId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        Long estadoIdFinal = estadoId;
        
        // Si no se proporcionó por parámetro, intentamos extraerlo del cuerpo
        if (estadoIdFinal == null && requestBody != null && requestBody.containsKey("estadoId")) {
            Object estadoIdObj = requestBody.get("estadoId");
            if (estadoIdObj instanceof Number number) {
                estadoIdFinal = number.longValue();
            } else if (estadoIdObj instanceof String string) {
                try {
                    estadoIdFinal = Long.parseLong(string);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El estadoId debe ser un número válido");
                }
            }
        }
        
        // Verificar que se proporcionó el ID del estado
        if (estadoIdFinal == null) {
            throw new IllegalArgumentException("Se requiere el ID del estado para actualizar la orden");
        }
        
        OrdenDTO ordenActualizada = ordenService.actualizarEstadoOrden(id, estadoIdFinal);
        return new ResponseEntity<>(ordenActualizada, HttpStatus.OK);
    }

    /**
     * Cancela una orden.
     * 
     * @param id ID de la orden a cancelar
     * @return La orden cancelada
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<OrdenDTO> cancelarOrden(@PathVariable Long id) {
        OrdenDTO ordenCancelada = ordenService.cancelarOrden(id);
        return new ResponseEntity<>(ordenCancelada, HttpStatus.OK);
    }

    /**
     * Crea una nueva orden para un cliente sin cuenta de usuario (anónimo).
     * 
     * @param crearOrdenAnonimaDTO DTO con datos para crear la orden anónima
     * @return La orden creada
     */
    @PostMapping("/anonima")
    public ResponseEntity<OrdenDTO> crearOrdenAnonima(@RequestBody CrearOrdenAnonimaDTO crearOrdenAnonimaDTO) {
        OrdenDTO nuevaOrden = ordenService.crearOrdenAnonima(crearOrdenAnonimaDTO);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }
}
