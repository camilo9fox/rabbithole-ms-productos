package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.Categoria;
import com.rabbithole.productos.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controlador REST para manejar operaciones con la entidad Categoria.
 */
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Autowired
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Obtiene todas las categorías.
     *
     * @return Lista de categorías
     */
    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorias() {
        List<Categoria> categorias = categoriaService.getAllCategorias();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id ID de la categoría
     * @return Categoría encontrada o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) {
        return categoriaService.getCategoriaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca categorías por nombre.
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de categorías que coinciden con el criterio
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Categoria>> buscarCategoriasPorNombre(
            @RequestParam(name = "nombre") String nombre) {
        
        List<Categoria> categorias = categoriaService.buscarCategoriasPorNombre(nombre);
        return ResponseEntity.ok(categorias);
    }

    /**
     * Crea una nueva categoría.
     *
     * @param categoria Categoría a crear
     * @return Categoría creada
     */
    @PostMapping
    public ResponseEntity<Categoria> createCategoria(@Valid @RequestBody Categoria categoria) {
        // Generar ID numérico único si no se proporciona
        if (categoria.getId() == null) {
            // Generar un número largo aleatorio positivo usando ThreadLocalRandom (más eficiente)
            long randomId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            categoria.setId(randomId);
        }
        
        Categoria nuevaCategoria = categoriaService.saveCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }

    /**
     * Actualiza una categoría existente.
     *
     * @param id ID de la categoría a actualizar
     * @param categoria Datos actualizados
     * @return Categoría actualizada o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> updateCategoria(
            @PathVariable Long id, 
            @Valid @RequestBody Categoria categoria) {
        
        return categoriaService.getCategoriaById(id)
                .map(categoriaExistente -> {
                    categoria.setId(id); // Asegurar que el ID no se modifique
                    return ResponseEntity.ok(categoriaService.saveCategoria(categoria));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Este endpoint ha sido deshabilitado debido a que el campo 'activo' ya no existe en la entidad Categoria.
     * Devolvemos un estado 410 (Gone) para indicar que este recurso ya no está disponible.
     *
     * @param id ID de la categoría
     * @param activar Flag para activar/desactivar (ya no utilizado)
     * @return Error 410 Gone
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstadoCategoria(
            @PathVariable Long id,
            @RequestParam(name = "activar", required = false) boolean activar) {
        
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body("Este endpoint ha sido deshabilitado porque el campo 'activo' ya no existe en la entidad Categoria");
    }
}
