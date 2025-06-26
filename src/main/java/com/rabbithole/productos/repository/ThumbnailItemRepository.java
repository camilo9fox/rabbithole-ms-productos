package com.rabbithole.productos.repository;

import com.rabbithole.productos.model.ThumbnailItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThumbnailItemRepository extends JpaRepository<ThumbnailItem, Long> {
    
    List<ThumbnailItem> findByItemCarritoId(Long itemCarritoId);
    
    List<ThumbnailItem> findByItemOrdenId(Long itemOrdenId);
    
    Optional<ThumbnailItem> findByItemCarritoIdAndTipoAnguloId(Long itemCarritoId, Long tipoAnguloId);
    
    Optional<ThumbnailItem> findByItemOrdenIdAndTipoAnguloId(Long itemOrdenId, Long tipoAnguloId);
    
    boolean existsByCloudinaryResourceId(Long cloudinaryResourceId);
    
    void deleteByItemCarritoId(Long itemCarritoId);
    
    void deleteByItemOrdenId(Long itemOrdenId);
}
