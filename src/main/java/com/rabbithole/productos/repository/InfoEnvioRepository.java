package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.InfoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad InfoEnvio
 */
@Repository
public interface InfoEnvioRepository extends JpaRepository<InfoEnvio, Long> {
    
    /**
     * Encuentra la información de envío por el ID de la orden
     * 
     * @param ordenId ID de la orden
     * @return Objeto InfoEnvio si existe
     */
    Optional<InfoEnvio> findByOrdenId(Long ordenId);
}
