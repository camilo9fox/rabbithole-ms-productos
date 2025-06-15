package com.rabbithole.productos.service;

import com.rabbithole.productos.model.Color;
import com.rabbithole.productos.model.DisenoPersonalizado;
import com.rabbithole.productos.model.Producto;
import com.rabbithole.productos.model.Talla;
import com.rabbithole.productos.repository.ColorRepository;
import com.rabbithole.productos.repository.DisenoPersonalizadoRepository;
import com.rabbithole.productos.repository.ProductoRepository;
import com.rabbithole.productos.repository.TallaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio para manejar la lógica de negocio relacionada con los productos.
 */
@Service
@Slf4j
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final DisenoPersonalizadoRepository disenoPersonalizadoRepository;
    private final ColorRepository colorRepository;
    private final TallaRepository tallaRepository;

    @Autowired
    public ProductoService(
            ProductoRepository productoRepository,
            DisenoPersonalizadoRepository disenoPersonalizadoRepository,
            ColorRepository colorRepository,
            TallaRepository tallaRepository) {
        this.productoRepository = productoRepository;
        this.disenoPersonalizadoRepository = disenoPersonalizadoRepository;
        this.colorRepository = colorRepository;
        this.tallaRepository = tallaRepository;
    }

    /**
     * Obtiene todos los productos con paginación.
     *
     * @param pageable Información de paginación
     * @return Página de productos
     */
    @Transactional(readOnly = true)
    public Page<Producto> getAllProductos(Pageable pageable) {
        log.debug("Obteniendo todos los productos con paginación");
        return productoRepository.findAll(pageable);
    }

    /**
     * Obtiene todos los productos activos con paginación.
     *
     * @param pageable Información de paginación
     * @return Página de productos activos
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosActivos(Pageable pageable) {
        log.debug("Obteniendo productos activos con paginación");
        return productoRepository.findByActivo(true, pageable);
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return Producto encontrado o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<Producto> getProductoById(Long id) {
        log.debug("Buscando producto con ID: {}", id);
        return productoRepository.findById(id);
    }

    /**
     * Busca productos por texto en nombre o descripción.
     *
     * @param texto Texto a buscar
     * @param pageable Información de paginación
     * @return Página de productos que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public Page<Producto> buscarProductos(String texto, Pageable pageable) {
        log.debug("Buscando productos que contengan: {}", texto);
        return productoRepository.buscarPorTexto(texto, pageable);
    }

    /**
     * Obtiene productos por categoría.
     *
     * @param categoriaId ID de la categoría
     * @param pageable Información de paginación
     * @return Página de productos de la categoría especificada
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosPorCategoria(String categoriaId, Pageable pageable) {
        log.debug("Obteniendo productos de la categoría con ID: {}", categoriaId);
        return productoRepository.findByCategoriaIdAndActivo(categoriaId, true, pageable);
    }

    /**
     * Obtiene productos por rango de precios.
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param pageable Información de paginación
     * @return Página de productos en el rango de precios
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        log.debug("Obteniendo productos con precio entre {} y {}", precioMin, precioMax);
        return productoRepository.findByPrecioBaseBetweenAndActivo(precioMin, precioMax, true, pageable);
    }

    /**
     * Obtiene productos personalizables.
     *
     * @param pageable Información de paginación
     * @return Página de productos personalizables
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosPersonalizables(Pageable pageable) {
        log.debug("Obteniendo productos personalizables");
        return productoRepository.findByPersonalizableAndActivo(true, true, pageable);
    }

    /**
     * Obtiene productos por color disponible.
     *
     * @param colorId ID del color
     * @param pageable Información de paginación
     * @return Página de productos con el color especificado
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosPorColor(String colorId, Pageable pageable) {
        log.debug("Obteniendo productos con color ID: {}", colorId);
        return productoRepository.findByColorDisponibleAndActivo(colorId, pageable);
    }

    /**
     * Obtiene productos por talla disponible.
     *
     * @param tallaId ID de la talla
     * @param pageable Información de paginación
     * @return Página de productos con la talla especificada
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosPorTalla(String tallaId, Pageable pageable) {
        log.debug("Obteniendo productos con talla ID: {}", tallaId);
        return productoRepository.findByTallaDisponibleAndActivo(tallaId, pageable);
    }

    /**
     * Guarda un nuevo producto o actualiza uno existente.
     *
     * @param producto Producto a guardar/actualizar
     * @return Producto guardado/actualizado
     */
    public Producto saveProducto(Producto producto) {
        boolean esNuevo = producto.getId() == null;
        log.debug("{} producto: {}", esNuevo ? "Creando" : "Actualizando", producto.getNombre());
        return productoRepository.save(producto);
    }

    /**
     * Activa o desactiva un producto.
     *
     * @param id ID del producto
     * @param activo Estado de activación
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> cambiarEstadoProducto(Long id, boolean activo) {
        log.debug("{} producto con ID: {}", activo ? "Activando" : "Desactivando", id);
        Optional<Producto> productoOpt = productoRepository.findById(id);
        
        return productoOpt.map(producto -> {
            producto.setActivo(activo);
            return productoRepository.save(producto);
        });
    }
    
    /**
     * Añade un color a la lista de colores disponibles de un producto.
     *
     * @param productoId ID del producto
     * @param colorId ID del color
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> agregarColorAProducto(Long productoId, String colorId) {
        log.debug("Agregando color ID: {} al producto ID: {}", colorId, productoId);
        
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        Optional<Color> colorOpt = colorRepository.findById(colorId);
        
        if (productoOpt.isPresent() && colorOpt.isPresent()) {
            Producto producto = productoOpt.get();
            if (producto.getColoresDisponibles() == null) {
                producto.setColoresDisponibles(new HashSet<>());
            }
            producto.getColoresDisponibles().add(colorOpt.get());
            return Optional.of(productoRepository.save(producto));
        }
        
        return Optional.empty();
    }
    
    /**
     * Elimina un color de la lista de colores disponibles de un producto.
     *
     * @param productoId ID del producto
     * @param colorId ID del color
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> eliminarColorDeProducto(Long productoId, String colorId) {
        log.debug("Eliminando color ID: {} del producto ID: {}", colorId, productoId);
        
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        Optional<Color> colorOpt = colorRepository.findById(colorId);
        
        if (productoOpt.isPresent() && colorOpt.isPresent()) {
            Producto producto = productoOpt.get();
            Set<Color> colores = producto.getColoresDisponibles();
            if (colores != null) {
                colores.remove(colorOpt.get());
                return Optional.of(productoRepository.save(producto));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Añade una talla a la lista de tallas disponibles de un producto.
     *
     * @param productoId ID del producto
     * @param tallaId ID de la talla
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> agregarTallaAProducto(Long productoId, String tallaId) {
        log.debug("Agregando talla ID: {} al producto ID: {}", tallaId, productoId);
        
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        Optional<Talla> tallaOpt = tallaRepository.findById(tallaId);
        
        if (productoOpt.isPresent() && tallaOpt.isPresent()) {
            Producto producto = productoOpt.get();
            if (producto.getTallasDisponibles() == null) {
                producto.setTallasDisponibles(new HashSet<>());
            }
            producto.getTallasDisponibles().add(tallaOpt.get());
            return Optional.of(productoRepository.save(producto));
        }
        
        return Optional.empty();
    }
    
    /**
     * Elimina una talla de la lista de tallas disponibles de un producto.
     *
     * @param productoId ID del producto
     * @param tallaId ID de la talla
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> eliminarTallaDeProducto(Long productoId, String tallaId) {
        log.debug("Eliminando talla ID: {} del producto ID: {}", tallaId, productoId);
        
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        Optional<Talla> tallaOpt = tallaRepository.findById(tallaId);
        
        if (productoOpt.isPresent() && tallaOpt.isPresent()) {
            Producto producto = productoOpt.get();
            Set<Talla> tallas = producto.getTallasDisponibles();
            if (tallas != null) {
                tallas.remove(tallaOpt.get());
                return Optional.of(productoRepository.save(producto));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Actualiza el stock disponible de un producto.
     *
     * @param productoId ID del producto
     * @param nuevoStock Nuevo valor de stock
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> actualizarStockProducto(Long productoId, Integer nuevoStock) {
        log.debug("Actualizando stock del producto ID: {} a {}", productoId, nuevoStock);
        
        if (nuevoStock < 0) {
            log.error("No se puede establecer un stock negativo");
            return Optional.empty();
        }
        
        // Esta funcionalidad ya no aplica con el nuevo modelo pero la mantenemos por compatibilidad
        return productoRepository.findById(productoId);
    }
    
    /**
     * Actualiza el diseño personalizado de un producto.
     *
     * @param productoId ID del producto
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @return Producto actualizado o vacío si no existe
     */
    public Optional<Producto> asignarDisenoPersonalizado(Long productoId, Long disenoPersonalizadoId) {
        log.debug("Asignando diseño personalizado ID: {} al producto ID: {}", disenoPersonalizadoId, productoId);
        
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        Optional<DisenoPersonalizado> disenoOpt = disenoPersonalizadoRepository.findById(disenoPersonalizadoId);
        
        if (productoOpt.isPresent() && disenoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            producto.setDisenoPersonalizado(disenoOpt.get());
            return Optional.of(productoRepository.save(producto));
        }
        
        return Optional.empty();
    }
    
    /**
     * Método de compatibilidad que no realiza operación ya que en el nuevo modelo
     * un producto solo puede tener un diseño personalizado.
     *
     * @param productoId ID del producto
     * @param disenoId ID del diseño
     * @return Producto sin cambios o vacío si no existe
     */
    public Optional<Producto> eliminarDisenoDeProducto(Long productoId, String disenoId) {
        log.debug("Método eliminarDisenoDeProducto no aplicable en el nuevo modelo. Producto ID: {}, Diseño ID: {}", productoId, disenoId);
        return productoRepository.findById(productoId);
    }
    
    /**
     * Obtiene productos que tienen asignado un diseño personalizado específico.
     *
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @param pageable Información de paginación
     * @return Página de productos con el diseño personalizado especificado
     */
    @Transactional(readOnly = true)
    public Page<Producto> getProductosPorDisenoPersonalizado(Long disenoPersonalizadoId, Pageable pageable) {
        log.debug("Obteniendo productos con diseño personalizado ID: {}", disenoPersonalizadoId);
        return productoRepository.findByDisenoPersonalizadoIdAndActivo(disenoPersonalizadoId, pageable);
    }
}
