package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Talla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Talla.
 * Proporciona operaciones CRUD básicas y consultas personalizadas.
 */
@Repository
public interface TallaRepository extends JpaRepository<Talla, String> {
    
    
    /**
     * Encuentra tallas que contienen el nombre especificado.
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de tallas que coinciden con el criterio
     */
    List<Talla> findByNombreContainingIgnoreCase(String nombre);
}
