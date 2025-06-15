package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ElementoDiseno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio base para operaciones comunes de elementos de diseño.
 * Esta es una interfaz base que no se instancia directamente.
 * 
 * Se han eliminado completamente las referencias a angulo ya que el campo fue eliminado del modelo ElementoDiseno.
 * Los métodos mantienen las firmas originales por compatibilidad pero las consultas JPQL han sido modificadas.
 * 
 * NOTA: No se puede filtrar por el campo 'activo' ya que está marcado como @Transient y no existe en la base de datos.
 */
@NoRepositoryBean
public interface ElementoDisenoRepository<T extends ElementoDiseno> extends JpaRepository<T, Long> {
    
    /**
     * Busca elementos ignorando el parámetro anguloId.
     * Este método se mantiene por compatibilidad pero ya no filtra por ángulo.
     *
     * @param anguloId ID del ángulo (ignorado)
     * @return Lista de todos los elementos
     */
    @Query("SELECT e FROM #{#entityName} e")
    List<T> findByAnguloId(@Param("anguloId") Long anguloId);
    
    /**
     * Busca elementos con paginación ignorando el parámetro anguloId.
     * Este método se mantiene por compatibilidad pero ya no filtra por ángulo.
     *
     * @param anguloId ID del ángulo (ignorado)
     * @param pageable Información de paginación
     * @return Página de elementos
     */
    @Query("SELECT e FROM #{#entityName} e")
    Page<T> findByAnguloId(@Param("anguloId") Long anguloId, Pageable pageable);
    
    /**
     * Busca un elemento por su ID ignorando el parámetro anguloId.
     * Este método se mantiene por compatibilidad pero ya no filtra por ángulo.
     *
     * @param id ID del elemento
     * @param anguloId ID del ángulo (ignorado)
     * @return Elemento si existe
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdAndAnguloId(@Param("id") Long id, @Param("anguloId") Long anguloId);
    
    /**
     * Busca un elemento por su ID con una consulta personalizada.
     *
     * @param elementoId ID del elemento
     * @return Elemento si existe
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :elementoId")
    Optional<T> findByElementoId(@Param("elementoId") Long elementoId);
}
