package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.Talla;
import com.rabbithole.productos.service.TallaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para manejar operaciones con la entidad Talla.
 */
@RestController
@RequestMapping("/tallas")
public class TallaController {

    private final TallaService tallaService;

    @Autowired
    public TallaController(TallaService tallaService) {
        this.tallaService = tallaService;
    }

    /**
     * Obtiene todas las tallas.
     *
     * @param soloActivas Flag para filtrar solo tallas activas
     * @return Lista de tallas
     */
    @GetMapping
    public ResponseEntity<List<Talla>> getAllTallas(
            @RequestParam(name = "activas", required = false, defaultValue = "true") boolean soloActivas) {
        
        List<Talla> tallas = soloActivas ? 
                tallaService.getTallasActivas() : 
                tallaService.getAllTallas();
        
        return ResponseEntity.ok(tallas);
    }

    /**
     * Obtiene una talla por su ID.
     *
     * @param id ID de la talla
     * @return Talla encontrada o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Talla> getTallaById(@PathVariable String id) {
        return tallaService.getTallaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca tallas por nombre.
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de tallas que coinciden con el criterio
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Talla>> buscarTallasPorNombre(
            @RequestParam(name = "nombre") String nombre) {
        
        List<Talla> tallas = tallaService.buscarTallasPorNombre(nombre);
        return ResponseEntity.ok(tallas);
    }

    /**
     * Crea una nueva talla.
     *
     * @param talla Talla a crear
     * @return Talla creada
     */
    @PostMapping
    public ResponseEntity<Talla> createTalla(@Valid @RequestBody Talla talla) {
        // Generar ID único si no se proporciona
        if (talla.getId() == null) {
            talla.setId(UUID.randomUUID().toString());
        }
        
        Talla nuevaTalla = tallaService.saveTalla(talla);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTalla);
    }

    /**
     * Actualiza una talla existente.
     *
     * @param id ID de la talla a actualizar
     * @param talla Datos actualizados
     * @return Talla actualizada o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Talla> updateTalla(
            @PathVariable String id, 
            @Valid @RequestBody Talla talla) {
        
        return tallaService.getTallaById(id)
                .map(tallaExistente -> {
                    talla.setId(id); // Asegurar que el ID no se modifique
                    return ResponseEntity.ok(tallaService.saveTalla(talla));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Activa o desactiva una talla.
     *
     * @param id ID de la talla
     * @param activar Flag para activar/desactivar
     * @return Talla actualizada o 404 si no existe
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Talla> cambiarEstadoTalla(
            @PathVariable String id,
            @RequestParam(name = "activar") boolean activar) {
        
        return tallaService.cambiarEstadoTalla(id, activar)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
