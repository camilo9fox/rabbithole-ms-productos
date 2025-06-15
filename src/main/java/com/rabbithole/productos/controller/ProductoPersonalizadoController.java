package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import com.rabbithole.productos.service.ProductoPersonalizadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
