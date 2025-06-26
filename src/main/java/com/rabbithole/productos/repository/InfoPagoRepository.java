package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.InfoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad InfoPago
 */
@Repository
public interface InfoPagoRepository extends JpaRepository<InfoPago, Long> {
    
    /**
     * Encuentra la información de pago por el ID de la orden
     * 
     * @param ordenId ID de la orden
     * @return Objeto InfoPago si existe
     */
    Optional<InfoPago> findByOrdenId(Long ordenId);
}
