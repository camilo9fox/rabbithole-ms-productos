package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.ThumbnailItemDTO;
import com.rabbithole.productos.dto.request.ThumbnailBase64RequestDTO;
import com.rabbithole.productos.dto.request.ThumbnailUploadRequestDTO;
import com.rabbithole.productos.service.ThumbnailItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/thumbnails")
@CrossOrigin(origins = "*")
public class ThumbnailItemController {

    private final ThumbnailItemService thumbnailItemService;

    public ThumbnailItemController(ThumbnailItemService thumbnailItemService) {
        this.thumbnailItemService = thumbnailItemService;
    }

    /**
     * Obtiene todos los thumbnails para un ítem de carrito
     */
    @GetMapping("/carrito/{itemCarritoId}")
    public ResponseEntity<List<ThumbnailItemDTO>> getThumbnailsByItemCarritoId(@PathVariable Long itemCarritoId) {
        List<ThumbnailItemDTO> thumbnails = thumbnailItemService.getThumbnailsByItemCarritoId(itemCarritoId);
        return ResponseEntity.ok(thumbnails);
    }

    /**
     * Obtiene todos los thumbnails para un ítem de orden
     */
    @GetMapping("/orden/{itemOrdenId}")
    public ResponseEntity<List<ThumbnailItemDTO>> getThumbnailsByItemOrdenId(@PathVariable Long itemOrdenId) {
        List<ThumbnailItemDTO> thumbnails = thumbnailItemService.getThumbnailsByItemOrdenId(itemOrdenId);
        return ResponseEntity.ok(thumbnails);
    }

    /**
     * Sube un thumbnail para un ítem de carrito
     */
    @PostMapping(value = "/carrito/{itemCarritoId}/angulo/{tipoAnguloId}")
    public ResponseEntity<ThumbnailItemDTO> uploadThumbnailForItemCarrito(
            @PathVariable Long itemCarritoId,
            @PathVariable Long tipoAnguloId,
            @RequestBody ThumbnailBase64RequestDTO requestDTO) {

        ThumbnailItemDTO thumbnailItemDTO = thumbnailItemService.uploadThumbnailForItemCarrito(
                itemCarritoId, tipoAnguloId, requestDTO.getBase64Image());
        return new ResponseEntity<>(thumbnailItemDTO, HttpStatus.CREATED);
    }

    /**
     * Sube un thumbnail para un ítem de orden
     */
    @PostMapping(value = "/orden/{itemOrdenId}/angulo/{tipoAnguloId}")
    public ResponseEntity<ThumbnailItemDTO> uploadThumbnailForItemOrden(
            @PathVariable Long itemOrdenId,
            @PathVariable Long tipoAnguloId,
            @RequestBody ThumbnailBase64RequestDTO requestDTO) {

        ThumbnailItemDTO thumbnailItemDTO = thumbnailItemService.uploadThumbnailForItemOrden(
                itemOrdenId, tipoAnguloId, requestDTO.getBase64Image());
        return new ResponseEntity<>(thumbnailItemDTO, HttpStatus.CREATED);
    }

    /**
     * Actualiza un thumbnail por URL (sin subir archivo)
     */
    @PostMapping("/update-by-url")
    public ResponseEntity<ThumbnailItemDTO> updateThumbnailByUrl(
            @RequestBody ThumbnailUploadRequestDTO requestDTO) {

        ThumbnailItemDTO thumbnailItemDTO = thumbnailItemService.updateThumbnailByUrl(requestDTO);
        return ResponseEntity.ok(thumbnailItemDTO);
    }

    /**
     * Elimina un thumbnail por ID
     */
    @DeleteMapping("/{thumbnailId}")
    public ResponseEntity<Void> deleteThumbnail(@PathVariable Long thumbnailId) {
        thumbnailItemService.deleteThumbnail(thumbnailId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina todos los thumbnails de un ítem de carrito
     */
    @DeleteMapping("/carrito/{itemCarritoId}")
    public ResponseEntity<Void> deleteThumbnailsByItemCarritoId(@PathVariable Long itemCarritoId) {
        thumbnailItemService.deleteThumbnailsByItemCarritoId(itemCarritoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina todos los thumbnails de un ítem de orden
     */
    @DeleteMapping("/orden/{itemOrdenId}")
    public ResponseEntity<Void> deleteThumbnailsByItemOrdenId(@PathVariable Long itemOrdenId) {
        thumbnailItemService.deleteThumbnailsByItemOrdenId(itemOrdenId);
        return ResponseEntity.noContent().build();
    }
}
