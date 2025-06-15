package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.EstadoDiseno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con EstadoDiseno.
 */
@Repository
public interface EstadoDisenoRepository extends JpaRepository<EstadoDiseno, Long> {
    
    /**
     * Encuentra un estado de diseño por su código.
     *
     * @param codigo Código del estado
     * @return Estado de diseño encontrado o vacío si no existe
     */
    Optional<EstadoDiseno> findByCodigo(String codigo);
    
    /**
     * Encuentra un estado de diseño por su nombre.
     *
     * @param nombre Nombre del estado
     * @return Estado de diseño encontrado o vacío si no existe
     */
    Optional<EstadoDiseno> findByNombre(String nombre);
}
