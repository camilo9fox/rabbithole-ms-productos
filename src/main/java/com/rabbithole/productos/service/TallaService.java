package com.rabbithole.productos.service;

import com.rabbithole.productos.model.Talla;
import com.rabbithole.productos.repository.TallaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar la lógica de negocio relacionada con las tallas.
 */
@Service
@Slf4j
@Transactional
public class TallaService {

    private final TallaRepository tallaRepository;

    @Autowired
    public TallaService(TallaRepository tallaRepository) {
        this.tallaRepository = tallaRepository;
    }

    /**
     * Obtiene todas las tallas.
     *
     * @return Lista de todas las tallas
     */
    @Transactional(readOnly = true)
    public List<Talla> getAllTallas() {
        log.debug("Obteniendo todas las tallas");
        return tallaRepository.findAll();
    }

    /**
     * Obtiene todas las tallas activas.
     *
     * @return Lista de tallas activas
     */
    @Transactional(readOnly = true)
    public List<Talla> getTallasActivas() {
        log.debug("Obteniendo tallas activas");
        // Filtrar en memoria ya que activo es un campo transient
        return tallaRepository.findAll().stream()
                .filter(talla -> talla.getActivo() != null && talla.getActivo())
                .toList();
    }

    /**
     * Obtiene una talla por su ID.
     *
     * @param id ID de la talla
     * @return Talla encontrada o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<Talla> getTallaById(String id) {
        log.debug("Buscando talla con ID: {}", id);
        return tallaRepository.findById(id);
    }

    /**
     * Busca tallas que contengan el texto especificado en su nombre.
     *
     * @param nombre Texto a buscar
     * @return Lista de tallas que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public List<Talla> buscarTallasPorNombre(String nombre) {
        log.debug("Buscando tallas que contengan: {}", nombre);
        return tallaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Guarda una nueva talla o actualiza una existente.
     *
     * @param talla Talla a guardar/actualizar
     * @return Talla guardada/actualizada
     */
    public Talla saveTalla(Talla talla) {
        boolean esNueva = talla.getId() == null;
        log.debug("{} talla: {}", esNueva ? "Creando" : "Actualizando", talla.getNombre());
        return tallaRepository.save(talla);
    }

    /**
     * Activa o desactiva una talla.
     *
     * @param id ID de la talla
     * @param activo Estado de activación
     * @return Talla actualizada o vacío si no existe
     */
    public Optional<Talla> cambiarEstadoTalla(String id, boolean activo) {
        log.debug("{} talla con ID: {}", activo ? "Activando" : "Desactivando", id);
        Optional<Talla> tallaOpt = tallaRepository.findById(id);
        
        return tallaOpt.map(talla -> {
            talla.setActivo(activo);
            return tallaRepository.save(talla);
        });
    }
}
