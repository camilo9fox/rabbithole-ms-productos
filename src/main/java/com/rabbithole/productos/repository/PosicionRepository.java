package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Posicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Posicion.
 * Permite la gestión de las operaciones CRUD para las posiciones.
 */
@Repository
public interface PosicionRepository extends JpaRepository<Posicion, Long> {
    // Métodos básicos heredados de JpaRepository
}
