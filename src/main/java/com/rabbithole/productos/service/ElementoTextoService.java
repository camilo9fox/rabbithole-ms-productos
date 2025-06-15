package com.rabbithole.productos.service;

import com.rabbithole.productos.model.ElementoTexto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para operaciones relacionadas con elementos de texto.
 */
public interface ElementoTextoService {
    
    /**
     * Guarda un elemento de texto en la base de datos.
     *
     * @param elementoTexto Elemento de texto a guardar
     * @return Elemento de texto guardado
     */
    ElementoTexto save(ElementoTexto elementoTexto);
    
    /**
     * Busca un elemento de texto por su ID.
     *
     * @param id ID del elemento de texto
     * @return Optional con el elemento si existe
     */
    Optional<ElementoTexto> findById(Long id);
    
    /**
     * Elimina un elemento de texto por su ID.
     *
     * @param id ID del elemento de texto a eliminar
     */
    void deleteById(Long id);
    
    /**
     * Obtiene todos los elementos de texto.
     *
     * @return Lista de elementos de texto
     */
    List<ElementoTexto> findAll();
    
    /**
     * Obtiene todos los elementos de texto con paginación.
     *
     * @param pageable Información de paginación
     * @return Página de elementos de texto
     */
    Page<ElementoTexto> findAll(Pageable pageable);
    
    /**
     * Busca elementos de texto por ID de elemento.
     *
     * @param elementoId ID del elemento
     * @return Lista de elementos de texto encontrados
     */
    List<ElementoTexto> findByElementoId(Long elementoId);
    
    /**
     * Encuentra elementos de texto por fuente.
     *
     * @param fuente Nombre de la fuente
     * @return Lista de elementos de texto con la fuente especificada
     */
    List<ElementoTexto> getElementosByFuente(String fuente);
    
    /**
     * Encuentra elementos de texto por tamaño de fuente.
     *
     * @param tamanoFuente Tamaño de la fuente
     * @return Lista de elementos de texto con el tamaño de fuente especificado
     */
    List<ElementoTexto> getElementsByTamanoFuente(Integer tamanoFuente);
    
    /**
     * Busca elementos de texto por contenido (texto) con paginación.
     *
     * @param texto Texto a buscar (parcial)
     * @param pageable Información de paginación
     * @return Página de elementos de texto que contienen el texto especificado
     */
    Page<ElementoTexto> searchElementosByTexto(String texto, Pageable pageable);
}
