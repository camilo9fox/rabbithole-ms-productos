package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.Producto;
import com.rabbithole.productos.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para manejar operaciones con la entidad Producto.
 */
@RestController
@RequestMapping("/api/productos")
@Slf4j
public class ProductoController {
    
    // Constantes para respuestas paginadas
    private static final String KEY_PRODUCTOS = "productos";
    private static final String KEY_CURRENT_PAGE = "currentPage";
    private static final String KEY_TOTAL_ITEMS = "totalItems";
    private static final String KEY_TOTAL_PAGES = "totalPages";

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Obtiene todos los productos con paginación y ordenación.
     *
     * @param page Número de página (empezando por 0)
     * @param size Tamaño de la página
     * @param sort Campo por el que ordenar
     * @param dir Dirección de la ordenación (ASC o DESC)
     * @param soloActivos Flag para filtrar solo productos activos
     * @return Página de productos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProductos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "nombre") String sort,
            @RequestParam(name = "dir", defaultValue = "ASC") String dir,
            @RequestParam(name = "activos", defaultValue = "true") boolean soloActivos) {

        Sort.Direction sortDir = dir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));
        
        Page<Producto> productosPage = soloActivos ? 
                productoService.getProductosActivos(pageable) : 
                productoService.getAllProductos(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return Producto encontrado o 404 si no existe
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        return producto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca productos por texto en nombre o descripción.
     *
     * @param texto Texto a buscar
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de productos que coinciden con la búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarProductos(
            @RequestParam(name = "texto") String texto,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.buscarProductos(texto, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene productos por categoría.
     *
     * @param categoriaId ID de la categoría
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de productos de la categoría especificada
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Map<String, Object>> getProductosPorCategoria(
            @PathVariable String categoriaId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.getProductosPorCategoria(categoriaId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene productos personalizables.
     *
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de productos personalizables
     */
    @GetMapping("/personalizables")
    public ResponseEntity<Map<String, Object>> getProductosPersonalizables(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.getProductosPersonalizables(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene productos por rango de precios.
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de productos en el rango de precios
     */
    @GetMapping("/precio")
    public ResponseEntity<Map<String, Object>> getProductosPorRangoPrecio(
            @RequestParam(name = "min") BigDecimal precioMin,
            @RequestParam(name = "max") BigDecimal precioMax,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.getProductosPorRangoPrecio(precioMin, precioMax, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene productos por color disponible.
     *
     * @param colorId ID del color
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de productos con el color especificado
     */
    @GetMapping("/color/{colorId}")
    public ResponseEntity<Map<String, Object>> getProductosPorColor(
            @PathVariable String colorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.getProductosPorColor(colorId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene productos por talla disponible.
     *
     * @param tallaId ID de la talla
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de productos con la talla especificada
     */
    @GetMapping("/talla/{tallaId}")
    public ResponseEntity<Map<String, Object>> getProductosPorTalla(
            @PathVariable String tallaId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.getProductosPorTalla(tallaId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_PRODUCTOS, productosPage.getContent());
        response.put(KEY_CURRENT_PAGE, productosPage.getNumber());
        response.put(KEY_TOTAL_ITEMS, productosPage.getTotalElements());
        response.put(KEY_TOTAL_PAGES, productosPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo producto.
     *
     * @param producto Producto a crear
     * @return Producto creado
     */
    @PostMapping
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto) {
        Producto nuevoProducto = productoService.saveProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id ID del producto a actualizar
     * @param producto Datos actualizados
     * @return Producto actualizado o 404 si no existe
     */
    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> updateProducto(
            @PathVariable Long id, 
            @Valid @RequestBody Producto producto) {
        
        Optional<Producto> productoActualizado = productoService.getProductoById(id)
                .map(productoExistente -> {
                    producto.setId(id); // Asegurar que el ID no se modifique
                    return productoService.saveProducto(producto);
                });
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activa o desactiva un producto.
     *
     * @param id ID del producto
     * @param activar Flag para activar/desactivar
     * @return Producto actualizado o 404 si no existe
     */
    @PatchMapping("/productos/{id}/estado")
    public ResponseEntity<Producto> cambiarEstadoProducto(
            @PathVariable Long id,
            @RequestParam(name = "activar") boolean activar) {
        
        Optional<Producto> productoActualizado = productoService.cambiarEstadoProducto(id, activar);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Añade un color a un producto.
     *
     * @param productoId ID del producto
     * @param colorId ID del color
     * @return Producto actualizado o 404 si no existe
     */
    @PostMapping("/productos/{productoId}/colores/{colorId}")
    public ResponseEntity<Producto> agregarColorAProducto(
            @PathVariable Long productoId, 
            @PathVariable String colorId) {
        
        Optional<Producto> productoActualizado = productoService.agregarColorAProducto(productoId, colorId);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un color de un producto.
     *
     * @param productoId ID del producto
     * @param colorId ID del color
     * @return Producto actualizado o 404 si no existe
     */
    @DeleteMapping("/productos/{productoId}/colores/{colorId}")
    public ResponseEntity<Producto> eliminarColorDeProducto(
            @PathVariable Long productoId, 
            @PathVariable String colorId) {
        
        Optional<Producto> productoActualizado = productoService.eliminarColorDeProducto(productoId, colorId);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Añade una talla a un producto.
     *
     * @param productoId ID del producto
     * @param tallaId ID de la talla
     * @return Producto actualizado o 404 si no existe
     */
    @PostMapping("/productos/{productoId}/tallas/{tallaId}")
    public ResponseEntity<Producto> agregarTallaAProducto(
            @PathVariable Long productoId, 
            @PathVariable String tallaId) {
        
        Optional<Producto> productoActualizado = productoService.agregarTallaAProducto(productoId, tallaId);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una talla de un producto.
     *
     * @param productoId ID del producto
     * @param tallaId ID de la talla
     * @return Producto actualizado o 404 si no existe
     */
    @DeleteMapping("/productos/{productoId}/tallas/{tallaId}")
    public ResponseEntity<Producto> eliminarTallaDeProducto(
            @PathVariable Long productoId, 
            @PathVariable String tallaId) {
        
        Optional<Producto> productoActualizado = productoService.eliminarTallaDeProducto(productoId, tallaId);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualiza el stock disponible de un producto.
     *
     * @param productoId ID del producto
     * @param nuevoStock Nuevo valor de stock
     * @return Producto actualizado o 404 si no existe
     */
    @PutMapping("/productos/{productoId}/stock/{nuevoStock}")
    public ResponseEntity<Producto> actualizarStockProducto(
            @PathVariable Long productoId,
            @PathVariable Integer nuevoStock) {
        
        if (nuevoStock < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Producto> productoActualizado = productoService.actualizarStockProducto(productoId, nuevoStock);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene productos por diseño disponible.
     *
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @param pageable Paginación y ordenación
     * @return Página de productos con el diseño especificado
     */
    @GetMapping("/productos/disenos/{disenoPersonalizadoId}")
    public ResponseEntity<Page<Producto>> getProductosPorDisenoPersonalizado(
            @PathVariable Long disenoPersonalizadoId,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        
        Page<Producto> productos = productoService.getProductosPorDisenoPersonalizado(disenoPersonalizadoId, pageable);
        
        return ResponseEntity.ok(productos);
    }

    /**
     * Elimina un diseño de un producto.
     *
     * @param productoId ID del producto
     * @param disenoId ID del diseño
     * @return Producto actualizado o 404 si no existe
     */
    @DeleteMapping("/productos/{productoId}/disenos/{disenoId}")
    public ResponseEntity<Producto> eliminarDisenoDeProducto(
            @PathVariable Long productoId,
            @PathVariable String disenoId) {
        
        Optional<Producto> productoActualizado = productoService.eliminarDisenoDeProducto(productoId, disenoId);
        
        if (productoActualizado.isPresent()) {
            return ResponseEntity.ok(productoActualizado.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
