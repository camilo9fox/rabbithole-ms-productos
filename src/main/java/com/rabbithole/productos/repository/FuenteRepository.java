package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Fuente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Fuente
 */
@Repository
public interface FuenteRepository extends JpaRepository<Fuente, Long> {
    
}
