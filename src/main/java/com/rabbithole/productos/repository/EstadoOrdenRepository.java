package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.EstadoOrden;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad EstadoOrden
 */
@Repository
public interface EstadoOrdenRepository extends JpaRepository<EstadoOrden, Long> {
    /**
     * Encuentra un estado de orden por su nombre.
     * 
     * @param nombre Nombre del estado de orden
     * @return El estado de orden encontrado
     */
    EstadoOrden findByNombre(String nombre);
}
