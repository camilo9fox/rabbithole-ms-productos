package com.rabbithole.productos.controller;

import com.rabbithole.productos.dto.CarritoDTO;
import com.rabbithole.productos.model.*;
import com.rabbithole.productos.service.CarritoService;
import com.rabbithole.productos.service.DTOConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones relacionadas con carritos de compra.
 */
@RestController
@RequestMapping("/carritos")
@CrossOrigin(origins = "*")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private DTOConverterService dtoConverter;

    /**
     * Obtiene todos los carritos de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de carritos en formato DTO
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarritoDTO>> getCarritosPorUsuario(@PathVariable Long usuarioId) {
        List<Carrito> carritos = carritoService.obtenerCarritosUsuario(usuarioId);
        List<CarritoDTO> carritosDTO = carritos.stream()
                .map(dtoConverter::convertToCarritoDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(carritosDTO);
    }

    /**
     * Obtiene o crea el carrito activo para un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Carrito activo en formato DTO
     */
    @GetMapping("/usuario/{usuarioId}/activo")
    public ResponseEntity<CarritoDTO> getCarritoActivo(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.obtenerOCrearCarritoActivo(usuarioId);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Obtiene un carrito específico por su ID.
     *
     * @param carritoId ID del carrito
     * @return Carrito en formato DTO
     */
    @GetMapping("/{carritoId}")
    public ResponseEntity<CarritoDTO> getCarritoPorId(@PathVariable Long carritoId) {
        Carrito carrito = carritoService.obtenerCarritoPorId(carritoId);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Agrega un producto al carrito.
     *
     * @param carritoId   ID del carrito
     * @param requestBody Cuerpo JSON con los detalles del producto a agregar
     * @return Carrito actualizado en formato DTO
     */
    @PostMapping("/{carritoId}/productos")
    public ResponseEntity<CarritoDTO> agregarProducto(
            @PathVariable Long carritoId,
            @RequestBody Map<String, Object> requestBody) {

        Long productoId = Long.valueOf(requestBody.get("productoId").toString());
        String colorId = requestBody.get("colorId").toString();
        String tallaId = requestBody.get("tallaId").toString();
        Long tipoItemId = Long.valueOf(requestBody.get("tipoItemId").toString());
        Integer cantidad = requestBody.containsKey("cantidad") ? Integer.valueOf(requestBody.get("cantidad").toString())
                : 1;

        Carrito carrito = carritoService.agregarProductoAlCarrito(carritoId, productoId, colorId, tallaId, tipoItemId,
                cantidad);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Agrega un diseño personalizado al carrito.
     *
     * @param carritoId   ID del carrito
     * @param requestBody Cuerpo JSON con los detalles del diseño personalizado a
     *                    agregar
     * @return Carrito actualizado en formato DTO
     */
    @PostMapping("/{carritoId}/disenos")
    public ResponseEntity<CarritoDTO> agregarDiseno(
            @PathVariable Long carritoId,
            @RequestBody Map<String, Object> requestBody) {

        // Extraer los valores del cuerpo JSON
        Long disenoId = Long.valueOf(requestBody.get("disenoId").toString());
        String colorId = requestBody.get("colorId").toString();
        String tallaId = requestBody.get("tallaId").toString();
        Long tipoItemId = Long.valueOf(requestBody.get("tipoItemId").toString());
        BigDecimal precio = requestBody.containsKey("precio") ? new BigDecimal(requestBody.get("precio").toString())
                : BigDecimal.ZERO;
        Integer cantidad = requestBody.containsKey("cantidad") ? Integer.valueOf(requestBody.get("cantidad").toString())
                : 1;

        Carrito carrito = carritoService.agregarDisenoPersonalizadoAlCarrito(
                carritoId, disenoId, colorId, tallaId, tipoItemId, cantidad, precio);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Actualiza la cantidad de un ítem en el carrito.
     *
     * @param carritoId ID del carrito
     * @param itemId    ID del ítem
     * @param cantidad  Nueva cantidad
     * @return Carrito actualizado en formato DTO
     */
    @PutMapping("/{carritoId}/items/{itemId}/cantidad")
    public ResponseEntity<CarritoDTO> actualizarCantidadItem(
            @PathVariable Long carritoId,
            @PathVariable Long itemId,
            @RequestParam Integer cantidad) {

        Carrito carrito = carritoService.actualizarCantidadItem(carritoId, itemId, cantidad);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Elimina un ítem del carrito.
     *
     * @param carritoId ID del carrito
     * @param itemId    ID del ítem a eliminar
     * @return Carrito actualizado en formato DTO
     */
    @DeleteMapping("/{carritoId}/items/{itemId}")
    public ResponseEntity<CarritoDTO> eliminarItem(
            @PathVariable Long carritoId,
            @PathVariable Long itemId) {

        Carrito carrito = carritoService.eliminarItemDelCarrito(carritoId, itemId);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Vacía un carrito eliminando todos sus ítems.
     *
     * @param carritoId ID del carrito
     * @return Carrito vacío en formato DTO
     */
    @DeleteMapping("/{carritoId}/items")
    public ResponseEntity<CarritoDTO> vaciarCarrito(@PathVariable Long carritoId) {
        Carrito carrito = carritoService.vaciarCarrito(carritoId, false);
        CarritoDTO carritoDTO = dtoConverter.convertToCarritoDTO(carrito);
        return ResponseEntity.ok(carritoDTO);
    }

    /**
     * Elimina un carrito completo.
     *
     * @param carritoId ID del carrito a eliminar
     * @return Respuesta vacía con estado HTTP 204 No Content
     */
    @DeleteMapping("/{carritoId}")
    public ResponseEntity<Void> eliminarCarrito(@PathVariable Long carritoId) {
        carritoService.eliminarCarrito(carritoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
