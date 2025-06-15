package com.rabbithole.productos.service;

import com.rabbithole.productos.model.CloudinaryResource;
import com.rabbithole.productos.repository.CloudinaryResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Servicio para gestionar recursos de Cloudinary.
 * Proporciona métodos para crear, actualizar y eliminar recursos de imágenes.
 */
@Service
public class CloudinaryResourceService {

    private final CloudinaryResourceRepository cloudinaryResourceRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public CloudinaryResourceService(CloudinaryResourceRepository cloudinaryResourceRepository,
                                    CloudinaryService cloudinaryService) {
        this.cloudinaryResourceRepository = cloudinaryResourceRepository;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Crea un nuevo recurso Cloudinary con la información proporcionada
     *
     * @param url URL de la imagen
     * @param publicId ID público de Cloudinary
     * @param tipoImagen Tipo MIME de la imagen
     * @param nombreArchivo Nombre del archivo original
     * @param tamanoArchivo Tamaño del archivo en bytes
     * @return El recurso Cloudinary creado y guardado
     */
    @Transactional
    public CloudinaryResource crearRecurso(String url, String publicId, String tipoImagen,
                                          String nombreArchivo, Long tamanoArchivo) {
        CloudinaryResource recurso = new CloudinaryResource();
        recurso.setUrlImagen(url);
        recurso.setPublicId(publicId);
        recurso.setTipoImagen(tipoImagen);
        recurso.setNombreArchivo(nombreArchivo);
        recurso.setTamanoArchivo(tamanoArchivo);
        recurso.setCreadoEn(LocalDateTime.now());
        
        return cloudinaryResourceRepository.save(recurso);
    }

    /**
     * Crea un nuevo recurso Cloudinary a partir de un Map de resultado de la API de Cloudinary
     *
     * @param uploadResult Resultado de subida a Cloudinary
     * @return El recurso Cloudinary creado y guardado
     */
    @Transactional
    public CloudinaryResource crearRecursoDesdeUpload(Map uploadResult) {
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");
        String formatoArchivo = (String) uploadResult.get("format");
        String tipoImagen = "image/" + formatoArchivo;
        String nombreOriginal = (String) uploadResult.get("original_filename") + "." + formatoArchivo;
        Long tamano = Long.valueOf(uploadResult.get("bytes").toString());
        
        return crearRecurso(url, publicId, tipoImagen, nombreOriginal, tamano);
    }

    /**
     * Obtiene un recurso Cloudinary por su ID
     *
     * @param id ID del recurso
     * @return El recurso Cloudinary o null si no existe
     */
    public CloudinaryResource obtenerRecursoPorId(Long id) {
        return cloudinaryResourceRepository.findById(id).orElse(null);
    }

    /**
     * Obtiene un recurso Cloudinary por su publicId
     *
     * @param publicId ID público de Cloudinary
     * @return El recurso Cloudinary o null si no existe
     */
    public CloudinaryResource obtenerRecursoPorPublicId(String publicId) {
        return cloudinaryResourceRepository.findByPublicId(publicId);
    }

    /**
     * Elimina un recurso Cloudinary y la imagen asociada en Cloudinary
     *
     * @param recurso El recurso a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    @Transactional
    public boolean eliminarRecurso(CloudinaryResource recurso) {
        if (recurso != null) {
            try {
                // Primero eliminar imagen de Cloudinary
                cloudinaryService.eliminarImagen(recurso.getPublicId());
                // Luego eliminar el registro en la base de datos
                cloudinaryResourceRepository.delete(recurso);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Elimina un recurso Cloudinary por su ID
     *
     * @param id ID del recurso
     * @return true si se eliminó correctamente, false en caso contrario
     */
    @Transactional
    public boolean eliminarRecursoPorId(Long id) {
        CloudinaryResource recurso = obtenerRecursoPorId(id);
        return eliminarRecurso(recurso);
    }
}
