package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.AnguloDiseno;
import com.rabbithole.productos.model.DisenoPersonalizado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de base de datos relacionadas con AnguloDiseno.
 */
@Repository
public interface AnguloDisenoRepository extends JpaRepository<AnguloDiseno, Long> {
    
    /**
     * Encuentra todos los ángulos de un diseño personalizado.
     *
     * @param disenoPersonalizado Diseño personalizado
     * @return Lista de ángulos del diseño
     */
    List<AnguloDiseno> findByDisenoPersonalizado(DisenoPersonalizado disenoPersonalizado);
    
    /**
     * Encuentra todos los ángulos de un diseño personalizado por su ID.
     *
     * @param disenoId ID del diseño personalizado
     * @return Lista de ángulos del diseño
     */
    List<AnguloDiseno> findByDisenoPersonalizadoId(Long disenoId);
    
    /**
     * Encuentra todos los ángulos de un diseño personalizado de forma paginada.
     *
     * @param disenoId ID del diseño personalizado
     * @param pageable Información de paginación
     * @return Página de ángulos del diseño
     */
    Page<AnguloDiseno> findByDisenoPersonalizadoId(Long disenoId, Pageable pageable);
    
    
    /**
     * Encuentra ángulos de un diseño personalizado por tipo de ángulo.
     *
     * @param disenoId ID del diseño personalizado
     * @param tipoAnguloId ID del tipo de ángulo
     * @return Lista de ángulos del diseño con el tipo especificado
     */
    List<AnguloDiseno> findByDisenoPersonalizadoIdAndTipoAnguloId(Long disenoId, Long tipoAnguloId);
}
