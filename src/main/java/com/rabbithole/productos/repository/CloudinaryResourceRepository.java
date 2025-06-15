package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.CloudinaryResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad CloudinaryResource.
 * Proporciona operaciones CRUD básicas para la gestión de recursos de Cloudinary.
 */
@Repository
public interface CloudinaryResourceRepository extends JpaRepository<CloudinaryResource, Long> {
    
    /**
     * Encuentra un recurso por su publicId
     * 
     * @param publicId El identificador público de Cloudinary
     * @return Recurso de Cloudinary
     */
    CloudinaryResource findByPublicId(String publicId);
}
