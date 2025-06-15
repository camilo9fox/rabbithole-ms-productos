package com.rabbithole.productos.service.impl;

import com.rabbithole.productos.model.ElementoTexto;
import com.rabbithole.productos.repository.ElementoTextoRepository;
import com.rabbithole.productos.service.ElementoTextoService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio ElementoTextoService para gestionar operaciones con elementos de texto.
 */
@Service
@Slf4j
@Transactional
public class ElementoTextoServiceImpl implements ElementoTextoService {
    
    private final ElementoTextoRepository elementoTextoRepository;
    
    @Autowired
    public ElementoTextoServiceImpl(ElementoTextoRepository elementoTextoRepository) {
        this.elementoTextoRepository = elementoTextoRepository;
    }
    
    @Override
    public ElementoTexto save(ElementoTexto elemento) {
        log.debug("Guardando ElementoTexto: {}", elemento);
        
        // Si la posición es nula, crear una nueva
        if (elemento.getPosicion() == null) {
            elemento.setPosicionX(elemento.getPosicionX() != null ? elemento.getPosicionX() : 0);
            elemento.setPosicionY(elemento.getPosicionY() != null ? elemento.getPosicionY() : 0);
        }
        
        return elementoTextoRepository.save(elemento);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ElementoTexto> findById(Long id) {
        log.debug("Buscando ElementoTexto por id: {}", id);
        return elementoTextoRepository.findById(id);
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("Eliminando ElementoTexto con id: {}", id);
        elementoTextoRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ElementoTexto> findAll() {
        log.debug("Obteniendo todos los ElementoTexto");
        return elementoTextoRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ElementoTexto> findAll(Pageable pageable) {
        log.debug("Obteniendo página de ElementoTexto");
        return elementoTextoRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ElementoTexto> findByElementoId(Long elementoId) {
        log.debug("Buscando ElementoTexto por elementoId: {}", elementoId);
        return elementoTextoRepository.findByElementoId(elementoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ElementoTexto> getElementosByFuente(String fuente) {
        log.debug("Buscando ElementoTexto por fuente: {}", fuente);
        return elementoTextoRepository.findByFuente(fuente);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ElementoTexto> getElementsByTamanoFuente(Integer tamanoFuente) {
        log.debug("Buscando ElementoTexto por tamaño de fuente: {}", tamanoFuente);
        return elementoTextoRepository.findByTamanoFuente(tamanoFuente);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ElementoTexto> searchElementosByTexto(String texto, Pageable pageable) {
        log.debug("Buscando ElementoTexto que contengan texto: {}", texto);
        return elementoTextoRepository.findByTextoContainingIgnoreCase(texto, pageable);
    }
}
