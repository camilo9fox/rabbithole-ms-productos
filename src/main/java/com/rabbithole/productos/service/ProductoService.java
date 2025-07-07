package com.rabbithole.productos.service;

import com.rabbithole.productos.model.Categoria;
import com.rabbithole.productos.model.DisenoPersonalizado;
import com.rabbithole.productos.model.Producto;
import com.rabbithole.productos.repository.CategoriaRepository;
import com.rabbithole.productos.repository.DisenoPersonalizadoRepository;
import com.rabbithole.productos.repository.ItemCarritoRepository;
import com.rabbithole.productos.repository.ItemOrdenRepository;
import com.rabbithole.productos.repository.ProductoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para la gestiÃ³n de productos.
 * Implementa las 5 operaciones CRUD bÃ¡sicas: getAll, getById, create, update y delete.
 */
@Service
@Slf4j
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DisenoPersonalizadoRepository disenoPersonalizadoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ItemOrdenRepository itemOrdenRepository;

    @Autowired
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            DisenoPersonalizadoRepository disenoPersonalizadoRepository,
            ItemCarritoRepository itemCarritoRepository,
            ItemOrdenRepository itemOrdenRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.disenoPersonalizadoRepository = disenoPersonalizadoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.itemOrdenRepository = itemOrdenRepository;
    }

    /**
     * Obtiene todos los productos con paginaciÃ³n.
     *
     * @param pageable InformaciÃ³n de paginaciÃ³n
     * @return PÃ¡gina de productos
     */
    @Transactional(readOnly = true)
    public Page<Producto> getAllProductos(Pageable pageable) {
        log.debug("Obteniendo todos los productos con paginaciÃ³n");
        return productoRepository.findAll(pageable);
    }

    /**
     * Obtiene todos los productos activos con paginaciÃ³n.
     *
     * @param pageable InformaciÃ³n de paginaciÃ³n
     * @return PÃ¡gina de productos activos
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosActivos(Pageable pageable) {
        log.debug("Obteniendo productos activos con paginaciÃ³n");
        return productoRepository.findByActivo(1, pageable);
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return Producto encontrado o vacÃ­o si no existe
     */
    @Transactional(readOnly = true)
    public Optional<Producto> getProductoById(Long id) {
        log.debug("Buscando producto con ID: {}", id);
        return productoRepository.findById(id);
    }
    
    /**
     * Crea un nuevo producto.
     *
     * @param producto Producto a crear
     * @return Producto creado
     */
    public Producto createProducto(Producto producto) {
        log.debug("Creando nuevo producto: {}", producto.getNombre());
        
        // Asegurar que es un nuevo producto
        producto.setId(null);
        
        // Establecer el producto como activo
        producto.setActivo(true);
        
        // Establecer fechas de creaciÃ³n y actualizaciÃ³n
        LocalDateTime now = LocalDateTime.now();
        producto.setCreadoEn(now);
        producto.setActualizadoEn(now);
        
        // Verificar y establecer relaciones con entidades existentes
        resolverRelacionesEntidad(producto);
        
        return productoRepository.save(producto);
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id ID del producto a actualizar
     * @param productoDetails Detalles del producto a actualizar
     * @return Producto actualizado o vacÃ­o si no existe
     */
    public Optional<Producto> updateProducto(Long id, Producto productoDetails) {
        log.debug("Actualizando producto con ID: {}", id);
        
        return productoRepository.findById(id)
            .map(productoExistente -> {
                // Actualizar campos bÃ¡sicos
                productoExistente.setNombre(productoDetails.getNombre());
                productoExistente.setDescripcion(productoDetails.getDescripcion());
                
                // Actualizar estado activo si se proporciona
                if (productoDetails.isActivo() != null) {
                    productoExistente.setActivo(productoDetails.isActivo());
                }
                
                // Actualizar fecha de modificaciÃ³n
                productoExistente.setActualizadoEn(LocalDateTime.now());
                
                // Actualizar relaciones con entidades si se proporcionan
                if (productoDetails.getCategoria() != null) {
                    actualizarCategoria(productoExistente, productoDetails.getCategoria());
                }
                
                if (productoDetails.getDisenoPersonalizado() != null) {
                    actualizarDisenoPersonalizado(productoExistente, productoDetails.getDisenoPersonalizado());
                } else {
                    productoExistente.setDisenoPersonalizado(null);
                }
                
                // Guardar y retornar producto actualizado
                return productoRepository.save(productoExistente);
            });
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto a eliminar
     * @return true si se eliminÃ³ correctamente, false si no existÃ­a
     */
    /**
     * Elimina un producto si no está referenciado en Items de carrito u orden.
     * @param id identificador del producto.
     * @return true si se eliminó; false si no existe o está referenciado.
     */
    public boolean deleteProducto(Long id) {
        log.debug("Eliminando producto con ID: {}", id);
        
        if (!productoRepository.existsById(id)) {
            log.warn("Producto con ID {} no existe", id);
            return false;
        }

        // Verificar referencias en carrito u orden
        boolean referenciado = itemCarritoRepository.existsByProductoId(id) ||
                               itemOrdenRepository.existsByProductoId(id);
        if (referenciado) {
            log.warn("No se puede eliminar el producto {} porque está referenciado en carritos u órdenes", id);
            return false;
        }

        productoRepository.deleteById(id);
        return true;
    }
    
    /**
     * Guarda un producto existente sin validaciones adicionales.
     * Útil para actualizaciones simples como cambiar el estado activo.
     *
     * @param producto Producto a guardar
     * @return Producto guardado
     */
    public Producto saveProducto(Producto producto) {
        log.debug("Guardando producto con ID: {}", producto.getId());
        producto.setActualizadoEn(LocalDateTime.now());
        return productoRepository.save(producto);
    }
    
    /**
     * Resuelve las relaciones con otras entidades (categorÃ­a y diseÃ±o personalizado)
     * al crear un producto.
     *
     * @param producto Producto a procesar
     */
    private void resolverRelacionesEntidad(Producto producto) {
        // Resolver categorÃ­a si se proporciona
        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            categoriaRepository.findById(producto.getCategoria().getId())
                .ifPresentOrElse(
                    producto::setCategoria,
                    () -> {
                        log.warn("CategorÃ­a con ID: {} no encontrada", producto.getCategoria().getId());
                        producto.setCategoria(null);
                    }
                );
        }
        
        // Resolver diseÃ±o personalizado si se proporciona
        if (producto.getDisenoPersonalizado() != null && producto.getDisenoPersonalizado().getId() != null) {
            disenoPersonalizadoRepository.findById(producto.getDisenoPersonalizado().getId())
                .ifPresentOrElse(
                    producto::setDisenoPersonalizado,
                    () -> {
                        log.warn("DiseÃ±o personalizado con ID: {} no encontrado", 
                               producto.getDisenoPersonalizado().getId());
                        producto.setDisenoPersonalizado(null);
                    }
                );
        }
    }
    
    /**
     * Actualiza la categorÃ­a de un producto existente.
     *
     * @param producto Producto a actualizar
     * @param categoria Nueva categorÃ­a
     */
    private void actualizarCategoria(Producto producto, Categoria categoria) {
        if (categoria.getId() != null) {
            categoriaRepository.findById(categoria.getId())
                .ifPresentOrElse(
                    producto::setCategoria,
                    () -> log.warn("CategorÃ­a con ID: {} no encontrada", categoria.getId())
                );
        }
    }
    
    /**
     * Actualiza el diseÃ±o personalizado de un producto existente.
     *
     * @param producto Producto a actualizar
     * @param disenoPersonalizado Nuevo diseÃ±o personalizado
     */
    private void actualizarDisenoPersonalizado(Producto producto, DisenoPersonalizado disenoPersonalizado) {
        if (disenoPersonalizado.getId() != null) {
            disenoPersonalizadoRepository.findById(disenoPersonalizado.getId())
                .ifPresentOrElse(
                    producto::setDisenoPersonalizado,
                    () -> log.warn("DiseÃ±o personalizado con ID: {} no encontrado", disenoPersonalizado.getId())
                );
        }
    }
}
