package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Color.
 * Proporciona métodos para acceder a la tabla colores en la base de datos.
 */
@Repository
public interface ColorRepository extends JpaRepository<Color, String> {
    // Los métodos básicos de CRUD ya están incluidos por JpaRepository
}
