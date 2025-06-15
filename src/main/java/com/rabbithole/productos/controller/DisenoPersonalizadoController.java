package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.DisenoPersonalizado;
import com.rabbithole.productos.service.DisenoPersonalizadoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para manejar operaciones con la entidad DisenoPersonalizado.
 * Proporciona endpoints para crear, actualizar, eliminar y consultar diseños personalizados.
 */
@RestController
@RequestMapping("/api/disenos-personalizados")
@Slf4j
public class DisenoPersonalizadoController {

    private final DisenoPersonalizadoService disenoPersonalizadoService;

    @Autowired
    public DisenoPersonalizadoController(DisenoPersonalizadoService disenoPersonalizadoService) {
        this.disenoPersonalizadoService = disenoPersonalizadoService;
    }

    /**
     * Obtiene todos los diseños personalizados con paginación.
     *
     * @param pageable Información de paginación
     * @return Página de diseños personalizados
     */
    @GetMapping
    public ResponseEntity<Page<DisenoPersonalizado>> getAllDisenosPersonalizados(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        
        Page<DisenoPersonalizado> disenos = disenoPersonalizadoService.getAllDisenosPersonalizados(pageable);
        return ResponseEntity.ok(disenos);
    }

    /**
     * Obtiene un diseño personalizado por su ID.
     *
     * @param id ID del diseño personalizado
     * @return Diseño personalizado encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<DisenoPersonalizado> getDisenoPersonalizadoById(@PathVariable Long id) {
        Optional<DisenoPersonalizado> diseno = disenoPersonalizadoService.getDisenoPersonalizadoById(id);
        return diseno.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo diseño personalizado.
     *
     * @param diseno Datos del nuevo diseño
     * @return Diseño personalizado creado
     */
    @PostMapping
    public ResponseEntity<DisenoPersonalizado> createDisenoPersonalizado(
            @Valid @RequestBody DisenoPersonalizado diseno) {
        
        DisenoPersonalizado nuevoDiseno = disenoPersonalizadoService.createDisenoPersonalizado(diseno);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDiseno);
    }

    /**
     * Actualiza un diseño personalizado existente.
     *
     * @param id ID del diseño personalizado a actualizar
     * @param diseno Datos actualizados
     * @return Diseño personalizado actualizado o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<DisenoPersonalizado> updateDisenoPersonalizado(
            @PathVariable Long id,
            @Valid @RequestBody DisenoPersonalizado diseno) {
        
        Optional<DisenoPersonalizado> disenoActualizado = 
                disenoPersonalizadoService.updateDisenoPersonalizado(id, diseno);
        
        return disenoActualizado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un diseño personalizado por su ID.
     *
     * @param id ID del diseño personalizado a eliminar
     * @return 204 No Content si se eliminó correctamente, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisenoPersonalizado(@PathVariable Long id) {
        if (disenoPersonalizadoService.deleteDisenoPersonalizado(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene diseños personalizados de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @param pageable Información de paginación
     * @return Página de diseños personalizados del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<DisenoPersonalizado>> getDisenosPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        
        Page<DisenoPersonalizado> disenos = 
                disenoPersonalizadoService.getDisenosPorUsuario(usuarioId, pageable);
        
        return ResponseEntity.ok(disenos);
    }

    /**
     * Obtiene diseños personalizados públicos.
     * NOTA: Esta funcionalidad está temporalmente modificada para devolver todos los diseños hasta que
     * se implemente el campo 'publico' en la entidad.
     *
     * @param pageable Información de paginación
     * @return Página de diseños personalizados
     * @deprecated El campo 'publico' no existe en la entidad DisenoPersonalizado.
     * @since 1.0
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @GetMapping("/publicos")
    public ResponseEntity<Page<DisenoPersonalizado>> getDiseniosPublicos(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        
        Page<DisenoPersonalizado> disenos = 
                disenoPersonalizadoService.getDiseniosPublicos(pageable);
        
        return ResponseEntity.ok(disenos);
    }

    /**
     * Cambia el estado de aprobación de un diseño personalizado.
     *
     * @param id ID del diseño personalizado
     * @param estadoId ID del nuevo estado
     * @return Diseño personalizado actualizado o 404 si no existe
     */
    @PatchMapping("/{id}/estado/{estadoId}")
    public ResponseEntity<DisenoPersonalizado> cambiarEstadoDisenoPersonalizado(
            @PathVariable Long id,
            @PathVariable Long estadoId) {
        
        Optional<DisenoPersonalizado> disenoActualizado = 
                disenoPersonalizadoService.cambiarEstadoDiseno(id, estadoId);
        
        return disenoActualizado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cambia la visibilidad pública de un diseño personalizado.
     * NOTA: Esta funcionalidad está temporalmente deshabilitada hasta que se agregue el campo 'publico' a la entidad.
     *
     * @param id ID del diseño personalizado
     * @param esPublico Flag para marcar como público/privado
     * @return Diseño personalizado (sin cambios) o 404 si no existe
     * @deprecated El campo 'publico' no existe en la entidad DisenoPersonalizado.
     * @since 1.0
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @PatchMapping("/{id}/publico")
    public ResponseEntity<DisenoPersonalizado> cambiarVisibilidadDisenoPersonalizado(
            @PathVariable Long id,
            @RequestParam boolean esPublico) {
        
        // Método depreciado - El campo 'publico' no existe en la entidad
        Optional<DisenoPersonalizado> disenoActualizado = 
                disenoPersonalizadoService.cambiarVisibilidadDiseno(id, esPublico);
        
        // Añadir mensaje de advertencia
        log.warn("Intento de cambiar visibilidad en el diseño ID: {} a público: {} - Funcionalidad no implementada", id, esPublico);
        
        return disenoActualizado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca diseños personalizados por texto en nombre o descripción.
     *
     * @param texto Texto de búsqueda
     * @param pageable Información de paginación
     * @return Página de diseños personalizados que coinciden con la búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<DisenoPersonalizado>> buscarDisenosPorTexto(
            @RequestParam String texto,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        
        Page<DisenoPersonalizado> disenos = 
                disenoPersonalizadoService.buscarDisenosPorTexto(texto, pageable);
        
        return ResponseEntity.ok(disenos);
    }
}
