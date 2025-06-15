package com.rabbithole.productos.service;

import com.rabbithole.productos.model.DisenoPersonalizado;
import com.rabbithole.productos.model.EstadoDiseno;
import com.rabbithole.productos.repository.DisenoPersonalizadoRepository;
import com.rabbithole.productos.repository.EstadoDisenoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para gestionar diseños personalizados.
 * Implementa operaciones CRUD y lógica de negocio específica para la entidad DisenoPersonalizado.
 */
@Service
@Slf4j
@Transactional
public class DisenoPersonalizadoService {

    private final DisenoPersonalizadoRepository disenoPersonalizadoRepository;
    private final EstadoDisenoRepository estadoDisenoRepository;

    @Autowired
    public DisenoPersonalizadoService(
            DisenoPersonalizadoRepository disenoPersonalizadoRepository,
            EstadoDisenoRepository estadoDisenoRepository) {
        this.disenoPersonalizadoRepository = disenoPersonalizadoRepository;
        this.estadoDisenoRepository = estadoDisenoRepository;
    }

    /**
     * Obtiene todos los diseños personalizados con paginación.
     *
     * @param pageable Información de paginación
     * @return Página de diseños personalizados
     */
    @Transactional(readOnly = true)
    public Page<DisenoPersonalizado> getAllDisenosPersonalizados(Pageable pageable) {
        log.debug("Obteniendo todos los diseños personalizados paginados");
        return disenoPersonalizadoRepository.findAll(pageable);
    }

    /**
     * Obtiene un diseño personalizado por su ID.
     *
     * @param id ID del diseño personalizado
     * @return Diseño personalizado encontrado o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<DisenoPersonalizado> getDisenoPersonalizadoById(Long id) {
        log.debug("Obteniendo diseño personalizado con ID: {}", id);
        return disenoPersonalizadoRepository.findById(id);
    }

    /**
     * Crea un nuevo diseño personalizado.
     * 
     * @param diseno Diseño personalizado a crear
     * @return Diseño personalizado creado
     */
    public DisenoPersonalizado createDisenoPersonalizado(DisenoPersonalizado diseno) {
        log.debug("Creando nuevo diseño personalizado: {}", diseno);
        
        // Asegurarnos de que las fechas están correctamente establecidas
        LocalDateTime ahora = LocalDateTime.now();
        if (diseno.getCreadoEn() == null) {
            diseno.setCreadoEn(ahora);
        }
        diseno.setActualizadoEn(ahora);
        
        // Si no se especifica un estado, asignar uno por defecto (pendiente)
        if (diseno.getEstado() == null) {
            // Intentar buscar por código "PENDIENTE" primero
            estadoDisenoRepository.findByCodigo("PENDIENTE")
                .or(() -> estadoDisenoRepository.findById(1L))
                .ifPresentOrElse(
                    diseno::setEstado,
                    () -> {
                        log.warn("No se encontró un estado predeterminado para el diseño. Creando uno nuevo.");
                        EstadoDiseno estadoPorDefecto = new EstadoDiseno();
                        estadoPorDefecto.setCodigo("PENDIENTE");
                        estadoPorDefecto.setNombre("Pendiente");
                        diseno.setEstado(estadoDisenoRepository.save(estadoPorDefecto));
                    }
                );
        }
        
        // El diseño se considera activo por defecto (no hay campo activo en esta entidad)
        
        return disenoPersonalizadoRepository.save(diseno);
    }

    /**
     * Actualiza un diseño personalizado existente.
     *
     * @param id ID del diseño personalizado
     * @param disenoActualizado Datos actualizados
     * @return Diseño personalizado actualizado o vacío si no existe
     */
    public Optional<DisenoPersonalizado> updateDisenoPersonalizado(Long id, DisenoPersonalizado disenoActualizado) {
        log.debug("Actualizando diseño personalizado ID: {}", id);
        
        return disenoPersonalizadoRepository.findById(id).map(disenoExistente -> {
            // Mantener el ID original
            disenoActualizado.setId(id);
            
            // Mantener la fecha de creación original
            disenoActualizado.setCreadoEn(disenoExistente.getCreadoEn());
            
            // Actualizar la fecha de modificación
            disenoActualizado.setActualizadoEn(LocalDateTime.now());
            
            // Guardar el diseño actualizado
            return disenoPersonalizadoRepository.save(disenoActualizado);
        });
    }

