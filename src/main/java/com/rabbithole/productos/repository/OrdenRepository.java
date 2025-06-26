package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Orden;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Orden
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    /**
     * Encuentra órdenes por el ID del usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de órdenes
     */
    List<Orden> findByUsuarioId(Long usuarioId);
    
    /**
     * Encuentra órdenes por el ID del usuario ordenadas por fecha de creación descendente.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de órdenes
     */
    List<Orden> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
}
