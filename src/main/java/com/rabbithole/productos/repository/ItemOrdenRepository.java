package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
    /**
     * Devuelve el primer ItemOrden que contenga el diseño personalizado indicado.
     */
    java.util.Optional<ItemOrden> findFirstByDisenoPersonalizadoId(Long disenoPersonalizadoId);

    /**
     * Indica si existe al menos un ItemOrden que referencie al producto dado.
     */
    boolean existsByProductoId(Long productoId);

    // Los métodos básicos del CRUD son proporcionados automáticamente por JpaRepository
}
