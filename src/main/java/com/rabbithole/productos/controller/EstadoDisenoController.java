package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.EstadoDiseno;
import com.rabbithole.productos.repository.EstadoDisenoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de estados de diseño personalizado
 */
@RestController
@RequestMapping("/estados-diseno")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class EstadoDisenoController {

    private final EstadoDisenoRepository estadoDisenoRepository;

    /**
     * Obtiene todos los estados de diseño disponibles en el sistema
     * 
     * @return Lista de estados de diseño
     */
    @GetMapping
    public ResponseEntity<List<EstadoDiseno>> listarEstadosDiseno() {
        log.info("Obteniendo todos los estados de diseño");
        List<EstadoDiseno> estados = estadoDisenoRepository.findAll();
        return ResponseEntity.ok(estados);
    }

    /**
     * Obtiene un estado de diseño por su ID
     * 
     * @param id ID del estado
     * @return Estado de diseño encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<EstadoDiseno> obtenerEstadoDisenoById(@PathVariable Long id) {
        log.info("Buscando estado de diseño con ID: {}", id);
        return estadoDisenoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un estado de diseño por su código
     * 
     * @param codigo Código del estado (ej: PENDING, APPROVED, REJECTED)
     * @return Estado de diseño encontrado
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EstadoDiseno> obtenerEstadoDisenoByCode(@PathVariable String codigo) {
        log.info("Buscando estado de diseño con código: {}", codigo);
        return estadoDisenoRepository.findByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
