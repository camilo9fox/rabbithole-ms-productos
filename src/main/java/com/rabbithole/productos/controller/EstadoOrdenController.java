package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.EstadoOrden;
import com.rabbithole.productos.repository.EstadoOrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Controlador para la gestión de estados de orden
 */
@RestController
@RequestMapping("/estados-orden")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class EstadoOrdenController {

    private final EstadoOrdenRepository estadoOrdenRepository;

    /**
     * Obtiene todos los estados de orden disponibles en el sistema
     * 
     * @return Lista de estados de orden
     */
    @GetMapping
    public ResponseEntity<List<EstadoOrdenDTO>> listarEstadosOrden() {
        log.info("Obteniendo todos los estados de orden");
        List<EstadoOrden> estados = estadoOrdenRepository.findAll();
        List<EstadoOrdenDTO> estadosDTO = estados.stream()
            .map(estado -> new EstadoOrdenDTO(
                estado.getId(),
                estado.getCodigo(),
                estado.getNombre(),
                estado.getDescripcion()
            ))
            .toList();
        return ResponseEntity.ok(estadosDTO);
    }

    /**
     * Obtiene un estado de orden por su ID
     * 
     * @param id ID del estado
     * @return Estado de orden encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<EstadoOrdenDTO> obtenerEstadoOrdenPorId(@PathVariable Long id) {
        log.info("Buscando estado de orden con ID: {}", id);
        return estadoOrdenRepository.findById(id)
                .map(estado -> new EstadoOrdenDTO(
                    estado.getId(),
                    estado.getCodigo(),
                    estado.getNombre(),
                    estado.getDescripcion()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un estado de orden por su código
     * 
     * @param codigo Código del estado (ej: PENDING, PAID)
     * @return Estado de orden encontrado
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EstadoOrdenDTO> obtenerEstadoOrdenPorCodigo(@PathVariable String codigo) {
        log.info("Buscando estado de orden con código: {}", codigo);
        EstadoOrden estado = estadoOrdenRepository.findByCodigo(codigo);
        return estado != null ? 
               ResponseEntity.ok(new EstadoOrdenDTO(
                   estado.getId(),
                   estado.getCodigo(),
                   estado.getNombre(),
                   estado.getDescripcion()
               )) : 
               ResponseEntity.notFound().build();
    }
    
    /**
     * DTO simplificado para EstadoOrden para evitar problemas de serialización
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EstadoOrdenDTO {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
    }
}
