package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.TipoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad TipoItem
 */
@Repository
public interface TipoItemRepository extends JpaRepository<TipoItem, String> {
    // Métodos personalizados pueden ser agregados aquí
}
