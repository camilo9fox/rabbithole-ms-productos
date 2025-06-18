package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.DisenoPersonalizado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



/**
 * Repositorio para operaciones de base de datos relacionadas con DisenoPersonalizado.
 */
@Repository
public interface DisenoPersonalizadoRepository extends JpaRepository<DisenoPersonalizado, Long> {
    
    /**
     * Encuentra diseños por usuario.
     *
     * @param usuarioId ID del usuario
     * @param pageable Información de paginación
     * @return Página de diseños del usuario
     */
    Page<DisenoPersonalizado> findByUsuarioId(Long usuarioId, Pageable pageable);
    

    
    /**
     * Encuentra diseños por estado de aprobación.
     *
     * @param estadoId ID del estado del diseño
     * @param pageable Información de paginación
     * @return Página de diseños con el estado especificado
     */
    Page<DisenoPersonalizado> findByEstadoId(Long estadoId, Pageable pageable);
    
    /**
     * Cuenta el número de diseños por usuario.
     *
     * @param usuarioId ID del usuario
     * @return Número de diseños del usuario
     */
    long countByUsuarioId(Long usuarioId);
    
    /**
     * Encuentra diseños por detalle conteniendo el texto de búsqueda.
     * Utiliza LIKE case-insensitive nativo que es compatible con campos CLOB.
     *
     * @param texto Texto de búsqueda
     * @param pageable Información de paginación
     * @return Página de diseños que coinciden con la búsqueda
     */
    @Query(value = "SELECT * FROM DISENOS_PERSONALIZADOS d WHERE LOWER(d.DETALLE) LIKE LOWER(CONCAT('%', :texto, '%'))", 
           nativeQuery = true)
    Page<DisenoPersonalizado> buscarPorTexto(@Param("texto") String texto, Pageable pageable);
}
