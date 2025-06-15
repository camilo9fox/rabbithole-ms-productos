package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repositorio para la entidad Producto.
 * Proporciona operaciones CRUD básicas y consultas personalizadas.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    /**
     * Encuentra productos por estado activo con paginación.
     *
     * @param activo Estado de activación del producto
     * @param pageable Información de paginación
     * @return Página de productos activos/inactivos
     */
    Page<Producto> findByActivo(Boolean activo, Pageable pageable);
    
    /**
     * Encuentra productos por categoría.
     *
     * @param categoriaId ID de la categoría
     * @param pageable Información de paginación
     * @return Página de productos de la categoría especificada
     */
    Page<Producto> findByCategoriaIdAndActivo(String categoriaId, Boolean activo, Pageable pageable);
    
    /**
     * Busca productos por nombre o descripción.
     *
     * @param texto Texto a buscar
     * @param pageable Información de paginación
     * @return Página de productos que coinciden con la búsqueda
     */
    @Query(
        value = "SELECT p.* FROM PRODUCTOS p WHERE (LOWER(p.NOMBRE) LIKE LOWER(CONCAT('%', :texto, '%')) OR DBMS_LOB.INSTR(p.DESCRIPCION, :texto) > 0) AND p.ACTIVO = 1",
        countQuery = "SELECT COUNT(*) FROM PRODUCTOS p WHERE (LOWER(p.NOMBRE) LIKE LOWER(CONCAT('%', :texto, '%')) OR DBMS_LOB.INSTR(p.DESCRIPCION, :texto) > 0) AND p.ACTIVO = 1",
        nativeQuery = true
    )
    Page<Producto> buscarPorTexto(@Param("texto") String texto, Pageable pageable);
    
    /**
     * Encuentra productos personalizables.
     *
     * @param personalizable Flag de personalización
     * @param pageable Información de paginación
     * @return Página de productos personalizables/no personalizables
     */
    Page<Producto> findByPersonalizableAndActivo(Boolean personalizable, Boolean activo, Pageable pageable);
    
    /**
     * Encuentra productos por rango de precios.
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param pageable Información de paginación
     * @return Página de productos en el rango de precios
     */
    Page<Producto> findByPrecioBaseBetweenAndActivo(BigDecimal precioMin, BigDecimal precioMax, Boolean activo, Pageable pageable);
    
    /**
     * Encuentra productos que tienen disponible una talla específica.
     * Debido a que tallasDisponibles está marcado como @Transient, usamos una consulta nativa
     * para consultar directamente la tabla de unión PRODUCTO_TALLA.
     *
     * @param tallaId ID de la talla
     * @param pageable Información de paginación
     * @return Página de productos con la talla especificada
     */
    @Query(value = "SELECT p.* FROM PRODUCTOS p JOIN PRODUCTO_TALLA pt ON p.ID = pt.PRODUCTO_ID WHERE pt.TALLA_ID = :tallaId AND p.ACTIVO = 1",
           countQuery = "SELECT COUNT(*) FROM PRODUCTOS p JOIN PRODUCTO_TALLA pt ON p.ID = pt.PRODUCTO_ID WHERE pt.TALLA_ID = :tallaId AND p.ACTIVO = 1",
           nativeQuery = true)
    Page<Producto> findByTallaDisponibleAndActivo(@Param("tallaId") String tallaId, Pageable pageable);
    
    /**
     * Encuentra productos que tienen disponible un color específico.
     * Debido a que coloresDisponibles está marcado como @Transient, usamos una consulta nativa
     * para consultar directamente la tabla de unión PRODUCTO_COLOR.
     *
     * @param colorId ID del color
     * @param pageable Información de paginación
     * @return Página de productos con el color especificado
     */
    @Query(value = "SELECT p.* FROM PRODUCTOS p JOIN PRODUCTO_COLOR pc ON p.ID = pc.PRODUCTO_ID WHERE pc.COLOR_ID = :colorId AND p.ACTIVO = 1",
           countQuery = "SELECT COUNT(*) FROM PRODUCTOS p JOIN PRODUCTO_COLOR pc ON p.ID = pc.PRODUCTO_ID WHERE pc.COLOR_ID = :colorId AND p.ACTIVO = 1",
           nativeQuery = true)
    Page<Producto> findByColorDisponibleAndActivo(@Param("colorId") String colorId, Pageable pageable);
    
    /**
     * Encuentra productos que tienen disponible un diseño específico.
     *
     * @param disenoId ID del diseño
     * @param pageable Información de paginación
     * @return Página de productos con el diseño especificado
     */
    /**
     * Encuentra productos por diseño personalizado.
     * Debido a que disenoPersonalizado está marcado como @Transient, usamos una consulta nativa
     * para consultar la relación con diseños personalizados desde la tabla PRODUCTOS.
     *
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @param pageable Información de paginación
     * @return Página de productos con el diseño personalizado especificado
     */
    @Query(value = "SELECT p.* FROM PRODUCTOS p WHERE p.DISENO_PERSONALIZADO_ID = :disenoPersonalizadoId AND p.ACTIVO = 1",
           countQuery = "SELECT COUNT(*) FROM PRODUCTOS p WHERE p.DISENO_PERSONALIZADO_ID = :disenoPersonalizadoId AND p.ACTIVO = 1",
           nativeQuery = true)
    Page<Producto> findByDisenoPersonalizadoIdAndActivo(@Param("disenoPersonalizadoId") Long disenoPersonalizadoId, Pageable pageable);
}
