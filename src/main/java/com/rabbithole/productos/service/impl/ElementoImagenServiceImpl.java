package com.rabbithole.productos.service.impl;

import com.rabbithole.productos.exception.ImageProcessingException;
import com.rabbithole.productos.model.CloudinaryResource;
import com.rabbithole.productos.model.ElementoImagen;
import com.rabbithole.productos.repository.ElementoImagenRepository;
import com.rabbithole.productos.service.CloudinaryService;
import com.rabbithole.productos.service.ElementoImagenService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio ElementoImagenService para gestionar elementos de imagen.
 */
@Service
@Slf4j
@Transactional
public class ElementoImagenServiceImpl implements ElementoImagenService {
    
    private final ElementoImagenRepository elementoImagenRepository;
    private final CloudinaryService cloudinaryService;
    
    @Autowired
    public ElementoImagenServiceImpl(
            ElementoImagenRepository elementoImagenRepository,
            CloudinaryService cloudinaryService) {
        this.elementoImagenRepository = elementoImagenRepository;
        this.cloudinaryService = cloudinaryService;
    }
    
    @Override
    public ElementoImagen save(ElementoImagen elementoImagen) {
        log.debug("Guardando elemento de imagen: {}", elementoImagen);
        return elementoImagenRepository.save(elementoImagen);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ElementoImagen> findById(Long id) {
        log.debug("Buscando elemento de imagen con ID: {}", id);
        return elementoImagenRepository.findById(id);
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("Eliminando elemento de imagen con ID: {}", id);
        elementoImagenRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ElementoImagen> findAll() {
        log.debug("Obteniendo todos los elementos de imagen");
        return elementoImagenRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ElementoImagen> findAll(Pageable pageable) {
        log.debug("Obteniendo página de elementos de imagen");
        return elementoImagenRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ElementoImagen> findByElementoId(Long elementoId) {
        log.debug("Buscando elementos de imagen por elementoId: {}", elementoId);
        return elementoImagenRepository.findByElementoId(elementoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ElementoImagen> findByElementoId(Long elementoId, Pageable pageable) {
        log.debug("Buscando elementos de imagen por elementoId: {} con paginación", elementoId);
        return elementoImagenRepository.findByElementoId(elementoId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ElementoImagen> getElementByIdAndElementoId(Long id, Long elementoId) {
        log.debug("Buscando elemento de imagen por id: {} y elementoId: {}", id, elementoId);
        return elementoImagenRepository.findByIdAndElementoId(id, elementoId);
    }
    
    @Override
    public Optional<ElementoImagen> createElement(Long elementoId, ElementoImagen elementoImagen) {
        log.debug("Creando elemento de imagen para elemento ID: {}", elementoId);
        elementoImagen.setCreadoEn(LocalDateTime.now());
        return Optional.of(elementoImagenRepository.save(elementoImagen));
    }
    
    @Override
    public Optional<ElementoImagen> updateElement(Long id, ElementoImagen elementoImagen) {
        log.debug("Actualizando elemento de imagen con id: {}", id);
        
        Optional<ElementoImagen> existingElemento = elementoImagenRepository.findById(id);
        if (existingElemento.isEmpty()) {
            return Optional.empty();
        }
        
        ElementoImagen elemento = existingElemento.get();
        // Actualizar propiedades manteniendo el ID
        elementoImagen.setId(id);
        
        // Si el cliente no envía un CloudinaryResource, mantener el existente
        if (elementoImagen.getCloudinaryResource() == null) {
            elementoImagen.setCloudinaryResource(elemento.getCloudinaryResource());
        }
        
        return Optional.of(elementoImagenRepository.save(elementoImagen));
    }
    
    @Override
    public List<ElementoImagen> getElementsByTipoImagen(Long anguloId, String tipoImagen) {
        log.debug("Buscando elementos de imagen por tipo: {} (ignorando anguloId: {})", tipoImagen, anguloId);
        return elementoImagenRepository.findByTipoImagen(tipoImagen);
    }
    
    @Override
    public Page<ElementoImagen> searchElementosByNombreArchivo(Long anguloId, String nombreArchivo, Pageable pageable) {
        log.debug("Buscando elementos de imagen por nombre de archivo: {} (ignorando anguloId: {})", nombreArchivo, anguloId);
        return elementoImagenRepository.findByNombreArchivoContainingIgnoreCase(nombreArchivo, pageable);
    }
    
    @Override
    public List<ElementoImagen> getElementsByTamanoMayorQue(Long anguloId, Long tamanoMinimo) {
        log.debug("Buscando elementos de imagen con tamaño mayor que: {} (ignorando anguloId: {})", tamanoMinimo, anguloId);
        return elementoImagenRepository.findByTamanoArchivoGreaterThan(tamanoMinimo);
    }
    
    @Override
    public List<ElementoImagen> getElementsByTamanoMenorQue(Long anguloId, Long tamanoMaximo) {
        log.debug("Buscando elementos de imagen con tamaño menor que: {} (ignorando anguloId: {})", tamanoMaximo, anguloId);
        return elementoImagenRepository.findByTamanoArchivoLessThan(tamanoMaximo);
    }
    
    @Override
    public List<ElementoImagen> getElementsByAnguloId(Long anguloId) {
        log.debug("Obteniendo elementos de imagen (usando anguloId: {} como elementoId)", anguloId);
        return elementoImagenRepository.findAll();
    }
    
    @Override
    public Page<ElementoImagen> getElementsByAnguloId(Long anguloId, Pageable pageable) {
        log.debug("Obteniendo página de elementos de imagen (ignorando anguloId: {})", anguloId);
        return elementoImagenRepository.findAll(pageable);
    }
    
    @Override
    public Optional<ElementoImagen> getElementByIdAndAnguloId(Long elementoId, Long anguloId) {
        log.debug("Buscando elemento de imagen ID: {} (ignorando anguloId: {})", elementoId, anguloId);
        return elementoImagenRepository.findById(elementoId);
    }
    
    @Override
    @Transactional
    public ElementoImagen uploadAndCreateElementoImagen(Long anguloId, MultipartFile file) throws ImageProcessingException {
        log.debug("Subiendo y creando elemento de imagen (ignorando anguloId: {})", anguloId);
        
        try {
            // Subir la imagen a Cloudinary
            Map<String, Object> result = cloudinaryService.uploadElementoImagen(file);
            
            // Crear nuevo elemento de imagen
            ElementoImagen elementoImagen = new ElementoImagen();
            elementoImagen.setCreadoEn(LocalDateTime.now());
            
            // Crear y configurar un nuevo recurso de Cloudinary
            CloudinaryResource cloudinaryResource = new CloudinaryResource();
            cloudinaryResource.setUrlImagen((String) result.get("secure_url"));
            cloudinaryResource.setPublicId((String) result.get("public_id"));
            cloudinaryResource.setTipoImagen(file.getContentType());
            cloudinaryResource.setNombreArchivo(file.getOriginalFilename());
            cloudinaryResource.setTamanoArchivo(file.getSize());
            
            // Asociar el recurso al elemento
            elementoImagen.setCloudinaryResource(cloudinaryResource);
            
            // Guardar y retornar
            return elementoImagenRepository.save(elementoImagen);
            
        } catch (IOException e) {
            throw new ImageProcessingException("Error al procesar imagen", e);
        }
    }
    
    @Override
    @Transactional
    public ElementoImagen updateElementoImagen(Long elementoId, MultipartFile file) throws ImageProcessingException {
        log.debug("Actualizando elemento de imagen ID: {} con nueva imagen", elementoId);
        
        // Buscar el elemento existente
        Optional<ElementoImagen> elementoOpt = elementoImagenRepository.findById(elementoId);
        if (elementoOpt.isEmpty()) {
            throw new IllegalArgumentException("Elemento de imagen no encontrado con ID: " + elementoId);
        }
        
        ElementoImagen elemento = elementoOpt.get();
        
        // Eliminar el recurso anterior de Cloudinary si existe
        if (elemento.getCloudinaryResource() != null) {
            String publicId = elemento.getCloudinaryResource().getPublicId();
            if (publicId != null && !publicId.isEmpty()) {
                try {
                    cloudinaryService.eliminarImagen(publicId);
                    log.info("Imagen anterior eliminada de Cloudinary: {}", publicId);
                } catch (IOException | ImageProcessingException e) {
                    log.warn("No se pudo eliminar la imagen anterior: {}", publicId, e);
                    // Continuamos con la actualización aunque falle la eliminación
                }
            }
        }
        
        // Subir la nueva imagen a Cloudinary
        Map<String, Object> result;
        try {
            result = cloudinaryService.uploadElementoImagen(file);
        } catch (IOException e) {
            throw new ImageProcessingException("Error al subir imagen a Cloudinary", e);
        }
        
        // Extraemos valores comunes
        final String secureUrl = (String) result.get("secure_url");
        final String publicId = (String) result.get("public_id");
        
        if (elemento.getCloudinaryResource() == null) {
            // Crear nuevo CloudinaryResource si no existe
            CloudinaryResource cloudinaryResource = new CloudinaryResource();
            cloudinaryResource.setUrlImagen(secureUrl);
            cloudinaryResource.setPublicId(publicId);
            cloudinaryResource.setTipoImagen(file.getContentType());
            cloudinaryResource.setNombreArchivo(file.getOriginalFilename());
            cloudinaryResource.setTamanoArchivo(file.getSize());
            elemento.setCloudinaryResource(cloudinaryResource);
        } else {
            // Actualizar CloudinaryResource existente
            CloudinaryResource cloudinaryResource = elemento.getCloudinaryResource();
            cloudinaryResource.setUrlImagen(secureUrl);
            cloudinaryResource.setPublicId(publicId);
            cloudinaryResource.setTipoImagen(file.getContentType());
            cloudinaryResource.setNombreArchivo(file.getOriginalFilename());
            cloudinaryResource.setTamanoArchivo(file.getSize());
        }
        
        // Guardar y retornar
        return elementoImagenRepository.save(elemento);
    }
    
    @Override
    @Transactional
    public boolean deleteElement(Long elementoId) {
        log.debug("Eliminando elemento de imagen ID: {} y su archivo en Cloudinary", elementoId);
        
        // Buscar el elemento
        Optional<ElementoImagen> elementoOpt = elementoImagenRepository.findById(elementoId);
        if (elementoOpt.isEmpty()) {
            return false;
        }
        
        ElementoImagen elemento = elementoOpt.get();
        
        // Eliminar la imagen de Cloudinary si existe un CloudinaryResource asociado
        CloudinaryResource cloudinaryResource = elemento.getCloudinaryResource();
        if (cloudinaryResource != null && cloudinaryResource.getPublicId() != null && !cloudinaryResource.getPublicId().isEmpty()) {
            try {
                cloudinaryService.eliminarImagen(cloudinaryResource.getPublicId());
                log.info("Imagen eliminada de Cloudinary: {}", cloudinaryResource.getPublicId());
            } catch (IOException | ImageProcessingException e) {
                log.error("Error al eliminar imagen de Cloudinary: {}", cloudinaryResource.getPublicId(), e);
                // Continuamos con la eliminación del elemento aunque falle la eliminación de la imagen
            }
        }
        
        // Eliminar el elemento de la base de datos (esto removerá también el CloudinaryResource por la cascada)
        elementoImagenRepository.delete(elemento);
        return true;
    }
    
    // El método verifyAnguloExists ha sido eliminado ya que ya no es necesario
}