    /**
     * Elimina un diseño personalizado por su ID.
     *
     * @param id ID del diseño personalizado
     * @return true si se eliminó correctamente, false si no existe
     */
    public boolean deleteDisenoPersonalizado(Long id) {
        log.debug("Eliminando diseño personalizado ID: {}", id);
        
        if (disenoPersonalizadoRepository.existsById(id)) {
            disenoPersonalizadoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Obtiene diseños personalizados de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @param pageable Información de paginación
     * @return Página de diseños personalizados del usuario
     */
    @Transactional(readOnly = true)
    public Page<DisenoPersonalizado> getDisenosPorUsuario(Long usuarioId, Pageable pageable) {
        log.debug("Obteniendo diseños personalizados para usuario ID: {}", usuarioId);
        return disenoPersonalizadoRepository.findByUsuarioId(usuarioId, pageable);
    }

    /**
     * Obtiene diseños personalizados públicos.
     * NOTA: Esta funcionalidad está temporalmente modificada para devolver todos los diseños hasta que
     * se implemente el campo 'publico' en la entidad.
     *
     * @param pageable Información de paginación
     * @return Página de diseños personalizados (todos, ya que no hay campo público)
     * @deprecated El campo 'publico' no existe en la entidad DisenoPersonalizado.
     * @since 1.0
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public Page<DisenoPersonalizado> getDiseniosPublicos(Pageable pageable) {
        log.debug("Obteniendo diseños personalizados (todos, ya que no existe el campo 'público')");
        log.warn("El campo 'publico' no existe en la entidad DisenoPersonalizado. Retornando todos los diseños.");
        // Retornamos todos los diseños ya que no hay filtro de público
        return disenoPersonalizadoRepository.findAll(pageable);
    }

    /**
     * Cambia el estado de un diseño personalizado.
     *
     * @param disenoId ID del diseño personalizado
     * @param estadoId ID del nuevo estado
     * @return Diseño personalizado actualizado o vacío si no existe
     */
    public Optional<DisenoPersonalizado> cambiarEstadoDiseno(Long disenoId, Long estadoId) {
        log.debug("Cambiando estado del diseño ID: {} a estado ID: {}", disenoId, estadoId);
        
        Optional<DisenoPersonalizado> disenoOpt = disenoPersonalizadoRepository.findById(disenoId);
        Optional<EstadoDiseno> estadoOpt = estadoDisenoRepository.findById(estadoId);
        
        if (disenoOpt.isPresent() && estadoOpt.isPresent()) {
            DisenoPersonalizado diseno = disenoOpt.get();
            diseno.setEstado(estadoOpt.get());
            diseno.setActualizadoEn(LocalDateTime.now());
            return Optional.of(disenoPersonalizadoRepository.save(diseno));
        }
        
        return Optional.empty();
    }

    /**
     * Cambia la visibilidad pública de un diseño personalizado.
     * NOTA: Esta funcionalidad está temporalmente deshabilitada hasta que se agregue el campo 'publico' a la entidad.
     *
     * @param disenoId ID del diseño personalizado
     * @param esPublico Flag para marcar como público/privado
     * @return Diseño personalizado actualizado o vacío si no existe
     * @deprecated El campo 'publico' no está implementado en la entidad DisenoPersonalizado.
     * Implementar este campo en la entidad cuando se requiera esta funcionalidad.
     * @since 1.0
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public Optional<DisenoPersonalizado> cambiarVisibilidadDiseno(Long disenoId, boolean esPublico) {
        log.debug("Cambiando visibilidad del diseño ID: {} a público: {} - FUNCIONALIDAD NO IMPLEMENTADA", disenoId, esPublico);
        log.warn("El campo 'publico' no existe en la entidad DisenoPersonalizado. Se requiere modificar la entidad.");
        
        // Simplemente devolvemos el diseño sin modificar su visibilidad
        return disenoPersonalizadoRepository.findById(disenoId);
    }

    /**
     * Busca diseños personalizados por texto en nombre o descripción.
     *
     * @param texto Texto de búsqueda
     * @param pageable Información de paginación
     * @return Página de diseños personalizados que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public Page<DisenoPersonalizado> buscarDisenosPorTexto(String texto, Pageable pageable) {
        log.debug("Buscando diseños personalizados con texto: {}", texto);
        return disenoPersonalizadoRepository.buscarPorTexto(texto, pageable);
    }
}
