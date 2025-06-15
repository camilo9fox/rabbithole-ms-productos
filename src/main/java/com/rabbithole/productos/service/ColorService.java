package com.rabbithole.productos.service;

import com.rabbithole.productos.model.Color;
import com.rabbithole.productos.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar operaciones con la entidad Color.
 */
@Service
public class ColorService {

    private final ColorRepository colorRepository;

    @Autowired
    public ColorService(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    /**
     * Obtiene todos los colores disponibles.
     * 
     * @return Lista de todos los colores
     */
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    /**
     * Obtiene un color por su ID.
     * 
     * @param id ID del color
     * @return Color encontrado o empty si no existe
     */
    public Optional<Color> getColorById(String id) {
        return colorRepository.findById(id);
    }
}
