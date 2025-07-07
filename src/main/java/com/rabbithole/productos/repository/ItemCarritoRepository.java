package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    /**
     * Indica si existe al menos un ItemCarrito que referencie al producto dado.
     * @param productoId identificador del producto.
     * @return true si existe referencia, false en caso contrario.
     */
    boolean existsByProductoId(Long productoId);

    // Los métodos básicos del CRUD son proporcionados automáticamente por JpaRepository
}
