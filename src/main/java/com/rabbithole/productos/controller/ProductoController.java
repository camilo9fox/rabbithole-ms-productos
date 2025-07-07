package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.ProductoDTO;
import com.rabbithole.productos.model.Producto;
import com.rabbithole.productos.service.DTOConverterService;
import com.rabbithole.productos.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para manejar operaciones CRUD con la entidad Producto.
 */
@RestController
@RequestMapping("/productos")
@Slf4j
public class ProductoController {
    
    // Constantes para respuestas paginadas
    private static final String KEY_PRODUCTOS = "productos";
    private static final String KEY_CURRENT_PAGE = "currentPage";
    private static final String KEY_TOTAL_ITEMS = "totalItems";
    private static final String KEY_TOTAL_PAGES = "totalPages";

    private final ProductoService productoService;
    private final DTOConverterService dtoConverterService;

    @Autowired
    public ProductoController(ProductoService productoService, DTOConverterService dtoConverterService) {
        this.productoService = productoService;
        this.dtoConverterService = dtoConverterService;
    }

    /**
     * Obtiene todos los productos con paginaciÃ³n y ordenaciÃ³n.
     *
     * @param page NÃºmero de pÃ¡gina (empezando por 0)
     * @param size TamaÃ±o de la pÃ¡gina
     * @param sort Campo por el que ordenar
     * @param dir DirecciÃ³n de la ordenaciÃ³n (ASC o DESC)
     * @param soloActivos Flag para filtrar solo productos activos
     * @return PÃ¡gina de productos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProductos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "nombre") String sort,
            @RequestParam(name = "dir", defaultValue = "ASC") String dir,
            @RequestParam(name = "activos", defaultValue = "false") boolean soloActivos) {

        Sort.Direction sortDir = dir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));
        
        Page<Producto> productosPage = soloActivos ? 
                productoService.getProductosActivos(pageable) : 
                productoService.getAllProductos(pageable);
        
        // Convertir entidades a DTOs
        Page<ProductoDTO> productoDTOsPage = dtoConverterService.convertToProductoDTOPage(productosPage);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productoDTOsPage.getContent());
        response.put(KEY_CURRENT_PAGE, productoDTOsPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productoDTOsPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productoDTOsPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return Producto encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Long id) {
        log.debug("REST request para obtener Producto con ID: {}", id);
        Optional<Producto> producto = productoService.getProductoById(id);
        
        return producto
                .map(dtoConverterService::convertToProductoDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo producto.
     *
     * @param producto Producto a crear
     * @return Producto creado
     */
    @PostMapping
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto) {
        log.debug("REST request para crear un nuevo Producto: {}", producto);
        
        // Prevenir actualizaciÃ³n de un producto existente vÃ­a endpoint de creaciÃ³n
        if (producto.getId() != null) {
            return ResponseEntity.badRequest().header("error", "Un nuevo producto no puede tener ID").build();
        }
        
        Producto nuevoProducto = productoService.createProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id ID del producto a actualizar
     * @param producto Datos actualizados
     * @return Producto actualizado o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(
            @PathVariable Long id, 
            @Valid @RequestBody Producto producto) {
        log.debug("REST request para actualizar Producto ID: {}, datos: {}", id, producto);
        
        Optional<Producto> productoActualizado = productoService.updateProducto(id, producto);
        
        return productoActualizado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto a eliminar
     * @return 204 No Content si se eliminÃ³ correctamente, 404 si no existÃ­a
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteProducto(@PathVariable Long id) {
        log.debug("REST request para eliminar Producto ID: {}", id);
        
        boolean eliminado = productoService.deleteProducto(id);
        if (eliminado) {
            return ResponseEntity.ok(Map.of("deleted", true));
        } else {
            // Puede ser inexistente o referenciado; devolvemos 409 para alertar conflicto.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("deleted", false));
        }
    }
    
    /**
     * Cambia el estado activo de un producto (toggle entre activo/inactivo).
     *
     * @param id ID del producto a modificar
     * @return Producto con el estado activo actualizado o 404 si no existe
     */
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ProductoDTO> toggleActivoProducto(@PathVariable Long id) {
        log.debug("REST request para cambiar estado activo del Producto ID: {}", id);
        
        Optional<Producto> optProducto = productoService.getProductoById(id);
        
        if (optProducto.isPresent()) {
            Producto producto = optProducto.get();
            // Invertir el valor de activo
            producto.setActivo(!producto.isActivo());
            
            Producto productoActualizado = productoService.saveProducto(producto);
            return ResponseEntity.ok(dtoConverterService.convertToProductoDTO(productoActualizado));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
