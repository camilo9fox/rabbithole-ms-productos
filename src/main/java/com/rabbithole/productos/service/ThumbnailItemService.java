package com.rabbithole.productos.service;

import com.rabbithole.productos.dto.ThumbnailItemDTO;
import com.rabbithole.productos.dto.request.ThumbnailUploadRequestDTO;
import com.rabbithole.productos.model.CloudinaryResource;
import com.rabbithole.productos.model.ThumbnailItem;
import com.rabbithole.productos.model.TipoAngulo;
import com.rabbithole.productos.repository.CloudinaryResourceRepository;
import com.rabbithole.productos.repository.ItemCarritoRepository;
import com.rabbithole.productos.repository.ItemOrdenRepository;
import com.rabbithole.productos.repository.ThumbnailItemRepository;
import com.rabbithole.productos.repository.TipoAnguloRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rabbithole.productos.util.Base64ImageUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ThumbnailItemService {

    private static final String ERROR_CLOUDINARY = "Error al eliminar imagen de Cloudinary";
    private static final String ERROR_CLOUDINARY_UPLOAD = "Error al subir imagen a Cloudinary";
    private static final String ERROR_TIPO_ANGULO_NO_ENCONTRADO = "Tipo de ángulo no encontrado con ID: ";
    private static final String ERROR_ITEM_CARRITO_NO_ENCONTRADO = "Ítem de carrito no encontrado con ID: ";
    private static final String ERROR_ITEM_ORDEN_NO_ENCONTRADO = "Ítem de orden no encontrado con ID: ";
    private static final String ERROR_THUMBNAIL_NO_ENCONTRADO = "Thumbnail no encontrado con ID: ";

    private final ThumbnailItemRepository thumbnailItemRepository;
    private final CloudinaryResourceRepository cloudinaryResourceRepository;
    private final CloudinaryService cloudinaryService;
    private final TipoAnguloRepository tipoAnguloRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ItemOrdenRepository itemOrdenRepository;
    private final DTOConverterService dtoConverterService;

    public ThumbnailItemService(ThumbnailItemRepository thumbnailItemRepository,
            CloudinaryResourceRepository cloudinaryResourceRepository,
            CloudinaryService cloudinaryService,
            TipoAnguloRepository tipoAnguloRepository,
            ItemCarritoRepository itemCarritoRepository,
            ItemOrdenRepository itemOrdenRepository,
            DTOConverterService dtoConverterService) {
        this.thumbnailItemRepository = thumbnailItemRepository;
        this.cloudinaryResourceRepository = cloudinaryResourceRepository;
        this.cloudinaryService = cloudinaryService;
        this.tipoAnguloRepository = tipoAnguloRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.itemOrdenRepository = itemOrdenRepository;
        this.dtoConverterService = dtoConverterService;
    }

    /**
     * Obtiene todos los thumbnails de un ítem de carrito
     */
    @Transactional(readOnly = true)
    public List<ThumbnailItemDTO> getThumbnailsByItemCarritoId(Long itemCarritoId) {
        List<ThumbnailItem> thumbnails = thumbnailItemRepository.findByItemCarritoId(itemCarritoId);
        return thumbnails.stream()
                .map(dtoConverterService::convertToThumbnailItemDTO)
                .toList();
    }

    /**
     * Obtiene todos los thumbnails de un ítem de orden
     */
    @Transactional(readOnly = true)
    public List<ThumbnailItemDTO> getThumbnailsByItemOrdenId(Long itemOrdenId) {
        List<ThumbnailItem> thumbnails = thumbnailItemRepository.findByItemOrdenId(itemOrdenId);
        return thumbnails.stream()
                .map(dtoConverterService::convertToThumbnailItemDTO)
                .toList();
    }

    /**
     * Sube un nuevo thumbnail para un ítem de carrito
     */
    @Transactional
    public ThumbnailItemDTO uploadThumbnailForItemCarrito(Long itemCarritoId, Long tipoAnguloId, String base64Image) {
        System.out.println("Iniciando uploadThumbnailForItemCarrito con itemCarritoId: " + itemCarritoId
                + ", tipoAnguloId: " + tipoAnguloId);

        // Verificar que el ítem de carrito existe
        var itemCarrito = itemCarritoRepository.findById(itemCarritoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ERROR_ITEM_CARRITO_NO_ENCONTRADO + itemCarritoId));
        System.out.println("Ítem de carrito encontrado con ID: " + itemCarritoId);

        // Verificar que el tipo de ángulo existe
        TipoAngulo tipoAngulo = tipoAnguloRepository.findById(tipoAnguloId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ERROR_TIPO_ANGULO_NO_ENCONTRADO + tipoAnguloId));
        System.out.println("Tipo de ángulo encontrado con ID: " + tipoAnguloId + ", nombre: " + tipoAngulo.getNombre());

        // Verificar si ya existe un thumbnail para este ítem y ángulo
        thumbnailItemRepository.findByItemCarritoIdAndTipoAnguloId(itemCarritoId, tipoAnguloId)
                .ifPresent(existing -> {
                    System.out.println("Encontrado thumbnail existente con ID: " + existing.getId());
                    // Si existe, eliminar el recurso de Cloudinary asociado
                    if (existing.getCloudinaryResource() != null) {
                        try {
                            System.out.println("Eliminando recurso Cloudinary con publicId: "
                                    + existing.getCloudinaryResource().getPublicId());
                            cloudinaryService.eliminarImagen(existing.getCloudinaryResource().getPublicId());
                        } catch (IOException e) {
                            System.err.println("Error al eliminar imagen de Cloudinary: " + e.getMessage());
                            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY, e);
                        }
                        System.out.println("Eliminando registro de recurso Cloudinary en BD");
                        cloudinaryResourceRepository.delete(existing.getCloudinaryResource());
                    }
                    System.out.println("Eliminando registro de thumbnail existente en BD");
                    thumbnailItemRepository.delete(existing);
                });

        // Convertir base64 a MultipartFile
        if (base64Image == null || base64Image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen en formato base64 es requerida");
        }
        System.out.println("Imagen base64 recibida con longitud: " + base64Image.length());

        // Subir imagen a Cloudinary
        Map<String, Object> uploadResult;
        try {
            // Convertir base64 a MultipartFile y subir
            MultipartFile file = Base64ImageUtil.createMultipartFileFromBase64(base64Image);
            System.out.println("Base64 convertido a MultipartFile: " + file.getOriginalFilename());
            uploadResult = cloudinaryService.uploadAnguloThumbnail(file);
            System.out.println("Imagen subida a Cloudinary exitosamente. PublicId: " + uploadResult.get("public_id"));
        } catch (IOException e) {
            System.err.println("Error al subir imagen a Cloudinary: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY_UPLOAD, e);
        }

        // Crear el recurso Cloudinary
        try {
            System.out.println("Creando registro de recurso Cloudinary");
            CloudinaryResource cloudinaryResource = new CloudinaryResource();
            cloudinaryResource.setPublicId((String) uploadResult.get("public_id"));
            cloudinaryResource.setUrlImagen((String) uploadResult.get("url"));
            cloudinaryResourceRepository.save(cloudinaryResource);
            System.out.println("Recurso Cloudinary guardado con ID: " + cloudinaryResource.getId());

            // Crear el thumbnail
            System.out.println("Creando registro de thumbnail");
            ThumbnailItem thumbnail = new ThumbnailItem();
            // Establecer solo la entidad, no el ID directamente para respetar
            // insertable=false, updatable=false
            thumbnail.setItemCarrito(itemCarrito);
            thumbnail.setItemOrden(null); // Establecer explícitamente a NULL para cumplir con la restricción CHECK
            thumbnail.setTipoAngulo(tipoAngulo);
            thumbnail.setCloudinaryResource(cloudinaryResource);

            System.out.println("Guardando thumbnail: itemCarritoId=" + thumbnail.getItemCarritoId()
                    + ", tipoAnguloId=" + tipoAngulo.getId()
                    + ", cloudinaryResourceId=" + cloudinaryResource.getId());

            ThumbnailItem savedThumbnail = thumbnailItemRepository.save(thumbnail);
            System.out.println("Thumbnail guardado exitosamente con ID: " + savedThumbnail.getId());

            return dtoConverterService.convertToThumbnailItemDTO(savedThumbnail);
        } catch (Exception e) {
            System.err.println("Error al guardar thumbnail: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sube un nuevo thumbnail para un ítem de orden
     */
    @Transactional
    public ThumbnailItemDTO uploadThumbnailForItemOrden(Long itemOrdenId, Long tipoAnguloId, String base64Image) {
        // Verificar que el ítem de orden existe
        var itemOrden = itemOrdenRepository.findById(itemOrdenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ERROR_ITEM_ORDEN_NO_ENCONTRADO + itemOrdenId));

        // Verificar que el tipo de ángulo existe
        TipoAngulo tipoAngulo = tipoAnguloRepository.findById(tipoAnguloId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ERROR_TIPO_ANGULO_NO_ENCONTRADO + tipoAnguloId));

        // Verificar si ya existe un thumbnail para este ítem y ángulo
        thumbnailItemRepository.findByItemOrdenIdAndTipoAnguloId(itemOrdenId, tipoAnguloId)
                .ifPresent(existing -> {
                    // Si existe, eliminar el recurso de Cloudinary asociado
                    if (existing.getCloudinaryResource() != null) {
                        try {
                            cloudinaryService.eliminarImagen(existing.getCloudinaryResource().getPublicId());
                        } catch (IOException e) {
                            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY, e);
                        }
                        cloudinaryResourceRepository.delete(existing.getCloudinaryResource());
                    }
                    thumbnailItemRepository.delete(existing);
                });

        // Convertir base64 a MultipartFile
        if (base64Image == null || base64Image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen en formato base64 es requerida");
        }

        // Subir imagen a Cloudinary
        Map<String, Object> uploadResult;
        try {
            // Convertir base64 a MultipartFile y subir
            MultipartFile file = Base64ImageUtil.createMultipartFileFromBase64(base64Image);
            uploadResult = cloudinaryService.uploadAnguloThumbnail(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY_UPLOAD, e);
        }

        // Crear el recurso Cloudinary
        CloudinaryResource cloudinaryResource = new CloudinaryResource();
        cloudinaryResource.setPublicId((String) uploadResult.get("public_id"));
        cloudinaryResource.setUrlImagen((String) uploadResult.get("url"));
        cloudinaryResourceRepository.save(cloudinaryResource);

        // Crear el thumbnail
        ThumbnailItem thumbnail = new ThumbnailItem();
        thumbnail.setItemOrden(itemOrden); // Establecer la entidad, no el ID
        thumbnail.setItemCarrito(null); // Establecer explícitamente a NULL para cumplir con la restricción CHECK
        thumbnail.setTipoAngulo(tipoAngulo);
        thumbnail.setCloudinaryResource(cloudinaryResource);

        thumbnailItemRepository.save(thumbnail);

        return dtoConverterService.convertToThumbnailItemDTO(thumbnail);
    }

    /**
     * Actualiza un thumbnail existente por URL (sin subir archivo)
     */
    @Transactional
    public ThumbnailItemDTO updateThumbnailByUrl(ThumbnailUploadRequestDTO requestDTO) {
        ThumbnailItem thumbnail;

        // Determinar si es para ítem de carrito o ítem de orden
        if (requestDTO.getItemCarritoId() != null) {
            // Verificar que el ítem existe
            var itemCarrito = itemCarritoRepository.findById(requestDTO.getItemCarritoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            ERROR_ITEM_CARRITO_NO_ENCONTRADO + requestDTO.getItemCarritoId()));

            // Buscar o crear nuevo thumbnail
            thumbnail = thumbnailItemRepository
                    .findByItemCarritoIdAndTipoAnguloId(requestDTO.getItemCarritoId(), requestDTO.getTipoAnguloId())
                    .orElse(new ThumbnailItem());

            thumbnail.setItemCarrito(itemCarrito); // Usar la entidad en lugar del ID
            thumbnail.setItemOrden(null); // Establecer explícitamente a NULL para cumplir con la restricción CHECK
        } else if (requestDTO.getItemOrdenId() != null) {
            // Verificar que el ítem existe
            var itemOrden = itemOrdenRepository.findById(requestDTO.getItemOrdenId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            ERROR_ITEM_ORDEN_NO_ENCONTRADO + requestDTO.getItemOrdenId()));

            // Buscar o crear nuevo thumbnail
            thumbnail = thumbnailItemRepository
                    .findByItemOrdenIdAndTipoAnguloId(requestDTO.getItemOrdenId(), requestDTO.getTipoAnguloId())
                    .orElse(new ThumbnailItem());

            thumbnail.setItemOrden(itemOrden); // Usar la entidad en lugar del ID
            thumbnail.setItemCarrito(null); // Establecer explícitamente a NULL para cumplir con la restricción CHECK
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe especificar itemCarritoId o itemOrdenId");
        }

        // Verificar que el tipo de ángulo existe
        TipoAngulo tipoAngulo = tipoAnguloRepository.findById(requestDTO.getTipoAnguloId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ERROR_TIPO_ANGULO_NO_ENCONTRADO + requestDTO.getTipoAnguloId()));

        thumbnail.setTipoAngulo(tipoAngulo);

        // Si ya tiene un recurso Cloudinary y se proporciona una nueva URL, actualizar
        if (requestDTO.getUrl() != null) {
            CloudinaryResource cloudinaryResource;
            if (thumbnail.getCloudinaryResource() != null) {
                cloudinaryResource = thumbnail.getCloudinaryResource();
                cloudinaryResource.setUrlImagen(requestDTO.getUrl());

                // Si se proporciona un nuevo publicId, actualizarlo también
                if (requestDTO.getPublicId() != null) {
                    cloudinaryResource.setPublicId(requestDTO.getPublicId());
                }
            } else {
                cloudinaryResource = new CloudinaryResource();
                cloudinaryResource.setUrlImagen(requestDTO.getUrl());
                cloudinaryResource.setPublicId(requestDTO.getPublicId() != null ? requestDTO.getPublicId()
                        : "manual_" + System.currentTimeMillis());
            }

            cloudinaryResourceRepository.save(cloudinaryResource);
            thumbnail.setCloudinaryResource(cloudinaryResource);
        }

        thumbnailItemRepository.save(thumbnail);

        return dtoConverterService.convertToThumbnailItemDTO(thumbnail);
    }

    /**
     * Elimina un thumbnail
     */
    @Transactional
    public void deleteThumbnail(Long thumbnailId) {
        ThumbnailItem thumbnail = thumbnailItemRepository.findById(thumbnailId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ERROR_THUMBNAIL_NO_ENCONTRADO + thumbnailId));

        // Eliminar el recurso de Cloudinary si existe
        if (thumbnail.getCloudinaryResource() != null) {
            try {
                cloudinaryService.eliminarImagen(thumbnail.getCloudinaryResource().getPublicId());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY, e);
            }
            cloudinaryResourceRepository.delete(thumbnail.getCloudinaryResource());
        }

        thumbnailItemRepository.delete(thumbnail);
    }

    /**
     * Elimina todos los thumbnails de un ítem de carrito
     */
    @Transactional
    public void deleteThumbnailsByItemCarritoId(Long itemCarritoId) {
        List<ThumbnailItem> thumbnails = thumbnailItemRepository.findByItemCarritoId(itemCarritoId);
        thumbnailItemRepository.deleteByItemCarritoId(itemCarritoId);
        // Eliminar los recursos de Cloudinary asociados
        for (ThumbnailItem thumbnail : thumbnails) {
            if (thumbnail.getCloudinaryResource() != null) {
                try {
                    cloudinaryService.eliminarImagen(thumbnail.getCloudinaryResource().getPublicId());
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY, e);
                }
                cloudinaryResourceRepository.delete(thumbnail.getCloudinaryResource());
            }
        }

    }

    /**
     * Elimina todos los thumbnails de un ítem de orden
     */
    @Transactional
    public void deleteThumbnailsByItemOrdenId(Long itemOrdenId) {
        List<ThumbnailItem> thumbnails = thumbnailItemRepository.findByItemOrdenId(itemOrdenId);
        thumbnailItemRepository.deleteByItemOrdenId(itemOrdenId);
        // Eliminar los recursos de Cloudinary asociados primero
        for (ThumbnailItem thumbnail : thumbnails) {
            if (thumbnail.getCloudinaryResource() != null) {
                try {
                    cloudinaryService.eliminarImagen(thumbnail.getCloudinaryResource().getPublicId());
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CLOUDINARY, e);
                }
                cloudinaryResourceRepository.delete(thumbnail.getCloudinaryResource());
            }
        }
    }
}
