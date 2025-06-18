package com.rabbithole.productos.service;

import com.rabbithole.productos.model.Fuente;
import com.rabbithole.productos.repository.FuenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar operaciones con la entidad Fuente.
 */
@Service
public class FuenteService {

    private final FuenteRepository fuenteRepository;

    @Autowired
    public FuenteService(FuenteRepository fuenteRepository) {
        this.fuenteRepository = fuenteRepository;
    }

    /**
     * Obtiene todas las fuentes disponibles.
     * 
     * @return Lista de todas las fuentes
     */
    public List<Fuente> getAllFuentes() {
        return fuenteRepository.findAll();
    }

    /**
     * Obtiene una fuente por su ID.
     * 
     * @param id ID de la fuente
     * @return Fuente encontrada o empty si no existe
     */
    public Optional<Fuente> getFuenteById(Long id) {
        return fuenteRepository.findById(id);
    }

    /**
     * Guarda una nueva fuente.
     * 
     * @param fuente Fuente a guardar
     * @return Fuente guardada
     */
    public Fuente saveFuente(Fuente fuente) {
        return fuenteRepository.save(fuente);
    }

    /**
     * Actualiza una fuente existente.
     * 
     * @param id ID de la fuente a actualizar
     * @param fuente Nuevos datos de la fuente
     * @return Fuente actualizada o null si no se encuentra
     */
    public Fuente updateFuente(Long id, Fuente fuente) {
        if (fuenteRepository.existsById(id)) {
            fuente.setId(id);
            return fuenteRepository.save(fuente);
        }
        return null;
    }

    /**
     * Elimina una fuente por su ID.
     * 
     * @param id ID de la fuente a eliminar
     */
    public void deleteFuente(Long id) {
        fuenteRepository.deleteById(id);
    }
}
