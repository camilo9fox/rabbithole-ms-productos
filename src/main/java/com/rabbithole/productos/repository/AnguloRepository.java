package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.AnguloDiseno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad AnguloDiseno
 */
@Repository
public interface AnguloRepository extends JpaRepository<AnguloDiseno, Long> {
    
    /**
     * Encuentra todos los ángulos asociados a un diseño personalizado
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @return Lista de ángulos
     */
    List<AnguloDiseno> findByDisenoPersonalizadoId(Long disenoPersonalizadoId);
    
    /**
     * Elimina todos los ángulos asociados a un diseño personalizado
     * @param disenoPersonalizadoId ID del diseño personalizado
     */
    void deleteByDisenoPersonalizadoId(Long disenoPersonalizadoId);
}
