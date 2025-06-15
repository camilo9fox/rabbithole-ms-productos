package com.rabbithole.productos.service;

import com.rabbithole.productos.model.Categoria;
import com.rabbithole.productos.repository.CategoriaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar la lógica de negocio relacionada con las categorías.
 */
@Service
@Slf4j
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Obtiene todas las categorías.
     *
     * @return Lista de todas las categorías
     */
    @Transactional(readOnly = true)
    public List<Categoria> getAllCategorias() {
        log.debug("Obteniendo todas las categorías");
        return categoriaRepository.findAll();
    }

    /**
     * Este método ha sido desactivado debido a que el campo 'activo' ya no existe en la entidad.
     * Se mantiene comentado como referencia.
     *
     * @return Lista de categorías activas
     */
    /*
    @Transactional(readOnly = true)
    public List<Categoria> getCategoriasActivas() {
        log.debug("Obteniendo categorías activas");
        return categoriaRepository.findByActivo(true);
    }
    */

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id ID de la categoría
     * @return Categoría encontrada o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<Categoria> getCategoriaById(Long id) {
        log.debug("Buscando categoría con ID: {}", id);
        return categoriaRepository.findById(id);
    }

    /**
     * Busca categorías que contengan el texto especificado en su nombre.
     *
     * @param nombre Texto a buscar
     * @return Lista de categorías que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public List<Categoria> buscarCategoriasPorNombre(String nombre) {
        log.debug("Buscando categorías que contengan: {}", nombre);
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Guarda una nueva categoría o actualiza una existente.
     *
     * @param categoria Categoría a guardar/actualizar
     * @return Categoría guardada/actualizada
     */
    public Categoria saveCategoria(Categoria categoria) {
        boolean esNueva = categoria.getId() == null;
        log.debug("{} categoría: {}", esNueva ? "Creando" : "Actualizando", categoria.getNombre());
        return categoriaRepository.save(categoria);
    }

    /**
     * Este método ha sido desactivado debido a que el campo 'activo' ya no existe en la entidad.
     * Se mantiene comentado como referencia.
     *
     * @param id ID de la categoría
     * @param activo Estado de activación
     * @return Categoría actualizada o vacío si no existe
     */
    /*
    public Optional<Categoria> cambiarEstadoCategoria(Long id, boolean activo) {
        log.debug("{} categoría con ID: {}", activo ? "Activando" : "Desactivando", id);
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        
        return categoriaOpt.map(categoria -> {
            categoria.setActivo(activo);
            return categoriaRepository.save(categoria);
        });
    }
    */
}
