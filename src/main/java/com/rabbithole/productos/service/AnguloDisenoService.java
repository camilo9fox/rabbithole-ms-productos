package com.rabbithole.productos.service;

import com.rabbithole.productos.model.AnguloDiseno;
import com.rabbithole.productos.model.TipoAngulo;
import com.rabbithole.productos.repository.AnguloDisenoRepository;
import com.rabbithole.productos.repository.DisenoPersonalizadoRepository;
import com.rabbithole.productos.repository.TipoAnguloRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar ángulos de diseños personalizados.
 * Implementa operaciones CRUD y lógica de negocio específica para la entidad AnguloDiseno.
 */
@Service
@Slf4j
@Transactional
public class AnguloDisenoService {

    private final AnguloDisenoRepository anguloDisenoRepository;
    private final DisenoPersonalizadoRepository disenoPersonalizadoRepository;
    private final TipoAnguloRepository tipoAnguloRepository;

    @Autowired
    public AnguloDisenoService(
            AnguloDisenoRepository anguloDisenoRepository,
            DisenoPersonalizadoRepository disenoPersonalizadoRepository,
            TipoAnguloRepository tipoAnguloRepository) {
        this.anguloDisenoRepository = anguloDisenoRepository;
        this.disenoPersonalizadoRepository = disenoPersonalizadoRepository;
        this.tipoAnguloRepository = tipoAnguloRepository;
    }

    /**
     * Obtiene todos los ángulos de un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @return Lista de ángulos del diseño
     */
    @Transactional(readOnly = true)
    public List<AnguloDiseno> getAngulosByDisenoId(Long disenoId) {
        log.debug("Obteniendo ángulos para el diseño ID: {}", disenoId);
        return anguloDisenoRepository.findByDisenoPersonalizadoId(disenoId);
    }

    /**
     * Obtiene todos los ángulos de un diseño personalizado con paginación.
     *
     * @param disenoId ID del diseño personalizado
     * @param pageable Información de paginación
     * @return Página de ángulos del diseño
     */
    @Transactional(readOnly = true)
    public Page<AnguloDiseno> getAngulosByDisenoId(Long disenoId, Pageable pageable) {
        log.debug("Obteniendo ángulos paginados para el diseño ID: {}", disenoId);
        return anguloDisenoRepository.findByDisenoPersonalizadoId(disenoId, pageable);
    }

    /**
     * Obtiene un ángulo específico de un diseño.
     *
     * @param disenoId ID del diseño personalizado
     * @param anguloId ID del ángulo
     * @return Ángulo encontrado o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<AnguloDiseno> getAnguloDisenoById(Long disenoId, Long anguloId) {
        log.debug("Obteniendo ángulo ID: {} para diseño ID: {}", anguloId, disenoId);
        Optional<AnguloDiseno> anguloOpt = anguloDisenoRepository.findById(anguloId);
        
        if (anguloOpt.isPresent() && anguloOpt.get().getDisenoPersonalizado().getId().equals(disenoId)) {
            return anguloOpt;
        }
        
        return Optional.empty();
    }

    /**
     * Crea un nuevo ángulo para un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @param angulo Datos del nuevo ángulo
     * @return Ángulo creado o vacío si no se pudo crear
     */
    public Optional<AnguloDiseno> createAnguloDiseno(Long disenoId, AnguloDiseno angulo) {
        log.debug("Creando nuevo ángulo para diseño ID: {}", disenoId);
        
        return disenoPersonalizadoRepository.findById(disenoId).map(diseno -> {
            // Vincular el ángulo al diseño
            angulo.setDisenoPersonalizado(diseno);
            
            // Ya no se establece fecha de creación, no existe en el esquema
            
            // Validar tipo de ángulo
            if (angulo.getTipoAngulo() == null && angulo.getTipoAngulo().getId() != null) {
                Optional<TipoAngulo> tipoAngulo = tipoAnguloRepository.findById(angulo.getTipoAngulo().getId());
                if (tipoAngulo.isPresent()) {
                    angulo.setTipoAngulo(tipoAngulo.get());
                } else {
                    // No se encontró el tipo de ángulo, usar tipo por defecto (ID=1 -> FRONTAL)
                    tipoAnguloRepository.findById(1L)
                        .ifPresent(angulo::setTipoAngulo);
                }
            }
            
            // Guardar el nuevo ángulo
            return anguloDisenoRepository.save(angulo);
        });
    }

    /**
     * Actualiza un ángulo existente de un diseño personalizado.
     *
     * @param anguloId ID del ángulo
     * @param anguloActualizado Datos actualizados
     * @return Ángulo actualizado o vacío si no existe
     */
    public Optional<AnguloDiseno> updateAnguloDiseno(Long anguloId, AnguloDiseno anguloActualizado) {
        log.debug("Actualizando ángulo ID: {}", anguloId);
        
        return anguloDisenoRepository.findById(anguloId).map(anguloExistente -> {
            // Mantener el ID y el diseño original
            anguloActualizado.setId(anguloId);
            anguloActualizado.setDisenoPersonalizado(anguloExistente.getDisenoPersonalizado());
            
            // Ya no hay campo de fecha de creación en la entidad
            
            // Validar tipo de ángulo
            if (anguloActualizado.getTipoAngulo() != null && anguloActualizado.getTipoAngulo().getId() != null) {
                Optional<TipoAngulo> tipoAngulo = tipoAnguloRepository.findById(anguloActualizado.getTipoAngulo().getId());
                if (tipoAngulo.isPresent()) {
                    anguloActualizado.setTipoAngulo(tipoAngulo.get());
                } else {
                    // Mantener el tipo de ángulo original si no se encuentra el nuevo
                    anguloActualizado.setTipoAngulo(anguloExistente.getTipoAngulo());
                }
            } else {
                // Mantener el tipo de ángulo original
                anguloActualizado.setTipoAngulo(anguloExistente.getTipoAngulo());
            }
            
            // Guardar el ángulo actualizado
            return anguloDisenoRepository.save(anguloActualizado);
        });
    }

    /**
     * Elimina un ángulo de un diseño personalizado.
     *
     * @param anguloId ID del ángulo
     * @return true si se eliminó correctamente, false si no existe
     */
    public boolean deleteAnguloDiseno(Long anguloId) {
        log.debug("Eliminando ángulo ID: {}", anguloId);
        
        if (anguloDisenoRepository.existsById(anguloId)) {
            anguloDisenoRepository.deleteById(anguloId);
            return true;
        }
        return false;
    }

    /**
     * Obtiene ángulos por tipo para un diseño específico.
     *
     * @param disenoId ID del diseño personalizado
     * @param tipoAnguloId ID del tipo de ángulo
     * @return Lista de ángulos del tipo especificado en el diseño
     */
    @Transactional(readOnly = true)
    public List<AnguloDiseno> getAngulosByTipo(Long disenoId, Long tipoAnguloId) {
        log.debug("Obteniendo ángulos de tipo ID: {} para diseño ID: {}", tipoAnguloId, disenoId);
        return anguloDisenoRepository.findByDisenoPersonalizadoIdAndTipoAnguloId(disenoId, tipoAnguloId);
    }
}
