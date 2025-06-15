package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.TipoAngulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con TipoAngulo.
 */
@Repository
public interface TipoAnguloRepository extends JpaRepository<TipoAngulo, Long> {
    
    /**
     * Encuentra un tipo de ángulo por su código.
     *
     * @param codigo Código del tipo de ángulo
     * @return Tipo de ángulo encontrado o vacío si no existe
     */
    Optional<TipoAngulo> findByCodigo(String codigo);
    
    /**
     * Encuentra un tipo de ángulo por su nombre.
     *
     * @param nombre Nombre del tipo de ángulo
     * @return Tipo de ángulo encontrado o vacío si no existe
     */
    Optional<TipoAngulo> findByNombre(String nombre);
}
