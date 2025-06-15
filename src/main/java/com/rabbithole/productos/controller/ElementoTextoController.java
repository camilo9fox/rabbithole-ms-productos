package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.ElementoTexto;
import com.rabbithole.productos.service.ElementoTextoService;
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
 * Controlador REST para operaciones con elementos de texto en diseños personalizados.
 */
@RestController
@RequestMapping("/api/disenos-personalizados/{disenoId}/angulos/{anguloId}/textos")
@Slf4j
public class ElementoTextoController {

    private final ElementoTextoService elementoTextoService;

    @Autowired
    public ElementoTextoController(ElementoTextoService elementoTextoService) {
        this.elementoTextoService = elementoTextoService;
    }

    /**
     * Obtiene todos los elementos de texto de un ángulo específico.
     *
     * @param anguloId ID del ángulo
     * @return Lista de elementos de texto
     */
    @GetMapping
    public ResponseEntity<List<ElementoTexto>> getAllElementosTexto(@PathVariable Long anguloId) {
        List<ElementoTexto> elementos = elementoTextoService.findByElementoId(anguloId);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Obtiene todos los elementos de texto de un ángulo con paginación.
     *
     * @param anguloId ID del ángulo
     * @param pageable Información de paginación
     * @return Página de elementos de texto
     */
    @GetMapping("/pagina")
    public ResponseEntity<Page<ElementoTexto>> getAllElementosTextoPaginados(
            @PathVariable Long anguloId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<ElementoTexto> elementos = elementoTextoService.findAll(pageable);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Obtiene un elemento de texto específico.
     *
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento
     * @return Elemento de texto encontrado o 404 si no existe
     */
    @GetMapping("/{elementoId}")
    public ResponseEntity<ElementoTexto> getElementoTextoById(
            @PathVariable Long anguloId,
            @PathVariable Long elementoId) {
        
        Optional<ElementoTexto> elemento = elementoTextoService.findById(elementoId);
        return elemento.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo elemento de texto.
     *
     * @param anguloId ID del ángulo
     * @param elemento Datos del nuevo elemento
     * @return Elemento de texto creado o 404 si el ángulo no existe
     */
    @PostMapping
    public ResponseEntity<ElementoTexto> createElementoTexto(
            @PathVariable Long anguloId,
            @Valid @RequestBody ElementoTexto elemento) {
        
        // El anguloId ahora se usa solo como referencia pero ya no se establece en la entidad
        elemento.setPosicionX(elemento.getPosicionX() != null ? elemento.getPosicionX() : 0);
        elemento.setPosicionY(elemento.getPosicionY() != null ? elemento.getPosicionY() : 0);
        ElementoTexto nuevoElemento = elementoTextoService.save(elemento);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoElemento);
    }

    /**
     * Actualiza un elemento de texto existente.
     *
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento
     * @param elemento Datos actualizados
     * @return Elemento de texto actualizado o 404 si no existe
     */
    @PutMapping("/{elementoId}")
    public ResponseEntity<ElementoTexto> updateElementoTexto(
            @PathVariable Long anguloId,
            @PathVariable Long elementoId,
            @Valid @RequestBody ElementoTexto elemento) {
        
        // Verificar que el elemento exista
        if (!elementoTextoService.findById(elementoId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // Asegurarnos de establecer el ID correcto en el elemento a actualizar
        elemento.setId(elementoId);
        ElementoTexto elementoActualizado = elementoTextoService.save(elemento);
        return ResponseEntity.ok(elementoActualizado);
    }

    /**
     * Elimina un elemento de texto.
     *
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento
     * @return 204 No Content si se eliminó correctamente, 404 si no existe
     */
    @DeleteMapping("/{elementoId}")
    public ResponseEntity<Void> deleteElementoTexto(
            @PathVariable Long anguloId,
            @PathVariable Long elementoId) {
        
        // Verificar que el elemento exista
        if (!elementoTextoService.findById(elementoId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        elementoTextoService.deleteById(elementoId);
        return ResponseEntity.noContent().build();
    }

    // El endpoint para cambiar estado activo se ha eliminado porque el campo activo ya no existe en ElementoDiseno

    /**
     * Encuentra elementos de texto por fuente.
     *
     * @param anguloId ID del ángulo
     * @param fuente Nombre de la fuente
     * @return Lista de elementos de texto con la fuente especificada
     */
    @GetMapping("/fuente/{fuente}")
    public ResponseEntity<List<ElementoTexto>> getElementosByFuente(
            @PathVariable Long anguloId,
            @PathVariable String fuente) {
        
        List<ElementoTexto> elementos = elementoTextoService.getElementosByFuente(fuente);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Encuentra elementos de texto por tamaño de fuente.
     *
     * @param anguloId ID del ángulo
     * @param tamanoFuente Tamaño de la fuente
     * @return Lista de elementos de texto con el tamaño de fuente especificado
     */
    @GetMapping("/tamano-fuente/{tamanoFuente}")
    public ResponseEntity<List<ElementoTexto>> getElementsByTamanoFuente(
            @PathVariable Long anguloId,
            @PathVariable Integer tamanoFuente) {
        
        List<ElementoTexto> elementos = elementoTextoService.getElementsByTamanoFuente(tamanoFuente);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Busca elementos de texto por contenido (texto) con paginación.
     *
     * @param anguloId ID del ángulo
     * @param texto Texto a buscar
     * @param pageable Información de paginación
     * @return Página de elementos de texto que contienen el texto especificado
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<ElementoTexto>> searchElementosByTexto(
            @PathVariable Long anguloId,
            @RequestParam String texto,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<ElementoTexto> elementos = elementoTextoService.searchElementosByTexto(texto, pageable);
        return ResponseEntity.ok(elementos);
    }
}
