package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import com.rabbithole.productos.service.ProductoPersonalizadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar productos personalizados y todas sus entidades relacionadas
 */
@RestController
@RequestMapping("/productos-personalizados")
@RequiredArgsConstructor
@Slf4j
public class ProductoPersonalizadoController {

    private final ProductoPersonalizadoService productoPersonalizadoService;
    
    /**
     * Endpoint para crear un producto personalizado completo
     */
    @PostMapping
    public ResponseEntity<DisenoPersonalizadoDTO> crearProductoPersonalizado(
            @RequestBody DisenoPersonalizadoDTO disenoDTO) {
        log.info("Solicitud para crear un producto personalizado");
        DisenoPersonalizadoDTO createdDiseno = productoPersonalizadoService.crearDisenoPersonalizado(disenoDTO);
        return new ResponseEntity<>(createdDiseno, HttpStatus.CREATED);
    }
    
    /**
     * Endpoint para obtener un producto personalizado por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DisenoPersonalizadoDTO> obtenerProductoPersonalizado(@PathVariable Long id) {
        log.info("Solicitud para obtener producto personalizado con ID: {}", id);
        DisenoPersonalizadoDTO diseno = productoPersonalizadoService.obtenerDisenoPersonalizado(id);
        return ResponseEntity.ok(diseno);
    }
    
    /**
     * Endpoint para obtener todos los productos personalizados de un usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<DisenoPersonalizadoDTO>> obtenerProductosPorUsuario(@PathVariable Long usuarioId) {
        log.info("Solicitud para obtener productos personalizados del usuario con ID: {}", usuarioId);
        List<DisenoPersonalizadoDTO> disenos = productoPersonalizadoService.obtenerDisenosPorUsuario(usuarioId);
        return ResponseEntity.ok(disenos);
    }
    
    /**
     * Endpoint para actualizar un producto personalizado existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<DisenoPersonalizadoDTO> actualizarProductoPersonalizado(
            @PathVariable Long id, 
            @RequestBody DisenoPersonalizadoDTO disenoDTO) {
        log.info("Solicitud para actualizar producto personalizado con ID: {}", id);
        DisenoPersonalizadoDTO updatedDiseno = productoPersonalizadoService.actualizarDisenoPersonalizado(id, disenoDTO);
        return ResponseEntity.ok(updatedDiseno);
    }
    
    /**
     * Endpoint para eliminar un producto personalizado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoPersonalizado(@PathVariable Long id) {
        log.info("Solicitud para eliminar producto personalizado con ID: {}", id);
        productoPersonalizadoService.eliminarDisenoPersonalizado(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Endpoint para actualizar el estado de un producto personalizado
     * Acepta el estadoId ya sea como parámetro de consulta o en el cuerpo de la solicitud
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<DisenoPersonalizadoDTO> actualizarEstadoDiseno(
            @PathVariable Long id,
            @RequestParam(required = false) Long estadoId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        log.info("Solicitud para actualizar estado del producto personalizado con ID: {}", id);
        
        // Extraer parámetros del body o query params
        Long estadoIdFinal = obtenerEstadoId(estadoId, requestBody);
        String motivoRechazo = extraerCampoTexto(requestBody, "motivoRechazo");
        String notasModificacion = extraerCampoTexto(requestBody, "notasModificacion");
        
        // Llamar al servicio para actualizar el estado
        DisenoPersonalizadoDTO disenoActualizado = productoPersonalizadoService.actualizarEstadoDiseno(
                id, estadoIdFinal, motivoRechazo, notasModificacion);
        
        return ResponseEntity.ok(disenoActualizado);
    }
    
    /**
     * Extrae y valida el ID del estado desde parámetros de consulta o cuerpo de la solicitud
     */
    private Long obtenerEstadoId(Long estadoIdParam, Map<String, Object> requestBody) {
        // Si ya viene como parámetro, lo usamos directamente
        if (estadoIdParam != null) {
            return estadoIdParam;
        }
        
        // Si no, intentamos extraerlo del cuerpo
        if (requestBody != null && requestBody.containsKey("estadoId")) {
            Object estadoIdObj = requestBody.get("estadoId");
            if (estadoIdObj instanceof Number number) {
                return number.longValue();
            } else if (estadoIdObj instanceof String string) {
                try {
                    return Long.parseLong(string);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El estadoId debe ser un número válido");
                }
            }
        }
        
        // Si no se encontró el estadoId
        throw new IllegalArgumentException("Se debe proporcionar el estadoId como parámetro de consulta o en el cuerpo de la solicitud");
    }
    
    /**
     * Extrae un campo de texto del cuerpo de la solicitud
     */
    private String extraerCampoTexto(Map<String, Object> requestBody, String nombreCampo) {
        if (requestBody != null && requestBody.containsKey(nombreCampo)) {
            Object obj = requestBody.get(nombreCampo);
            if (obj instanceof String string) {
                return string;
            }
        }
        return null;
    }
}
