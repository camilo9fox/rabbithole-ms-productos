package com.rabbithole.productos.controller;

import com.rabbithole.productos.model.ElementoImagen;
import com.rabbithole.productos.service.ElementoImagenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import com.rabbithole.productos.exception.ImageProcessingException;

/**
 * Controlador REST para operaciones con elementos de imagen en diseños personalizados.
 */
@RestController
@RequestMapping("/api/disenos-personalizados/{disenoId}/angulos/{anguloId}/imagenes")
@Slf4j
@CrossOrigin(origins = "*")
public class ElementoImagenController {

    private final ElementoImagenService elementoImagenService;

    @Autowired
    public ElementoImagenController(ElementoImagenService elementoImagenService) {
        this.elementoImagenService = elementoImagenService;
    }

    /**
     * Obtiene todos los elementos de imagen de un ángulo específico.
     *
     * @param anguloId ID del ángulo
     * @return Lista de elementos de imagen
     */
    @GetMapping
    public ResponseEntity<List<ElementoImagen>> getAllElementosImagen(@PathVariable Long anguloId) {
        List<ElementoImagen> elementos = elementoImagenService.getElementsByAnguloId(anguloId);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Obtiene todos los elementos de imagen de un ángulo con paginación.
     *
     * @param anguloId ID del ángulo
     * @param pageable Información de paginación
     * @return Página de elementos de imagen
     */
    @GetMapping("/pagina")
    public ResponseEntity<Page<ElementoImagen>> getAllElementosImagenPaginados(
            @PathVariable Long anguloId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<ElementoImagen> elementos = elementoImagenService.getElementsByAnguloId(anguloId, pageable);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Obtiene un elemento de imagen específico.
     *
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento
     * @return Elemento de imagen encontrado o 404 si no existe
     */
    @GetMapping("/{elementoId}")
    public ResponseEntity<ElementoImagen> getElementoImagenById(
            @PathVariable Long anguloId,
            @PathVariable Long elementoId) {
        
        Optional<ElementoImagen> elemento = elementoImagenService.getElementByIdAndAnguloId(elementoId, anguloId);
        return elemento.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo elemento de imagen.
     *
     * @param anguloId ID del ángulo
     * @param elemento Datos del nuevo elemento
     * @return Elemento de imagen creado o 404 si el ángulo no existe
     */
    @PostMapping
    public ResponseEntity<ElementoImagen> createElementoImagen(
            @PathVariable Long anguloId,
            @Valid @RequestBody ElementoImagen elemento) {
        
        Optional<ElementoImagen> nuevoElemento = elementoImagenService.createElement(anguloId, elemento);
        return nuevoElemento.map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un elemento de imagen existente.
     *
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento
     * @param elemento Datos actualizados
     * @return Elemento de imagen actualizado o 404 si no existe
     */
    @PutMapping("/{elementoId}")
    public ResponseEntity<ElementoImagen> updateElementoImagen(
            @PathVariable Long anguloId,
            @PathVariable Long elementoId,
            @Valid @RequestBody ElementoImagen elemento) {
        
        // Verificar que el elemento pertenezca al ángulo especificado
        if (elementoImagenService.getElementByIdAndAnguloId(elementoId, anguloId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<ElementoImagen> elementoActualizado = elementoImagenService.updateElement(elementoId, elemento);
        return elementoActualizado.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un elemento de imagen.
     *
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento
     * @return 204 No Content si se eliminó correctamente, 404 si no existe
     */
    @DeleteMapping("/{elementoId}")
    public ResponseEntity<Void> deleteElementoImagen(
            @PathVariable Long anguloId,
            @PathVariable Long elementoId) {
        
        // Verificar que el elemento pertenezca al ángulo especificado
        if (elementoImagenService.getElementByIdAndAnguloId(elementoId, anguloId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (elementoImagenService.deleteElement(elementoId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // El endpoint para cambiar estado activo se ha eliminado porque el campo activo ya no existe en ElementoDiseno

    /**
     * Encuentra elementos de imagen por tipo de imagen.
     *
     * @param anguloId ID del ángulo
     * @param tipoImagen Tipo de imagen
     * @return Lista de elementos de imagen del tipo especificado
     */
    @GetMapping("/tipo/{tipoImagen}")
    public ResponseEntity<List<ElementoImagen>> getElementsByTipoImagen(
            @PathVariable Long anguloId,
            @PathVariable String tipoImagen) {
        
        List<ElementoImagen> elementos = elementoImagenService.getElementsByTipoImagen(anguloId, tipoImagen);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Busca elementos de imagen por nombre de archivo.
     *
     * @param anguloId ID del ángulo
     * @param nombreArchivo Nombre de archivo a buscar
     * @param pageable Información de paginación
     * @return Página de elementos de imagen que coinciden con el nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<ElementoImagen>> searchElementosByNombreArchivo(
            @PathVariable Long anguloId,
            @RequestParam String nombreArchivo,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<ElementoImagen> elementos = elementoImagenService.searchElementosByNombreArchivo(
                anguloId, nombreArchivo, pageable);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Encuentra elementos de imagen con tamaño mayor al especificado.
     *
     * @param anguloId ID del ángulo
     * @param tamanoMinimo Tamaño mínimo en bytes
     * @return Lista de elementos de imagen con tamaño mayor al especificado
     */
    @GetMapping("/tamano-mayor")
    public ResponseEntity<List<ElementoImagen>> getElementsByTamanoMayorQue(
            @PathVariable Long anguloId,
            @RequestParam Long tamanoMinimo) {
        
        List<ElementoImagen> elementos = elementoImagenService.getElementsByTamanoMayorQue(anguloId, tamanoMinimo);
        return ResponseEntity.ok(elementos);
    }

    /**
     * Encuentra elementos de imagen con tamaño menor al especificado.
     *
     * @param anguloId ID del ángulo
     * @param tamanoMaximo Tamaño máximo en bytes
     * @return Lista de elementos de imagen con tamaño menor al especificado
     */
    @GetMapping("/tamano-menor")
    public ResponseEntity<List<ElementoImagen>> getElementsByTamanoMenorQue(
            @PathVariable Long anguloId,
            @RequestParam Long tamanoMaximo) {
        
        List<ElementoImagen> elementos = elementoImagenService.getElementsByTamanoMenorQue(anguloId, tamanoMaximo);
        return ResponseEntity.ok(elementos);
    }
    
    /**
     * Sube una imagen a Cloudinary y crea un elemento de imagen asociado a un ángulo de diseño.
     *
     * @param disenoId ID del diseño personalizado (para mantener la consistencia de rutas)
     * @param anguloId ID del ángulo
     * @param file Archivo de imagen a subir
     * @return Elemento de imagen creado
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadAndCreateElementoImagen(
            @PathVariable Long disenoId,
            @PathVariable Long anguloId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Archivo vacío");
            }
            
            ElementoImagen elemento = elementoImagenService.uploadAndCreateElementoImagen(anguloId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(elemento);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al subir imagen", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ImageProcessingException e) {
            log.error("Error al procesar o subir imagen a Cloudinary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar o subir imagen: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza un elemento de imagen existente con una nueva imagen.
     *
     * @param disenoId ID del diseño personalizado (para mantener la consistencia de rutas)
     * @param anguloId ID del ángulo
     * @param elementoId ID del elemento de imagen
     * @param file Nuevo archivo de imagen
     * @return Elemento de imagen actualizado
     */
    @PutMapping(value = "/{elementoId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateElementoImagenWithFile(
            @PathVariable Long disenoId,
            @PathVariable Long anguloId,
            @PathVariable Long elementoId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Verificar que el elemento pertenezca al ángulo especificado
            if (elementoImagenService.getElementByIdAndAnguloId(elementoId, anguloId).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Archivo vacío");
            }
            
            ElementoImagen elementoActualizado = elementoImagenService.updateElementoImagen(elementoId, file);
            return ResponseEntity.ok(elementoActualizado);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar imagen", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ImageProcessingException e) {
            log.error("Error al procesar o subir nueva imagen a Cloudinary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar imagen: " + e.getMessage());
        }
    }
}
