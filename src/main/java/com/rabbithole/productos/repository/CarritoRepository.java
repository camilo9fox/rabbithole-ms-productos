package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a los datos de la entidad Carrito.
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Busca todos los carritos pertenecientes a un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de carritos del usuario
     */
    List<Carrito> findByUsuarioId(Long usuarioId);
    
    /**
     * Busca el carrito activo (más reciente) de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return El carrito activo si existe
     */
    @Query("SELECT c FROM Carrito c WHERE c.usuario.id = :usuarioId ORDER BY c.creadoEn DESC")
    List<Carrito> findByUsuarioIdOrderByCreadoEnDesc(@Param("usuarioId") Long usuarioId);
    
    /**
     * Busca carritos que contengan un producto específico.
     * 
     * @param productoId ID del producto
     * @return Lista de carritos que contienen el producto
     */
    @Query("SELECT DISTINCT c FROM Carrito c JOIN c.items i WHERE i.producto.id = :productoId")
    List<Carrito> findByItemsProductoId(@Param("productoId") Long productoId);
    
    /**
     * Busca carritos que contengan un diseño personalizado específico.
     * 
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @return Lista de carritos que contienen el diseño personalizado
     */
    @Query("SELECT DISTINCT c FROM Carrito c JOIN c.items i WHERE i.disenoPersonalizado.id = :disenoPersonalizadoId")
    List<Carrito> findByItemsDisenoPersonalizadoId(@Param("disenoPersonalizadoId") Long disenoPersonalizadoId);
}
