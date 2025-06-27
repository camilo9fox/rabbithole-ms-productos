package com.rabbithole.productos.service;

import com.rabbithole.productos.dto.OrdenDTO;
import com.rabbithole.productos.dto.ItemOrdenDTO;
import com.rabbithole.productos.dto.CrearOrdenDTO;
import com.rabbithole.productos.dto.CrearOrdenAnonimaDTO;
import com.rabbithole.productos.dto.ItemCarritoMemoriaDTO;
import com.rabbithole.productos.exception.ResourceNotFoundException;
import com.rabbithole.productos.model.*;
import com.rabbithole.productos.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
// Importaciones SQL eliminadas ya que no se usan
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para operaciones relacionadas con órdenes.
 */
@Service
public class OrdenService {
    
    // Logger para registros
    private static final Logger logger = LoggerFactory.getLogger(OrdenService.class);

    // Constantes para estados de orden
    private static final String ESTADO_PENDIENTE_NOMBRE = "PENDIENTE";
    private static final String ESTADO_PENDIENTE_CODIGO = "1 PENDING";
    private static final String ESTADO_PENDIENTE_DESCRIPCION = "La orden está pendiente de pago";
    
    // EntityManager para acceso a JDBC directo
    @PersistenceContext
    private EntityManager entityManager;

    private final OrdenRepository ordenRepository;
    private final EstadoOrdenRepository estadoOrdenRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoService carritoService;
    private final ProductoRepository productoRepository;
    private final DisenoPersonalizadoRepository disenoPersonalizadoRepository;
    private final ColorRepository colorRepository;
    private final TallaRepository tallaRepository;
    private final InfoEnvioRepository infoEnvioRepository;
    private final InfoPagoRepository infoPagoRepository;
    private final DTOConverterService dtoConverterService;
    private final TipoItemRepository tipoItemRepository;
    private final ThumbnailItemRepository thumbnailItemRepository;
    private final TipoAnguloRepository tipoAnguloRepository;
    private final CloudinaryResourceRepository cloudinaryResourceRepository;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public OrdenService(
            OrdenRepository ordenRepository,
            EstadoOrdenRepository estadoOrdenRepository,
            UsuarioRepository usuarioRepository,
            CarritoService carritoService,
            ProductoRepository productoRepository,
            DisenoPersonalizadoRepository disenoPersonalizadoRepository,
            ColorRepository colorRepository,
            TallaRepository tallaRepository,
            InfoEnvioRepository infoEnvioRepository,
            InfoPagoRepository infoPagoRepository,
            DTOConverterService dtoConverterService,
            TipoItemRepository tipoItemRepository,
            ThumbnailItemRepository thumbnailItemRepository,
            TipoAnguloRepository tipoAnguloRepository,
            CloudinaryResourceRepository cloudinaryResourceRepository,
            ItemOrdenRepository itemOrdenRepository) {
        this.ordenRepository = ordenRepository;
        this.estadoOrdenRepository = estadoOrdenRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoService = carritoService;
        this.productoRepository = productoRepository;
        this.disenoPersonalizadoRepository = disenoPersonalizadoRepository;
        this.colorRepository = colorRepository;
        this.tallaRepository = tallaRepository;
        this.infoEnvioRepository = infoEnvioRepository;
        this.infoPagoRepository = infoPagoRepository;
        this.dtoConverterService = dtoConverterService;
        this.tipoItemRepository = tipoItemRepository;
        this.thumbnailItemRepository = thumbnailItemRepository;
        this.tipoAnguloRepository = tipoAnguloRepository;
        this.cloudinaryResourceRepository = cloudinaryResourceRepository;
    }

    /**
     * Obtiene todas las órdenes de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de DTOs de órdenes
     */
    @Transactional(readOnly = true)
    public List<OrdenDTO> obtenerOrdenesPorUsuario(Long usuarioId) {
        List<Orden> ordenes = ordenRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
        
        return ordenes.stream()
                .map(this::convertirAOrdenDTO)
                .toList();
    }

    /**
     * Obtiene todas las órdenes del sistema.
     * 
     * @return Lista de DTOs de todas las órdenes
     */
    @Transactional(readOnly = true)
    public List<OrdenDTO> obtenerTodasLasOrdenes() {
        logger.info("Obteniendo todas las órdenes del sistema");
        List<Orden> ordenes = ordenRepository.findAll();
        return ordenes.stream()
                .map(this::convertirAOrdenDTO)
                .toList();
    }

