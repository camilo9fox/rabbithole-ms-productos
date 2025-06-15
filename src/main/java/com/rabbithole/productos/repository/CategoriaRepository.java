package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Categoria.
 * Proporciona operaciones CRUD básicas y consultas personalizadas.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    /**
     * Este método ha sido comentado porque el campo 'activo' ya no existe en la entidad Categoria.
     */
    // List<Categoria> findByActivo(Boolean activo);
    
    /**
     * Encuentra categorías que contienen el nombre especificado.
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de categorías que coinciden con el criterio
     */
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
}
