package com.rabbithole.productos.service;

import com.rabbithole.productos.exception.ImageProcessingException;
import com.rabbithole.productos.model.ElementoImagen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para operaciones relacionadas con elementos de imagen.
 */
public interface ElementoImagenService {
    
    /**
     * Guarda un elemento de imagen.
     *
     * @param elementoImagen el elemento a guardar
     * @return el elemento guardado
     */
    ElementoImagen save(ElementoImagen elementoImagen);
    
    /**
     * Busca un elemento de imagen por su ID.
     *
     * @param id el ID del elemento
     * @return un Optional con el elemento encontrado o vacío si no existe
     */
    Optional<ElementoImagen> findById(Long id);
    
    /**
     * Elimina un elemento de imagen por su ID.
     *
     * @param id el ID del elemento a eliminar
     */
    void deleteById(Long id);
    
    /**
     * Obtiene todos los elementos de imagen.
     *
     * @return lista de elementos
     */
    List<ElementoImagen> findAll();
    
    /**
     * Obtiene todos los elementos de imagen con paginación.
     *
     * @param pageable configuración de paginación
     * @return página de elementos
     */
    Page<ElementoImagen> findAll(Pageable pageable);
    
    /**
     * Busca elementos de imagen por ID de elemento.
     *
     * @param elementoId ID del elemento
     * @return lista de elementos encontrados
     */
    List<ElementoImagen> findByElementoId(Long elementoId);
    
    /**
     * Busca elementos de imagen por ID de elemento con paginación.
     *
     * @param elementoId ID del elemento
     * @param pageable configuración de paginación
     * @return página de elementos
     */
    Page<ElementoImagen> findByElementoId(Long elementoId, Pageable pageable);
    
    /**
     * Obtiene un elemento de imagen por su ID y el ID del elemento.
     *
     * @param id ID del elemento de imagen
     * @param elementoId ID del elemento
     * @return Optional con el elemento encontrado o vacío
     */
    Optional<ElementoImagen> getElementByIdAndElementoId(Long id, Long elementoId);
    
    /**
     * Crea un nuevo elemento de imagen asociado a un elemento.
     *
     * @param elementoId ID del elemento
     * @param elementoImagen datos del elemento de imagen
     * @return Optional con el elemento creado o vacío
     */
    Optional<ElementoImagen> createElement(Long elementoId, ElementoImagen elementoImagen);
    
    /**
     * Actualiza un elemento de imagen.
     *
     * @param id ID del elemento de imagen
     * @param elementoImagen datos actualizados
     * @return Optional con el elemento actualizado o vacío
     */
    Optional<ElementoImagen> updateElement(Long id, ElementoImagen elementoImagen);
    
    /**
     * Busca elementos de imagen por tipo de imagen.
     * 
     * @param anguloId ID del ángulo (para compatibilidad)
     * @param tipoImagen tipo de imagen a buscar
     * @return lista de elementos
     */
    List<ElementoImagen> getElementsByTipoImagen(Long anguloId, String tipoImagen);
    
    /**
     * Busca elementos de imagen por nombre de archivo.
     * 
     * @param anguloId ID del ángulo (para compatibilidad)
     * @param nombreArchivo nombre de archivo a buscar
     * @param pageable configuración de paginación
     * @return página de elementos
     */
    Page<ElementoImagen> searchElementosByNombreArchivo(Long anguloId, String nombreArchivo, Pageable pageable);
    
    /**
     * Busca elementos de imagen con tamaño mayor que el especificado.
     * 
     * @param anguloId ID del ángulo (para compatibilidad)
     * @param tamanoMinimo tamaño mínimo en bytes
     * @return lista de elementos
     */
    List<ElementoImagen> getElementsByTamanoMayorQue(Long anguloId, Long tamanoMinimo);
    
    /**
     * Busca elementos de imagen con tamaño menor que el especificado.
     * 
     * @param anguloId ID del ángulo (para compatibilidad)
     * @param tamanoMaximo tamaño máximo en bytes
     * @return lista de elementos
     */
    List<ElementoImagen> getElementsByTamanoMenorQue(Long anguloId, Long tamanoMaximo);
    
    /**
     * Obtiene elementos de imagen asociados a un ángulo.
     * Mantenido para compatibilidad con el controlador.
     * 
     * @param anguloId ID del ángulo
     * @return lista de elementos
     */
    List<ElementoImagen> getElementsByAnguloId(Long anguloId);
    
    /**
     * Obtiene elementos de imagen asociados a un ángulo con paginación.
     * Mantenido para compatibilidad con el controlador.
     * 
     * @param anguloId ID del ángulo
     * @param pageable configuración de paginación
     * @return página de elementos
     */
    Page<ElementoImagen> getElementsByAnguloId(Long anguloId, Pageable pageable);
    
    /**
     * Obtiene un elemento de imagen por su ID y el ID del ángulo.
     * Mantenido para compatibilidad con el controlador.
     * 
     * @param elementoId ID del elemento
     * @param anguloId ID del ángulo
     * @return Optional con el elemento o vacío
     */
    Optional<ElementoImagen> getElementByIdAndAnguloId(Long elementoId, Long anguloId);
    
    /**
     * Sube una imagen a Cloudinary y crea un elemento de imagen.
     * 
     * @param anguloId ID del ángulo (para compatibilidad)
     * @param file archivo de imagen
     * @return elemento de imagen creado
     * @throws ImageProcessingException si hay error al procesar la imagen
     */
    ElementoImagen uploadAndCreateElementoImagen(Long anguloId, MultipartFile file) throws ImageProcessingException;
    
    /**
     * Actualiza un elemento de imagen con un nuevo archivo.
     * 
     * @param elementoId ID del elemento
     * @param file nuevo archivo de imagen
     * @return elemento actualizado
     * @throws ImageProcessingException si hay error al procesar la imagen
     */
    ElementoImagen updateElementoImagen(Long elementoId, MultipartFile file) throws ImageProcessingException;
    
    /**
     * Elimina un elemento de imagen y su archivo en Cloudinary.
     * 
     * @param elementoId ID del elemento
     * @return true si se eliminó correctamente
     */
    boolean deleteElement(Long elementoId);
}