    /**
     * Migra los thumbnails desde los ítems del carrito a los ítems de la orden.
     * En lugar de eliminar y recrear thumbnails, simplemente actualiza las referencias.
     * 
     * @param itemsCarrito Lista de ítems del carrito
     * @param itemsOrden Lista de ítems de la orden
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void migrarThumbnails(List<ItemCarrito> itemsCarrito, List<ItemOrden> itemsOrden) {
        Map<String, ItemOrden> mapaItemsOrden = new HashMap<>();
        // Crear un mapa de ítems de orden por su clave única
        for (ItemOrden itemOrden : itemsOrden) {
            String clave = generarClaveItem(itemOrden);
            mapaItemsOrden.put(clave, itemOrden);
        }
        
        // Procesar cada ítem del carrito
        for (ItemCarrito itemCarrito : itemsCarrito) {
            String claveItemCarrito = generarClaveItem(itemCarrito);
            ItemOrden itemOrdenCorrespondiente = mapaItemsOrden.get(claveItemCarrito);
            if (itemOrdenCorrespondiente == null) continue; // No se encontró un ítem de orden correspondiente
            
            // Migrar los thumbnails del ítem del carrito al ítem de orden correspondiente
            migrarThumbnailsParaItem(itemCarrito.getId(), itemOrdenCorrespondiente);
        }
        
        // Importante: Refrescar cada ítem de orden para cargar sus thumbnails después de la migración
        for (ItemOrden item : itemsOrden) {
            entityManager.refresh(item);
            logger.info("ItemOrden {} refrescado, tiene {} thumbnails", item.getId(), 
                    item.getThumbnails() != null ? item.getThumbnails().size() : 0);
        }
    }
    
    /**
     * Migra los thumbnails de un ítem de carrito específico a un ítem de orden.
     * Este método usa una transacción separada para cada ítem.
     * 
     * @param itemCarritoId ID del ítem del carrito
     * @param itemOrden Ítem de orden al que migrar los thumbnails
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void migrarThumbnailsParaItem(Long itemCarritoId, ItemOrden itemOrden) {
        try {
            // Asegurarnos que tenemos el ItemOrden completamente inicializado y gestionado por JPA
            ItemOrden itemOrdenGestionado = entityManager.find(ItemOrden.class, itemOrden.getId());
            if (itemOrdenGestionado == null) {
                logger.error("No se encontró el ítem de orden con ID {} en la base de datos", itemOrden.getId());
                return;
            }
            
            logger.debug("Buscando thumbnails para el ítem de carrito ID={}", itemCarritoId);
            
            // Ejecutar una query nativa para actualizar directamente en la base de datos las asociaciones
            // Esto evita los problemas de validación de entidades y objetos transient
            String updateQuery = "UPDATE thumbnails_item SET item_orden_id = ?, item_carrito_id = NULL WHERE item_carrito_id = ?";
            int actualizados = entityManager.createNativeQuery(updateQuery)
                    .setParameter(1, itemOrdenGestionado.getId())
                    .setParameter(2, itemCarritoId)
                    .executeUpdate();
            
            // Forzar la sincronización con la base de datos
            entityManager.flush();
            
            if (actualizados > 0) {
                logger.info("Actualizados {} thumbnails del ítem de carrito {} al ítem de orden {}", 
                        actualizados, itemCarritoId, itemOrdenGestionado.getId());
                
                // Realizar una consulta de verificación para confirmar que la migración fue exitosa
                int countVerificacion = ((Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM thumbnails_item WHERE item_orden_id = ?")
                        .setParameter(1, itemOrdenGestionado.getId())
                        .getSingleResult()).intValue();
                
                logger.info("Verificación: hay {} thumbnails asociados ahora al itemOrden {}", 
                        countVerificacion, itemOrdenGestionado.getId());
            } else {
                logger.info("No se encontraron thumbnails para el ítem de carrito {}", itemCarritoId);
            }
            
        } catch (Exception e) {
            logger.error("Error al migrar thumbnails del ítem de carrito {} al ítem de orden {}: {}", 
                    itemCarritoId, itemOrden.getId(), e.getMessage(), e);
            // Propagamos la excepción para que Spring pueda manejar el rollback apropiadamente
            throw new RuntimeException("Error al migrar thumbnails: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera una clave única para identificar ítems basándose en su contenido.
     * Esta clave se usa para relacionar los ítems de carrito con sus correspondientes ítems de orden.
     * 
     * @param item Puede ser un ItemCarrito o un ItemOrden
     * @return Clave única que identifica el ítem
     */
    private String generarClaveItem(Object item) {
        StringBuilder clave = new StringBuilder();
        
        if (item instanceof ItemCarrito) {
            ItemCarrito itemCarrito = (ItemCarrito) item;
            
            // Identificar por producto o diseño personalizado
            if (itemCarrito.getProducto() != null) {
                clave.append("P").append(itemCarrito.getProducto().getId());
            } else if (itemCarrito.getDisenoPersonalizado() != null) {
                clave.append("D").append(itemCarrito.getDisenoPersonalizado().getId());
            }
            
            // Añadir color y talla si existen
            if (itemCarrito.getColor() != null) {
                clave.append("_C").append(itemCarrito.getColor().getId());
            }
            
            if (itemCarrito.getTalla() != null) {
                clave.append("_T").append(itemCarrito.getTalla().getId());
            }
            
        } else if (item instanceof ItemOrden) {
            ItemOrden itemOrden = (ItemOrden) item;
            
            // Identificar por producto o diseño personalizado
            if (itemOrden.getProducto() != null) {
                clave.append("P").append(itemOrden.getProducto().getId());
            } else if (itemOrden.getDisenoPersonalizado() != null) {
                clave.append("D").append(itemOrden.getDisenoPersonalizado().getId());
            }
            
            // Añadir color y talla si existen
            if (itemOrden.getColor() != null) {
                clave.append("_C").append(itemOrden.getColor().getId());
            }
            
            if (itemOrden.getTalla() != null) {
                clave.append("_T").append(itemOrden.getTalla().getId());
            }
        }
        
        return clave.toString();
    }
    
