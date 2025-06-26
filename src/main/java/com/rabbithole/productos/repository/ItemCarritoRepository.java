package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    // Los métodos básicos del CRUD son proporcionados automáticamente por JpaRepository
}
