package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
    // Los métodos básicos del CRUD son proporcionados automáticamente por JpaRepository
}