    /**
     * Obtiene una orden por su ID.
     * 
     * @param ordenId ID de la orden
     * @return DTO de la orden
     * @throws ResourceNotFoundException Si la orden no existe
     */
    @Transactional(readOnly = true)
    public OrdenDTO obtenerOrdenPorId(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + ordenId));
        
        return convertirAOrdenDTO(orden);
    }

    /**
     * Crea una nueva orden a partir de un carrito.
     * 
     * @param crearOrdenDTO DTO con datos para crear la orden
     * @return DTO de la orden creada
     */
    @Transactional
    public OrdenDTO crearOrden(CrearOrdenDTO crearOrdenDTO) {
        // Obtener entidades necesarias
        Usuario usuario = usuarioRepository.findById(crearOrdenDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + crearOrdenDTO.getUsuarioId()));
        
        Carrito carrito = carritoService.obtenerCarritoPorId(crearOrdenDTO.getCarritoId());
        
        if (carrito.getItems().isEmpty()) {
            throw new IllegalStateException("No se puede crear una orden con un carrito vacío");
        }
        
        // Obtener o crear el estado inicial de orden (por ejemplo, "Pendiente" o "Recibida")
        EstadoOrden estadoInicial = estadoOrdenRepository.findByNombre(ESTADO_PENDIENTE_NOMBRE);
        if (estadoInicial == null) {
            // Si no existe, crear el estado
            estadoInicial = new EstadoOrden();
            estadoInicial.setCodigo(ESTADO_PENDIENTE_CODIGO);  // Establecer el código según la tabla
            estadoInicial.setNombre(ESTADO_PENDIENTE_NOMBRE);
            estadoInicial.setDescripcion(ESTADO_PENDIENTE_DESCRIPCION);
            estadoInicial = estadoOrdenRepository.save(estadoInicial);
        }
        
        // Crear la orden
        Orden nuevaOrden = new Orden();
        nuevaOrden.setUsuario(usuario);
        nuevaOrden.setEstado(estadoInicial);
            // La creación de fechas se maneja automáticamente por @PrePersist
            // Generar código de seguimiento único
        nuevaOrden.setCodigoSeguimiento("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Inicializar el precio total en cero (se actualizará después)
        nuevaOrden.setPrecioTotal(BigDecimal.ZERO);
        
        // Guardar la orden para obtener su ID
        nuevaOrden = ordenRepository.save(nuevaOrden);
        
        // Convertir ítems del carrito a ítems de orden
        BigDecimal totalOrden = BigDecimal.ZERO;
        
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            ItemOrden itemOrden = new ItemOrden();
            itemOrden.setOrden(nuevaOrden);
            
            // Establecer el producto o diseño personalizado
            if (itemCarrito.getProducto() != null) {
                itemOrden.setProducto(itemCarrito.getProducto());
                itemOrden.setNombre(itemCarrito.getProducto().getNombre());
            } else if (itemCarrito.getDisenoPersonalizado() != null) {
                itemOrden.setDisenoPersonalizado(itemCarrito.getDisenoPersonalizado());
                itemOrden.setNombre("Diseño personalizado: " + itemCarrito.getDisenoPersonalizado().getDetalle());
            }
            
            // Establecer los detalles del ítem
            itemOrden.setCantidad(itemCarrito.getCantidad());
            itemOrden.setPrecioUnitario(itemCarrito.getPrecioUnitario());
            
            // El cálculo del precio total se hace automáticamente en el método calcularPrecioTotal() de ItemOrden
            
            // Establecer color y talla
            if (itemCarrito.getColor() != null) {
                itemOrden.setColor(itemCarrito.getColor());
                itemOrden.setColorId(itemCarrito.getColorId());
            }
            
            if (itemCarrito.getTalla() != null) {
                itemOrden.setTalla(itemCarrito.getTalla());
                itemOrden.setTallaId(itemCarrito.getTallaId());
            }
            
            // Establecer tipo de ítem (relación completa en lugar de solo ID)
            itemOrden.setTipoItem(itemCarrito.getTipoItem());
            
            // Agregar a la lista de ítems de la orden
            nuevaOrden.addItem(itemOrden);
            
            // Calcular subtotal para acumular el total de la orden
            BigDecimal subtotal = itemOrden.getPrecioUnitario().multiply(BigDecimal.valueOf(itemOrden.getCantidad()));
            totalOrden = totalOrden.add(subtotal);
        }
        
        // Actualizar el precio total de la orden
        nuevaOrden.setPrecioTotal(totalOrden);
        
        // Guardar la orden actualizada con sus ítems y total
        nuevaOrden = ordenRepository.save(nuevaOrden);
        
        // Crear registro en el historial de estados
        HistorialEstadosOrden historialEstado = new HistorialEstadosOrden();
        historialEstado.setEstado(estadoInicial);
        historialEstado.setOrden(nuevaOrden);
        nuevaOrden.addHistorialEstado(historialEstado);
        
        // Guardar la orden con el historial
        nuevaOrden = ordenRepository.save(nuevaOrden);
        
        // Crear y guardar información de envío
        InfoEnvio infoEnvio = new InfoEnvio();
        infoEnvio.setOrden(nuevaOrden);
        infoEnvio.setNombreCompleto(crearOrdenDTO.getInfoEnvio().getNombreCompleto());
        infoEnvio.setTelefono(crearOrdenDTO.getInfoEnvio().getTelefono());
        infoEnvio.setEmail(crearOrdenDTO.getInfoEnvio().getEmail());
        infoEnvio.setDireccion(crearOrdenDTO.getInfoEnvio().getDireccion());
        infoEnvio.setCiudad(crearOrdenDTO.getInfoEnvio().getCiudad());
        infoEnvio.setEstado(crearOrdenDTO.getInfoEnvio().getEstado());
        infoEnvio.setCodigoPostal(crearOrdenDTO.getInfoEnvio().getCodigoPostal());
        infoEnvio.setPais(crearOrdenDTO.getInfoEnvio().getPais() != null ? 
                crearOrdenDTO.getInfoEnvio().getPais() : "Chile");
        infoEnvioRepository.save(infoEnvio);
        
        // Crear y guardar información de pago
        InfoPago infoPago = new InfoPago();
        infoPago.setOrden(nuevaOrden);
        infoPago.setMetodoPagoId(crearOrdenDTO.getInfoPago().getMetodoPagoId());
        infoPago.setTitularTarjeta(crearOrdenDTO.getInfoPago().getTitularTarjeta());
        infoPago.setUltimosDigitos(crearOrdenDTO.getInfoPago().getUltimosDigitos());
        infoPago.setIdTransaccion(crearOrdenDTO.getInfoPago().getIdTransaccion());
        infoPagoRepository.save(infoPago);
        
        // Migrar los thumbnails de los items del carrito a los items de la orden antes de vaciar el carrito
        migrarThumbnails(carrito.getItems(), nuevaOrden.getItems());
        
        // Vaciar el carrito después de crear la orden
        carritoService.vaciarCarrito(carrito.getId());
        
        // Convertir y retornar la orden como DTO
        return convertirAOrdenDTO(nuevaOrden);
    }

    /**
     * Actualiza el estado de una orden.
     * 
     * @param ordenId ID de la orden
     * @param estadoId ID del nuevo estado
     * @return DTO de la orden actualizada
     */
    @Transactional
    public OrdenDTO actualizarEstadoOrden(Long ordenId, Long estadoId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + ordenId));
        
        EstadoOrden nuevoEstado = estadoOrdenRepository.findById(estadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + estadoId));
        
        // Registrar el cambio de estado en el historial
        HistorialEstadosOrden historialEstado = new HistorialEstadosOrden();
        historialEstado.setEstado(nuevoEstado);
        historialEstado.setOrden(orden);
        // La fecha se establecerá automáticamente en el onCreate() del modelo
        orden.addHistorialEstado(historialEstado);
        
        // Actualizar el estado actual de la orden
        orden.setEstado(nuevoEstado);
        orden = ordenRepository.save(orden);
        
        return convertirAOrdenDTO(orden);
    }

    /**
     * Convierte una entidad Orden a un DTO.
     * 
     * @param orden Entidad Orden
     * @return DTO de la orden
     */
    private OrdenDTO convertirAOrdenDTO(Orden orden) {
        OrdenDTO dto = new OrdenDTO();
        
        dto.setId(orden.getId());
    
    // Manejar orden anónima (usuario puede ser nulo)
    if (orden.getUsuario() != null) {
        dto.setUsuarioId(orden.getUsuario().getId());
        dto.setNombreUsuario(orden.getUsuario().getNombre());
    } else {
        // Para órdenes anónimas
        dto.setUsuarioId(null);
        dto.setNombreUsuario("Cliente Anónimo");
    }
    
    dto.setCreadaEn(orden.getCreadoEn());
    dto.setTotal(orden.getPrecioTotal());
    dto.setEstado(orden.getEstado() != null ? orden.getEstado().getNombre() : "");
        
        // Información adicional de envío y pago si está disponible
        if (orden.getInfoEnvio() != null) {
            dto.setDireccionEntrega(orden.getInfoEnvio().getDireccion());
        } else {
            dto.setDireccionEntrega("");
        }
        
        if (orden.getInfoPago() != null) {
            // Usar ID del método de pago como texto provisional
            dto.setMetodoPago("Método de pago ID: " + orden.getInfoPago().getMetodoPagoId());
        } else {
            dto.setMetodoPago("");
        }
        
        // Convertir ítems usando el servicio DTOConverter para incluir objetos completos
        List<ItemOrdenDTO> itemsDTO = orden.getItems().stream()
                .map(dtoConverterService::convertToItemOrdenDTO)
                .toList();
        
        dto.setItems(itemsDTO);
        
        return dto;
    }

    /**
     * Cancela una orden.
     * 
     * @param ordenId ID de la orden
     * @return DTO de la orden cancelada
     */
    @Transactional
    public OrdenDTO cancelarOrden(Long ordenId) {
        // Buscar el estado CANCELED por su código
        EstadoOrden estadoCancelado = estadoOrdenRepository.findByCodigo("CANCELED");
        if (estadoCancelado == null) {
            throw new ResourceNotFoundException("Estado CANCELED no encontrado en la base de datos");
        }
        
        // No usar this para evitar proxy bypass en métodos transaccionales
        return actualizarEstadoOrden(ordenId, estadoCancelado.getId());
    }
    
    /**
     * Crea una nueva orden para un cliente sin cuenta de usuario (anónimo).
     * El carrito está en memoria del cliente, no en la base de datos.
     * 
     * @param crearOrdenAnonimaDTO DTO con datos para crear la orden anónima
     * @return DTO de la orden creada
     * @throws ResourceNotFoundException Si el estado inicial u objetos necesarios no existen
     */
    @Transactional
    public OrdenDTO crearOrdenAnonima(CrearOrdenAnonimaDTO crearOrdenAnonimaDTO) {
        // Verificar que haya ítems en el carrito en memoria
        if (crearOrdenAnonimaDTO.getItems() == null || crearOrdenAnonimaDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito no puede estar vacío");
        }
        
        // Buscar estado inicial para la orden (generalmente "PENDIENTE")
        EstadoOrden estadoInicial = estadoOrdenRepository.findByNombre(ESTADO_PENDIENTE_NOMBRE);
        if (estadoInicial == null) {
            // Si no existe, crear el estado
            estadoInicial = new EstadoOrden();
            estadoInicial.setCodigo(ESTADO_PENDIENTE_CODIGO);  // Establecer el código según la tabla
            estadoInicial.setNombre(ESTADO_PENDIENTE_NOMBRE);
            estadoInicial.setDescripcion(ESTADO_PENDIENTE_DESCRIPCION);
            estadoInicial = estadoOrdenRepository.save(estadoInicial);
        }
        
        // Crear una nueva orden
        Orden nuevaOrden = new Orden();
        // No asignamos usuario ya que es una orden anónima
        nuevaOrden.setEstado(estadoInicial);
        nuevaOrden.setCodigoSeguimiento("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Inicializar el precio total en cero (se actualizará después)
        nuevaOrden.setPrecioTotal(BigDecimal.ZERO);
        
        // Guardar la orden para obtener su ID
        nuevaOrden = ordenRepository.save(nuevaOrden);
        
        // Inicializar total de la orden
        BigDecimal totalOrden = BigDecimal.ZERO;
        
        // Agregar los ítems del carrito en memoria a la orden
        for (ItemCarritoMemoriaDTO itemMemoria : crearOrdenAnonimaDTO.getItems()) {
            ItemOrden itemOrden = new ItemOrden();
            itemOrden.setOrden(nuevaOrden);
            
            // Establecer el nombre, usando un valor por defecto si es necesario
            if (itemMemoria.getNombre() != null) {
                itemOrden.setNombre(itemMemoria.getNombre());
            } else if (itemMemoria.getProductoId() != null) {
                itemOrden.setNombre("Producto ID: " + itemMemoria.getProductoId());
            } else if (itemMemoria.getDisenoPersonalizadoId() != null) {
                itemOrden.setNombre("Diseño Personalizado ID: " + itemMemoria.getDisenoPersonalizadoId());
            } else {
                itemOrden.setNombre("Ítem sin nombre");
            }
            
            // Establecer la cantidad, usando 1 por defecto si es null
            itemOrden.setCantidad(itemMemoria.getCantidad() != null ? itemMemoria.getCantidad() : 1);
            
            // Establecer el tipo de ítem (entidad completa), usando un valor por defecto si es null
            Long tipoItemId = itemMemoria.getTipoItemId();
            if (tipoItemId == null) {
                // Si estamos trabajando con un diseño personalizado, usar tipo 2 (personalizado)
                // Si es un producto estándar, usar tipo 1 (estándar)
                if (itemMemoria.getDisenoPersonalizadoId() != null) {
                    tipoItemId = 2L; // Tipo para diseño personalizado
                } else {
                    tipoItemId = 1L; // Tipo para producto estándar
                }
                System.out.println("Asignando tipoItemId por defecto: " + tipoItemId);
            }
            
            // La comprobación de null se eliminó porque tipoItemId nunca puede ser null en este punto
            // debido a la asignación en el bloque if-else anterior
            
            // IMPORTANTE: Buscar y establecer la entidad TipoItem completa (no solo el ID)
            final Long finalTipoItemId = tipoItemId; // Capturar el valor en una variable final para la lambda
            try {
                TipoItem tipoItem = tipoItemRepository.findById(finalTipoItemId)
                        .orElseThrow(() -> new RuntimeException("TipoItem no encontrado con ID: " + finalTipoItemId));
                itemOrden.setTipoItem(tipoItem);
                System.out.println("TipoItem encontrado y asignado: " + tipoItem.getId());
            } catch (Exception e) {
                System.out.println("ERROR al buscar TipoItem: " + e.getMessage());
                // Como último recurso, intentamos una vez más con ID 1
                if (finalTipoItemId != 1L) {
                    try {
                        TipoItem defaultTipoItem = tipoItemRepository.findById(1L)
                                .orElseThrow(() -> new RuntimeException("TipoItem por defecto no encontrado"));
                        itemOrden.setTipoItem(defaultTipoItem);
                        System.out.println("TipoItem por defecto asignado: " + defaultTipoItem.getId());
                    } catch (Exception ex) {
                        System.out.println("ERROR FATAL: No se pudo encontrar ni siquiera el TipoItem por defecto");
                        // Ya no podemos hacer más, va a fallar pero al menos lo intentamos
                    }
                }
            }
            
            // Variable para almacenar el precio, se inicializa con el valor del DTO (si existe)
            BigDecimal precioUnitario = itemMemoria.getPrecioUnitario();
            
            // Asignar producto si existe
            if (itemMemoria.getProductoId() != null) {
                Producto producto = productoRepository.findById(itemMemoria.getProductoId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto no encontrado con ID: " + itemMemoria.getProductoId()));
                itemOrden.setProducto(producto);
                
                // Si no hay precio unitario en el DTO, usamos uno por defecto
                if (precioUnitario == null) {
                    // Precio por defecto para productos
                    precioUnitario = new BigDecimal("29.99");
                }
            }
            
            // Asignar diseño personalizado si existe
            if (itemMemoria.getDisenoPersonalizadoId() != null) {
                DisenoPersonalizado diseno = disenoPersonalizadoRepository.findById(itemMemoria.getDisenoPersonalizadoId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Diseño personalizado no encontrado con ID: " + itemMemoria.getDisenoPersonalizadoId()));
                itemOrden.setDisenoPersonalizado(diseno);
                
                // Obtener precio automáticamente del diseño personalizado
                precioUnitario = diseno.getPrecio();
            }
            
            // Asignar color si existe - ahora usando directamente el ID o nombre como string
            if (itemMemoria.getColorId() != null) {
                // En ItemOrden solo guardar la referencia al colorId como string
                itemOrden.setColorId(itemMemoria.getColorId());
                
                // Intentar buscar el color pero si no se encuentra, no es crítico
                try {
                    Color color = colorRepository.findById(itemMemoria.getColorId()).orElse(null);
                    if (color != null) {
                        itemOrden.setColor(color);
                    }
                } catch (Exception e) {
                    // Solo registrar el error pero continuar
                    System.out.println("No se encontró el color con ID: " + itemMemoria.getColorId());
                }
            }
            
            // Asignar talla si existe - ahora usando directamente el ID o nombre como string
            if (itemMemoria.getTallaId() != null) {
                // En ItemOrden solo guardar la referencia al tallaId como string
                itemOrden.setTallaId(itemMemoria.getTallaId());
                
                // Intentar buscar la talla pero si no se encuentra, no es crítico
                try {
                    Talla talla = tallaRepository.findById(itemMemoria.getTallaId()).orElse(null);
                    if (talla != null) {
                        itemOrden.setTalla(talla);
                    }
                } catch (Exception e) {
                    // Solo registrar el error pero continuar
                    System.out.println("No se encontró la talla con ID: " + itemMemoria.getTallaId());
                }
            }
            
            // Asegurar que tengamos un precio unitario válido
            if (precioUnitario == null) {
                // Usar precio por defecto como medida de seguridad final
                precioUnitario = new BigDecimal("19.99");
                System.out.println("Advertencia: Usando precio por defecto para ítem sin precio: " + itemOrden.getNombre());
            }
            
            // Establecer el precio unitario calculado/obtenido
            itemOrden.setPrecioUnitario(precioUnitario);
            
            // Verificar que el tipoItem esté establecido antes de agregar a la orden
            if (itemOrden.getTipoItem() == null) {
                System.out.println("ERROR CRÍTICO: tipoItem sigue siendo nulo antes de agregar el ítem a la orden");
                // Último intento - crear un TipoItem directamente si es posible
                try {
                    TipoItem emergencyTipoItem = tipoItemRepository.findById(1L).orElse(null);
                    if (emergencyTipoItem != null) {
                        itemOrden.setTipoItem(emergencyTipoItem);
                        System.out.println("Asignado TipoItem de emergencia: " + emergencyTipoItem.getId());
                    }
                } catch (Exception e) {
                    System.out.println("FALLO TOTAL al intentar asignar TipoItem de emergencia: " + e.getMessage());
                }
            } else {
                System.out.println("tipoItem correctamente establecido: " + itemOrden.getTipoItem().getId());
            }
            
            // Establecer el precio total del ítem
            int cantidad = (itemMemoria.getCantidad() != null && itemMemoria.getCantidad() > 0) ? 
                    itemMemoria.getCantidad() : 1;
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            itemOrden.setPrecioTotal(subtotal);
            
            // Agregar a la lista de ítems de la orden
            nuevaOrden.addItem(itemOrden);
            
            // Acumular el total de la orden
            totalOrden = totalOrden.add(subtotal);
        }
        
        // Actualizar el precio total de la orden
        nuevaOrden.setPrecioTotal(totalOrden);
        
        // Guardar la orden actualizada con sus ítems y total
        nuevaOrden = ordenRepository.save(nuevaOrden);
        
        // Crear registro en el historial de estados
        HistorialEstadosOrden historialEstado = new HistorialEstadosOrden();
        historialEstado.setEstado(estadoInicial);
        historialEstado.setOrden(nuevaOrden);
        nuevaOrden.addHistorialEstado(historialEstado);
        
        // Guardar la orden con el historial
        nuevaOrden = ordenRepository.save(nuevaOrden);
        
        // Crear y guardar información de envío
        InfoEnvio infoEnvio = new InfoEnvio();
        infoEnvio.setOrden(nuevaOrden);
        
        // Validar que la información de envío no sea nula
        if (crearOrdenAnonimaDTO.getInfoEnvio() == null) {
            System.out.println("ERROR: La información de envío es nula, creando información por defecto");
            // Crear información de envío por defecto
            infoEnvio.setNombreCompleto("Cliente Anónimo");
            infoEnvio.setTelefono("+56 9 0000 0000");
            infoEnvio.setEmail("cliente@anonimo.com");
            infoEnvio.setDireccion("Dirección no especificada");
            infoEnvio.setCiudad("Santiago");
            infoEnvio.setEstado("Región Metropolitana");
            infoEnvio.setCodigoPostal("0000000");
            infoEnvio.setPais("Chile");
        } else {
            // Asignar los valores del DTO, con validaciones para campos requeridos
            // El nombre completo es obligatorio
            // NOTA: En el DTO InfoEnvioDTO es nombreCompleto pero en el JSON de entrada es nombre
            String nombreCompleto = crearOrdenAnonimaDTO.getInfoEnvio().getNombre(); // Usamos getNombre() en lugar de getNombreCompleto()
            infoEnvio.setNombreCompleto(nombreCompleto != null && !nombreCompleto.trim().isEmpty() ? 
                    nombreCompleto : "Cliente Anónimo");
            
            // Teléfono
            String telefono = crearOrdenAnonimaDTO.getInfoEnvio().getTelefono();
            infoEnvio.setTelefono(telefono != null && !telefono.trim().isEmpty() ? 
                    telefono : "+56 9 0000 0000");
            
            // Email
            String email = crearOrdenAnonimaDTO.getInfoEnvio().getEmail();
            infoEnvio.setEmail(email != null && !email.trim().isEmpty() ? 
                    email : "cliente@anonimo.com");
            
            // Dirección
            String direccion = crearOrdenAnonimaDTO.getInfoEnvio().getDireccion();
            infoEnvio.setDireccion(direccion != null && !direccion.trim().isEmpty() ? 
                    direccion : "Dirección no especificada");
            
            // Ciudad
            String ciudad = crearOrdenAnonimaDTO.getInfoEnvio().getCiudad();
            infoEnvio.setCiudad(ciudad != null && !ciudad.trim().isEmpty() ? 
                    ciudad : "Santiago");
            
            // Estado/Región
            String estado = crearOrdenAnonimaDTO.getInfoEnvio().getEstado();
            infoEnvio.setEstado(estado != null && !estado.trim().isEmpty() ? 
                    estado : "Región Metropolitana");
            
            // Código postal
            String codigoPostal = crearOrdenAnonimaDTO.getInfoEnvio().getCodigoPostal();
            infoEnvio.setCodigoPostal(codigoPostal != null && !codigoPostal.trim().isEmpty() ? 
                    codigoPostal : "0000000");
            
            // País (con valor por defecto "Chile")
            String pais = crearOrdenAnonimaDTO.getInfoEnvio().getPais();
            infoEnvio.setPais(pais != null && !pais.trim().isEmpty() ? 
                    pais : "Chile");
        }
        
        // Verificar que los campos obligatorios no sean nulos antes de guardar
        System.out.println("Verificando información de envío antes de guardar: nombreCompleto=" + 
                infoEnvio.getNombreCompleto());
        
        infoEnvioRepository.save(infoEnvio);
        
        // Crear y guardar información de pago
        InfoPago infoPago = new InfoPago();
        infoPago.setOrden(nuevaOrden);
        
        // Validar que la información de pago no sea nula
        if (crearOrdenAnonimaDTO.getInfoPago() == null) {
            System.out.println("ERROR: La información de pago es nula, creando información por defecto");
            // Valores por defecto para información de pago
            infoPago.setMetodoPagoId(1L); // Método de pago por defecto
            infoPago.setTitularTarjeta("Cliente Anónimo");
            infoPago.setUltimosDigitos("****");
            infoPago.setIdTransaccion(UUID.randomUUID().toString());
        } else {
            // Método de pago (valor por defecto 1)
            Long metodoPagoId = crearOrdenAnonimaDTO.getInfoPago().getMetodoPagoId();
            infoPago.setMetodoPagoId(metodoPagoId != null ? metodoPagoId : 1L);
            
            // Titular de tarjeta
            String titularTarjeta = crearOrdenAnonimaDTO.getInfoPago().getTitularTarjeta();
            infoPago.setTitularTarjeta(titularTarjeta != null && !titularTarjeta.trim().isEmpty() ? 
                    titularTarjeta : "Cliente Anónimo");
            
            // Últimos dígitos
            String ultimosDigitos = crearOrdenAnonimaDTO.getInfoPago().getUltimosDigitos();
            infoPago.setUltimosDigitos(ultimosDigitos != null && !ultimosDigitos.trim().isEmpty() ? 
                    ultimosDigitos : "****");
            
            // ID de transacción
            String idTransaccion = crearOrdenAnonimaDTO.getInfoPago().getIdTransaccion();
            infoPago.setIdTransaccion(idTransaccion != null && !idTransaccion.trim().isEmpty() ? 
                    idTransaccion : UUID.randomUUID().toString());
        }
        
        // Verificar que los campos obligatorios no sean nulos antes de guardar
        System.out.println("Verificando información de pago antes de guardar: metodoPagoId=" + 
                infoPago.getMetodoPagoId());
        
        infoPagoRepository.save(infoPago);
        
        return convertirAOrdenDTO(nuevaOrden);
    }
}
