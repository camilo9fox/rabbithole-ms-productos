package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ElementoTexto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con ElementoTexto.
 */
@Repository
public interface ElementoTextoRepository extends JpaRepository<ElementoTexto, Long> {
    
    /**
     * Encuentra elementos de texto por fuente.
     *
     * @param fuente Nombre de la fuente
     * @return Lista de elementos de texto con la fuente especificada
     */
    @Query("SELECT e FROM ElementoTexto e WHERE e.fuente = :fuente")
    List<ElementoTexto> findByFuente(@Param("fuente") String fuente);
    
    /**
     * Encuentra elementos de texto por tamaño de fuente.
     *
     * @param tamanoFuente Tamaño de la fuente
     * @return Lista de elementos de texto con el tamaño de fuente especificado
     */
    @Query("SELECT e FROM ElementoTexto e WHERE e.tamanoFuente = :tamanoFuente")
    List<ElementoTexto> findByTamanoFuente(@Param("tamanoFuente") Integer tamanoFuente);
    
    /**
     * Busca elementos de texto por contenido (texto) con paginación.
     *
     * @param texto Texto a buscar (parcial)
     * @param pageable Información de paginación
     * @return Página de elementos de texto que contienen el texto especificado
     */
    @Query(value = "SELECT e.* FROM ELEMENTOS_TEXTO e WHERE DBMS_LOB.INSTR(e.CONTENIDO, :texto) > 0", 
           countQuery = "SELECT COUNT(*) FROM ELEMENTOS_TEXTO e WHERE DBMS_LOB.INSTR(e.CONTENIDO, :texto) > 0",
           nativeQuery = true)
    Page<ElementoTexto> findByTextoContainingIgnoreCase(@Param("texto") String texto, Pageable pageable);
    
    /**
     * Busca elementos de texto por su ID.
     *
     * @param elementoId ID del elemento texto
     * @return Lista de elementos texto que coinciden
     */
    @Query("SELECT e FROM ElementoTexto e WHERE e.id = :elementoId")
    List<ElementoTexto> findByElementoId(@Param("elementoId") Long elementoId);
    
    /**
     * Busca elementos de texto por su ID con paginación.
     *
     * @param elementoId ID del elemento texto
     * @param pageable Información de paginación
     * @return Página de elementos texto que coinciden
     */
    @Query("SELECT e FROM ElementoTexto e WHERE e.id = :elementoId")
    Page<ElementoTexto> findByElementoId(@Param("elementoId") Long elementoId, Pageable pageable);
    
    /**
     * Busca un elemento de texto por su ID y elementoId.
     * Este método mantiene la compatibilidad con las antiguas consultas.
     *
     * @param id ID del elemento
     * @param elementoId ID del elemento (redundante pero se mantiene por compatibilidad)
     * @return Optional con el elemento texto si existe
     */
    @Query("SELECT e FROM ElementoTexto e WHERE e.id = :id AND e.id = :elementoId")
    Optional<ElementoTexto> findByIdAndElementoId(@Param("id") Long id, @Param("elementoId") Long elementoId);
}
