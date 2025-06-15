package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.AnguloDiseno;
import com.rabbithole.productos.service.AnguloDisenoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para manejar operaciones con la entidad AnguloDiseno.
 * Proporciona endpoints para crear, actualizar, eliminar y consultar ángulos de diseños personalizados.
 */
@RestController
@RequestMapping("/api/disenos-personalizados/{disenoId}/angulos")
@Slf4j
public class AnguloDisenoController {

    private final AnguloDisenoService anguloDisenoService;

    @Autowired
    public AnguloDisenoController(AnguloDisenoService anguloDisenoService) {
        this.anguloDisenoService = anguloDisenoService;
    }

    /**
     * Obtiene todos los ángulos de un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @return Lista de ángulos del diseño
     */
    @GetMapping
    public ResponseEntity<List<AnguloDiseno>> getAngulosByDisenoId(@PathVariable Long disenoId) {
        List<AnguloDiseno> angulos = anguloDisenoService.getAngulosByDisenoId(disenoId);
        return ResponseEntity.ok(angulos);
    }

    /**
     * Obtiene todos los ángulos de un diseño personalizado con paginación.
     *
     * @param disenoId ID del diseño personalizado
     * @param pageable Información de paginación
     * @return Página de ángulos del diseño
     */
    @GetMapping("/pagina")
    public ResponseEntity<Page<AnguloDiseno>> getAngulosByDisenoIdPaginados(
            @PathVariable Long disenoId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<AnguloDiseno> angulos = anguloDisenoService.getAngulosByDisenoId(disenoId, pageable);
        return ResponseEntity.ok(angulos);
    }

    /**
     * Obtiene un ángulo específico de un diseño.
     *
     * @param disenoId ID del diseño personalizado
     * @param anguloId ID del ángulo
     * @return Ángulo encontrado o 404 si no existe
     */
    @GetMapping("/{anguloId}")
    public ResponseEntity<AnguloDiseno> getAnguloDisenoById(
            @PathVariable Long disenoId,
            @PathVariable Long anguloId) {
        
        Optional<AnguloDiseno> angulo = anguloDisenoService.getAnguloDisenoById(disenoId, anguloId);
        return angulo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo ángulo para un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @param angulo Datos del nuevo ángulo
     * @return Ángulo creado o 404 si el diseño no existe
     */
    @PostMapping
    public ResponseEntity<AnguloDiseno> createAnguloDiseno(
            @PathVariable Long disenoId,
            @Valid @RequestBody AnguloDiseno angulo) {
        
        Optional<AnguloDiseno> nuevoAngulo = anguloDisenoService.createAnguloDiseno(disenoId, angulo);
        return nuevoAngulo.map(a -> ResponseEntity.status(HttpStatus.CREATED).body(a))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un ángulo existente de un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @param anguloId ID del ángulo
     * @param angulo Datos actualizados
     * @return Ángulo actualizado o 404 si no existe
     */
    @PutMapping("/{anguloId}")
    public ResponseEntity<AnguloDiseno> updateAnguloDiseno(
            @PathVariable Long disenoId,
            @PathVariable Long anguloId,
            @Valid @RequestBody AnguloDiseno angulo) {
        
        // Verificar que el ángulo pertenezca al diseño especificado
        Optional<AnguloDiseno> anguloExistente = anguloDisenoService.getAnguloDisenoById(disenoId, anguloId);
        if (anguloExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<AnguloDiseno> anguloActualizado = anguloDisenoService.updateAnguloDiseno(anguloId, angulo);
        return anguloActualizado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un ángulo de un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @param anguloId ID del ángulo
     * @return 204 No Content si se eliminó correctamente, 404 si no existe
     */
    @DeleteMapping("/{anguloId}")
    public ResponseEntity<Void> deleteAnguloDiseno(
            @PathVariable Long disenoId,
            @PathVariable Long anguloId) {
        
        // Verificar que el ángulo pertenezca al diseño especificado
        Optional<AnguloDiseno> anguloExistente = anguloDisenoService.getAnguloDisenoById(disenoId, anguloId);
        if (anguloExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (anguloDisenoService.deleteAnguloDiseno(anguloId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene ángulos por tipo para un diseño específico.
     *
     * @param disenoId ID del diseño personalizado
     * @param tipoAnguloId ID del tipo de ángulo
     * @return Lista de ángulos del tipo especificado en el diseño
     */
    @GetMapping("/tipo/{tipoAnguloId}")
    public ResponseEntity<List<AnguloDiseno>> getAngulosByTipo(
            @PathVariable Long disenoId,
            @PathVariable Long tipoAnguloId) {
        
        List<AnguloDiseno> angulos = anguloDisenoService.getAngulosByTipo(disenoId, tipoAnguloId);
        return ResponseEntity.ok(angulos);
    }
}
