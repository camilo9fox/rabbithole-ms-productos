package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ElementoImagen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con ElementoImagen.
 */
@Repository
public interface ElementoImagenRepository extends JpaRepository<ElementoImagen, Long> {
    
    /**
     * Encuentra elementos de imagen por tipo de imagen.
     *
     * @param tipoImagen Tipo de imagen (ej: PNG, JPG)
     * @return Lista de elementos de imagen del tipo especificado
     */
    @Query("SELECT e FROM ElementoImagen e JOIN e.cloudinaryResource cr WHERE cr.tipoImagen = :tipoImagen")
    List<ElementoImagen> findByTipoImagen(@Param("tipoImagen") String tipoImagen);
    
    /**
     * Encuentra elementos de imagen por nombre de archivo con paginación.
     *
     * @param nombreArchivo Nombre de archivo (parcial)
     * @param pageable Información de paginación
     * @return Página de elementos de imagen con el nombre de archivo especificado
     */
    @Query("SELECT e FROM ElementoImagen e JOIN e.cloudinaryResource cr WHERE LOWER(cr.nombreArchivo) LIKE LOWER(CONCAT('%', :nombreArchivo, '%'))")
    Page<ElementoImagen> findByNombreArchivoContainingIgnoreCase(@Param("nombreArchivo") String nombreArchivo, Pageable pageable);
    
    /**
     * Encuentra elementos de imagen con un tamaño de archivo mayor que el especificado.
     *
     * @param tamanoArchivo Tamaño de archivo mínimo
     * @return Lista de elementos de imagen con tamaño mayor al especificado
     */
    @Query("SELECT e FROM ElementoImagen e JOIN e.cloudinaryResource cr WHERE cr.tamanoArchivo > :tamanoArchivo")
    List<ElementoImagen> findByTamanoArchivoGreaterThan(@Param("tamanoArchivo") Long tamanoArchivo);
    
    /**
     * Encuentra elementos de imagen con un tamaño de archivo menor que el especificado.
     *
     * @param tamanoArchivo Tamaño de archivo máximo
     * @return Lista de elementos de imagen con tamaño menor al especificado
     */
    @Query("SELECT e FROM ElementoImagen e JOIN e.cloudinaryResource cr WHERE cr.tamanoArchivo < :tamanoArchivo")
    List<ElementoImagen> findByTamanoArchivoLessThan(@Param("tamanoArchivo") Long tamanoArchivo);
    
    /**
     * Busca elementos de imagen por ID usando una consulta JPQL personalizada.
     * Este método reemplaza la antigua relación con anguloDiseno que ya no existe.
     *
     * @param elementoId ID del elemento imagen
     * @return Lista con los elementos imagen que coinciden
     */
    @Query("SELECT e FROM ElementoImagen e WHERE e.id = :elementoId")
    List<ElementoImagen> findByElementoId(@Param("elementoId") Long elementoId);
    
    /**
     * Busca elementos de imagen por ID con paginación.
     * Este método reemplaza la antigua relación con anguloDiseno que ya no existe.
     *
     * @param elementoId ID del elemento imagen
     * @param pageable Información de paginación
     * @return Página de elementos de imagen
     */
    @Query("SELECT e FROM ElementoImagen e WHERE e.id = :elementoId")
    Page<ElementoImagen> findByElementoId(@Param("elementoId") Long elementoId, Pageable pageable);
    
    /**
     * Busca un elemento por su ID y elementoId.
     * Este método reemplaza la antigua consulta que utilizaba anguloId.
     *
     * @param id ID del elemento
     * @param elementoId ID del elemento (en este caso es redundante pero se mantiene por compatibilidad)
     * @return Optional con el elemento imagen si existe
     */
    @Query("SELECT e FROM ElementoImagen e WHERE e.id = :id AND e.id = :elementoId")
    Optional<ElementoImagen> findByIdAndElementoId(@Param("id") Long id, @Param("elementoId") Long elementoId);
}
