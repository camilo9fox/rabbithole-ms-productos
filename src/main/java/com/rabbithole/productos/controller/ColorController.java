package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.Color;
import com.rabbithole.productos.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para manejar operaciones con la entidad Color.
 * Este controlador se utiliza para probar la conexión a la base de datos Oracle.
 */
@RestController
@RequestMapping("/colores")
public class ColorController {

    private final ColorService colorService;

    @Autowired
    public ColorController(ColorService colorService) {
        this.colorService = colorService;
    }

    /**
     * Endpoint para obtener todos los colores.
     * 
     * @return Lista de colores
     */
    @GetMapping
    public ResponseEntity<List<Color>> getAllColors() {
        List<Color> colors = colorService.getAllColors();
        return ResponseEntity.ok(colors);
    }

    /**
     * Endpoint para obtener un color por su ID.
     * 
     * @param id ID del color
     * @return Color encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Color> getColorById(@PathVariable String id) {
        return colorService.getColorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
