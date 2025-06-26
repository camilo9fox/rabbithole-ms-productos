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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para operaciones relacionadas con órdenes.
 */
@Service
public class OrdenService {
    
    // Constantes para estados de orden
    private static final String ESTADO_PENDIENTE_NOMBRE = "PENDIENTE";
    private static final String ESTADO_PENDIENTE_CODIGO = "1 PENDING";
    private static final String ESTADO_PENDIENTE_DESCRIPCION = "La orden está pendiente de pago";
    

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
            DTOConverterService dtoConverterService) {
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
        
        // Vaciar el carrito después de crear la orden
        carritoService.vaciarCarrito(carrito.getId());
        
        // Convertir y retornar la orden como DTO
        return convertirAOrdenDTO(nuevaOrden);
    }

    /**
     * Actualiza el estado de una orden.
     * 
     * @param ordenId ID de la orden
     * @param estadoNombre Nombre del nuevo estado
     * @return DTO de la orden actualizada
     */
    @Transactional
    public OrdenDTO actualizarEstadoOrden(Long ordenId, String estadoNombre) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + ordenId));
        
        EstadoOrden nuevoEstado = estadoOrdenRepository.findByNombre(estadoNombre);
        if (nuevoEstado == null) {
            // Si no existe, crear el estado
            nuevoEstado = new EstadoOrden();
            nuevoEstado.setNombre(estadoNombre);
            nuevoEstado = estadoOrdenRepository.save(nuevoEstado);
        }
        
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
        dto.setUsuarioId(orden.getUsuario().getId());
        dto.setNombreUsuario(orden.getUsuario().getNombre());
        dto.setCreadaEn(orden.getCreadoEn());
        dto.setTotal(orden.getPrecioTotal());
        dto.setEstado(orden.getEstado().getNombre());
        
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
        // No usar this para evitar proxy bypass en métodos transaccionales
        return actualizarEstadoOrden(ordenId, "CANCELADA");
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
            itemOrden.setNombre(itemMemoria.getNombre());
            itemOrden.setCantidad(itemMemoria.getCantidad());
            itemOrden.setTipoItemId(itemMemoria.getTipoItemId());
            
            // Variable para almacenar el precio, se inicializa con el valor del DTO (si existe)
            BigDecimal precioUnitario = itemMemoria.getPrecioUnitario();
            
            // Asignar producto si existe
            if (itemMemoria.getProductoId() != null) {
                Producto producto = productoRepository.findById(itemMemoria.getProductoId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto no encontrado con ID: " + itemMemoria.getProductoId()));
                itemOrden.setProducto(producto);
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
            
            // Asignar color si existe
            if (itemMemoria.getColorId() != null) {
                Color color = colorRepository.findById(itemMemoria.getColorId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Color no encontrado con ID: " + itemMemoria.getColorId()));
                itemOrden.setColor(color);
                itemOrden.setColorId(itemMemoria.getColorId());
            }
            
            // Asignar talla si existe
            if (itemMemoria.getTallaId() != null) {
                Talla talla = tallaRepository.findById(itemMemoria.getTallaId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Talla no encontrada con ID: " + itemMemoria.getTallaId()));
                itemOrden.setTalla(talla);
                itemOrden.setTallaId(itemMemoria.getTallaId());
            }
            
            // Establecer el precio unitario calculado/obtenido
            itemOrden.setPrecioUnitario(precioUnitario);
            
            // Agregar a la lista de ítems de la orden
            nuevaOrden.addItem(itemOrden);
            
            // Calcular subtotal para acumular el total de la orden
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(itemMemoria.getCantidad()));
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
        infoEnvio.setNombreCompleto(crearOrdenAnonimaDTO.getInfoEnvio().getNombreCompleto());
        infoEnvio.setTelefono(crearOrdenAnonimaDTO.getInfoEnvio().getTelefono());
        infoEnvio.setEmail(crearOrdenAnonimaDTO.getInfoEnvio().getEmail());
        infoEnvio.setDireccion(crearOrdenAnonimaDTO.getInfoEnvio().getDireccion());
        infoEnvio.setCiudad(crearOrdenAnonimaDTO.getInfoEnvio().getCiudad());
        infoEnvio.setEstado(crearOrdenAnonimaDTO.getInfoEnvio().getEstado());
        infoEnvio.setCodigoPostal(crearOrdenAnonimaDTO.getInfoEnvio().getCodigoPostal());
        infoEnvio.setPais(crearOrdenAnonimaDTO.getInfoEnvio().getPais() != null ? 
                crearOrdenAnonimaDTO.getInfoEnvio().getPais() : "Chile");
        infoEnvioRepository.save(infoEnvio);
        
        // Crear y guardar información de pago
        InfoPago infoPago = new InfoPago();
        infoPago.setOrden(nuevaOrden);
        infoPago.setMetodoPagoId(crearOrdenAnonimaDTO.getInfoPago().getMetodoPagoId());
        infoPago.setTitularTarjeta(crearOrdenAnonimaDTO.getInfoPago().getTitularTarjeta());
        infoPago.setUltimosDigitos(crearOrdenAnonimaDTO.getInfoPago().getUltimosDigitos());
        infoPago.setIdTransaccion(crearOrdenAnonimaDTO.getInfoPago().getIdTransaccion());
        infoPagoRepository.save(infoPago);
        
        return convertirAOrdenDTO(nuevaOrden);
    }
}
