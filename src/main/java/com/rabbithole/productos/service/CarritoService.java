package com.rabbithole.productos.service;

import com.rabbithole.productos.exception.ResourceNotFoundException;
import com.rabbithole.productos.model.*;
import com.rabbithole.productos.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones relacionadas con carritos de compra.
 */
@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private ColorRepository colorRepository;
    
    @Autowired
    private TallaRepository tallaRepository;
    
    @Autowired
    private TipoItemRepository tipoItemRepository;
    
    @Autowired
    private DisenoPersonalizadoRepository disenoPersonalizadoRepository;

    /**
     * Obtiene todos los carritos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de todos los carritos del usuario
     */
    @Transactional(readOnly = true)
    public List<Carrito> obtenerCarritosUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Obtiene el carrito activo de un usuario o crea uno nuevo si no existe.
     * 
     * @param usuarioId ID del usuario
     * @return Carrito activo del usuario
     */
    @Transactional
    public Carrito obtenerOCrearCarritoActivo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        
        List<Carrito> carritos = carritoRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
        
        // Si hay carritos existentes, retorna el más reciente
        if (!carritos.isEmpty()) {
            return carritos.get(0);
        }
        
        // Si no hay carritos, crea uno nuevo
        Carrito nuevoCarrito = new Carrito();
        nuevoCarrito.setUsuario(usuario);
        return carritoRepository.save(nuevoCarrito);
    }

    /**
     * Obtiene un carrito por su ID.
     * 
     * @param carritoId ID del carrito a buscar
     * @return El carrito encontrado
     * @throws ResourceNotFoundException Si el carrito no existe
     */
    @Transactional(readOnly = true)
    public Carrito obtenerCarritoPorId(Long carritoId) {
        return carritoRepository.findById(carritoId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + carritoId));
    }

    /**
     * Agrega un ítem de producto al carrito.
     * 
     * @param carritoId ID del carrito
     * @param productoId ID del producto
     * @param colorId ID del color
     * @param tallaId ID de la talla
     * @param tipoItemId ID del tipo de ítem
     * @param cantidad Cantidad del ítem
     * @return El carrito actualizado
     */
    @Transactional
    public Carrito agregarProductoAlCarrito(Long carritoId, Long productoId, String colorId, String tallaId, 
                                           String tipoItemId, Integer cantidad) {
        // Convertimos tipoItemId a Long para coincidir con el tipo en la entidad ItemCarrito
        Long tipoItemIdLong = Long.valueOf(tipoItemId);
        // Obtener entidades
        Carrito carrito = obtenerCarritoPorId(carritoId);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado con ID: " + colorId));
        Talla talla = tallaRepository.findById(tallaId)
                .orElseThrow(() -> new ResourceNotFoundException("Talla no encontrada con ID: " + tallaId));
        // Obtener el tipo de item
        TipoItem tipoItem = tipoItemRepository.findById(tipoItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de ítem no encontrado con ID: " + tipoItemId));
        
        // Verificar si el ítem ya existe en el carrito
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto() != null && item.getProducto().getId().equals(productoId)
                       && item.getColorId().equals(colorId) 
                       && item.getTallaId().equals(tallaId)
                       && item.getTipoItemId().equals(tipoItemIdLong))
                .findFirst();
        
        if (itemExistente.isPresent()) {
            // Actualizar cantidad
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            // Crear nuevo ítem
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setColor(color);
            nuevoItem.setTalla(talla);
            nuevoItem.setTipoItem(tipoItem);
            
            nuevoItem.setCantidad(cantidad);
            // Obtener precio desde el diseño personalizado
            BigDecimal precioUnitario = producto.getDisenoPersonalizado() != null ? 
                producto.getDisenoPersonalizado().getPrecio() : BigDecimal.ZERO;            
            nuevoItem.setPrecioUnitario(precioUnitario);
            
            carrito.addItem(nuevoItem);
        }
        
        return carritoRepository.save(carrito);
    }
    
    /**
     * Agrega un ítem con diseño personalizado al carrito.
     * 
     * @param carritoId ID del carrito
     * @param disenoPersonalizadoId ID del diseño personalizado
     * @param colorId ID del color
     * @param tallaId ID de la talla
     * @param tipoItemId ID del tipo de ítem
     * @param cantidad Cantidad del ítem
     * @param precioUnitario Precio unitario del ítem personalizado
     * @return El carrito actualizado
     */
    @Transactional
    public Carrito agregarDisenoPersonalizadoAlCarrito(Long carritoId, Long disenoPersonalizadoId, 
            String colorId, String tallaId, String tipoItemId, Integer cantidad, BigDecimal precioUnitario) {
        // Convertimos tipoItemId a Long para coincidir con el tipo en la entidad ItemCarrito
        Long tipoItemIdLong = Long.valueOf(tipoItemId);
        // Obtener entidades
        Carrito carrito = obtenerCarritoPorId(carritoId);
        DisenoPersonalizado diseno = disenoPersonalizadoRepository.findById(disenoPersonalizadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Diseño personalizado no encontrado con ID: " + disenoPersonalizadoId));
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado con ID: " + colorId));
        Talla talla = tallaRepository.findById(tallaId)
                .orElseThrow(() -> new ResourceNotFoundException("Talla no encontrada con ID: " + tallaId));
        // Obtener el tipo de item
        TipoItem tipoItem = tipoItemRepository.findById(tipoItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de ítem no encontrado con ID: " + tipoItemId));
        
        // Verificar si el ítem ya existe en el carrito
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getDisenoPersonalizado() != null 
                       && item.getDisenoPersonalizado().getId().equals(disenoPersonalizadoId)
                       && item.getColorId().equals(colorId) 
                       && item.getTallaId().equals(tallaId)
                       && item.getTipoItemId().equals(tipoItemIdLong))
                .findFirst();
        
        if (itemExistente.isPresent()) {
            // Actualizar cantidad
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            // Crear nuevo ítem
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setDisenoPersonalizado(diseno);
            nuevoItem.setColor(color);
            nuevoItem.setTalla(talla);
            nuevoItem.setTipoItem(tipoItem);
            
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(precioUnitario);
            
            carrito.addItem(nuevoItem);
        }
        
        return carritoRepository.save(carrito);
    }
    
    /**
     * Actualiza la cantidad de un ítem en el carrito.
     * 
     * @param carritoId ID del carrito
     * @param itemId ID del ítem
     * @param nuevaCantidad Nueva cantidad del ítem
     * @return El carrito actualizado
     */
    @Transactional
    public Carrito actualizarCantidadItem(Long carritoId, Long itemId, Integer nuevaCantidad) {
        Carrito carrito = obtenerCarritoPorId(carritoId);
        
        // Buscar el ítem en el carrito
        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado en el carrito con ID: " + itemId));
        
        if (nuevaCantidad <= 0) {
            // Si la cantidad es 0 o negativa, eliminar el ítem
            carrito.removeItem(item);
        } else {
            // Actualizar la cantidad
            item.setCantidad(nuevaCantidad);
        }
        
        return carritoRepository.save(carrito);
    }
    
    /**
     * Elimina un ítem del carrito.
     * 
     * @param carritoId ID del carrito
     * @param itemId ID del ítem a eliminar
     * @return El carrito actualizado
     */
    @Transactional
    public Carrito eliminarItemDelCarrito(Long carritoId, Long itemId) {
        Carrito carrito = obtenerCarritoPorId(carritoId);
        
        // Buscar el ítem en el carrito
        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado en el carrito con ID: " + itemId));
        
        // Eliminar el ítem
        carrito.removeItem(item);
        
        return carritoRepository.save(carrito);
    }
    
    /**
     * Vacía un carrito eliminando todos sus ítems.
     * 
     * @param carritoId ID del carrito
     * @return El carrito vacío
     */
    @Transactional
    public Carrito vaciarCarrito(Long carritoId) {
        Carrito carrito = obtenerCarritoPorId(carritoId);
        
        // Limpiar todos los ítems
        carrito.getItems().clear();
        
        return carritoRepository.save(carrito);
    }
    
    /**
     * Elimina un carrito.
     * 
     * @param carritoId ID del carrito a eliminar
     */
    @Transactional
    public void eliminarCarrito(Long carritoId) {
        // Verificar que el carrito existe antes de eliminarlo
        if (!carritoRepository.existsById(carritoId)) {
            throw new ResourceNotFoundException("Carrito no encontrado con ID: " + carritoId);
        }
        
        carritoRepository.deleteById(carritoId);
    }
}
