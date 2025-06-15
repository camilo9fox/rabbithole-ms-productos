package com.rabbithole.productos.service;

import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import java.util.List;

/**
 * Interfaz para el servicio de productos personalizados que gestiona todo el flujo
 * de creación, edición, eliminación y consulta de diseños personalizados y sus componentes.
 */
public interface ProductoPersonalizadoService {
    
    /**
     * Crea un diseño personalizado completo con todos sus ángulos y elementos asociados.
     * Gestiona la subida de imágenes a Cloudinary.
     * 
     * @param disenoDTO DTO con toda la información del diseño a crear
     * @return El diseño creado con sus IDs y URLs de Cloudinary
     */
    DisenoPersonalizadoDTO crearDisenoPersonalizado(DisenoPersonalizadoDTO disenoDTO);
    
    /**
     * Obtiene un diseño personalizado por su ID con todos sus componentes relacionados.
     * 
     * @param id ID del diseño personalizado
     * @return Diseño personalizado completo
     */
    DisenoPersonalizadoDTO obtenerDisenoPersonalizado(Long id);
    
    /**
     * Obtiene todos los diseños personalizados de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de diseños personalizados
     */
    List<DisenoPersonalizadoDTO> obtenerDisenosPorUsuario(Long usuarioId);
    
    /**
     * Actualiza un diseño personalizado existente y sus componentes.
     * Gestiona la actualización de imágenes en Cloudinary según sea necesario.
     * 
     * @param id ID del diseño a actualizar
     * @param disenoDTO DTO con la información actualizada
     * @return El diseño actualizado
     */
    DisenoPersonalizadoDTO actualizarDisenoPersonalizado(Long id, DisenoPersonalizadoDTO disenoDTO);
    
    /**
     * Elimina un diseño personalizado y todos sus componentes asociados,
     * incluyendo la eliminación de imágenes en Cloudinary.
     * 
     * @param id ID del diseño a eliminar
     */
    void eliminarDisenoPersonalizado(Long id);
}
