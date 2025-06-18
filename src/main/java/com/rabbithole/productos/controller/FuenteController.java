package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.Fuente;
import com.rabbithole.productos.service.FuenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para manejar operaciones con la entidad Fuente.
 */
@RestController
@RequestMapping("/fuentes")
public class FuenteController {

    private final FuenteService fuenteService;

    @Autowired
    public FuenteController(FuenteService fuenteService) {
        this.fuenteService = fuenteService;
    }

    /**
     * Endpoint para obtener todas las fuentes.
     * 
     * @return Lista de fuentes
     */
    @GetMapping
    public ResponseEntity<List<Fuente>> getAllFuentes() {
        List<Fuente> fuentes = fuenteService.getAllFuentes();
        return ResponseEntity.ok(fuentes);
    }

    /**
     * Endpoint para obtener una fuente por su ID.
     * 
     * @param id ID de la fuente
     * @return Fuente encontrada o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Fuente> getFuenteById(@PathVariable Long id) {
        Optional<Fuente> fuente = fuenteService.getFuenteById(id);
        return fuente.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para crear una nueva fuente.
     * 
     * @param fuente Datos de la fuente a crear
     * @return Fuente creada
     */
    @PostMapping
    public ResponseEntity<Fuente> createFuente(@RequestBody Fuente fuente) {
        Fuente savedFuente = fuenteService.saveFuente(fuente);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFuente);
    }

    /**
     * Endpoint para actualizar una fuente existente.
     * 
     * @param id ID de la fuente a actualizar
     * @param fuente Nuevos datos de la fuente
     * @return Fuente actualizada o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Fuente> updateFuente(@PathVariable Long id, @RequestBody Fuente fuente) {
        Fuente updatedFuente = fuenteService.updateFuente(id, fuente);
        if (updatedFuente != null) {
            return ResponseEntity.ok(updatedFuente);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Endpoint para eliminar una fuente.
     * 
     * @param id ID de la fuente a eliminar
     * @return 204 sin contenido si se elimina correctamente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFuente(@PathVariable Long id) {
        fuenteService.deleteFuente(id);
        return ResponseEntity.noContent().build();
    }
}
