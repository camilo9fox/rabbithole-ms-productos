package com.rabbithole.productos.service;

import com.rabbithole.productos.dto.*;
import com.rabbithole.productos.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para convertir entidades a DTOs
 */
@Service
public class DTOConverterService {

    /**
     * Convierte un DisenoPersonalizado a su DTO
     */
    public DisenoPersonalizadoDTO convertToDisenoDTO(DisenoPersonalizado diseno) {
        if (diseno == null) {
            return null;
        }

        DisenoPersonalizadoDTO dto = new DisenoPersonalizadoDTO();
        dto.setId(diseno.getId());
        dto.setUsuarioId(diseno.getUsuarioId());
        dto.setDetalle(diseno.getDetalle());

        // Establecer IDs en lugar de objetos completos
        if (diseno.getColor() != null) {
            dto.setColorId(diseno.getColor().getId());
        }

        if (diseno.getTalla() != null) {
            dto.setTallaId(diseno.getTalla().getId());
        }

        dto.setPrecio(diseno.getPrecio());

        if (diseno.getEstado() != null) {
            dto.setEstadoId(diseno.getEstado().getId());
        }

        dto.setMotivoRechazo(diseno.getMotivoRechazo());
        dto.setNotasModificacion(diseno.getNotasModificacion());
        dto.setCreadoPorAdmin(diseno.getCreadoPorAdmin());
        dto.setCreadoEn(diseno.getCreadoEn());
        dto.setActualizadoEn(diseno.getActualizadoEn());

        // Convertir ángulos si existen
        if (diseno.getAngulos() != null && !diseno.getAngulos().isEmpty()) {
            List<AnguloDTO> angulosDTO = diseno.getAngulos().stream()
                    .map(this::convertToAnguloDTO)
                    .toList();
            dto.setAngulos(angulosDTO);
        }

        return dto;
    }

    /**
     * Convierte un AnguloDiseno a su DTO
     */
    private AnguloDTO convertToAnguloDTO(AnguloDiseno angulo) {
        if (angulo == null) {
            return null;
        }

        AnguloDTO dto = new AnguloDTO();
        dto.setId(angulo.getId());

        setTipoAnguloInfo(dto, angulo);
        dto.setThumbnailUrl(angulo.getThumbnailUrl());
        setElementoInfo(dto, angulo);

        return dto;
    }

    /**
     * Establece la información del tipo de ángulo en el DTO
     */
    private void setTipoAnguloInfo(AnguloDTO dto, AnguloDiseno angulo) {
        if (angulo.getTipoAngulo() != null) {
            dto.setTipoAnguloId(angulo.getTipoAngulo().getId());

            if (angulo.getTipoAngulo().getNombre() != null) {
                dto.setNombreAngulo(angulo.getTipoAngulo().getNombre());
            }
        }
    }

    /**
     * Establece la información del elemento en el DTO (texto o imagen)
     */
    private void setElementoInfo(AnguloDTO dto, AnguloDiseno angulo) {
        if (angulo.getElementoTexto() != null) {
            dto.setElemento(createElementoTextoDTO(angulo.getElementoTexto()));
        } else if (angulo.getElementoImagen() != null) {
            dto.setElemento(createElementoImagenDTO(angulo.getElementoImagen()));
        }
    }

    /**
     * Crea un ElementoDTO a partir de un ElementoTexto
     */
    private ElementoDTO createElementoTextoDTO(ElementoTexto elementoTexto) {
        ElementoDTO elementoDTO = new ElementoDTO();
        elementoDTO.setTipo("TEXTO");

        ElementoDisenoDTO propiedadesDiseno = createPropiedadesDisenoFromTexto(elementoTexto);
        Map<String, Object> propiedadesElemento = createPropiedadesElementoFromTexto(elementoTexto);

        elementoDTO.setPropiedadesDiseno(propiedadesDiseno);
        elementoDTO.setPropiedadesElemento(propiedadesElemento);

        return elementoDTO;
    }

    /**
     * Crea un ElementoDTO a partir de un ElementoImagen
     */
    private ElementoDTO createElementoImagenDTO(ElementoImagen elementoImagen) {
        ElementoDTO elementoDTO = new ElementoDTO();
        elementoDTO.setTipo("IMAGEN");

        ElementoDisenoDTO propiedadesDiseno = createPropiedadesDisenoFromImagen(elementoImagen);
        Map<String, Object> propiedadesElemento = createPropiedadesElementoFromImagen(elementoImagen);

        elementoDTO.setPropiedadesDiseno(propiedadesDiseno);
        elementoDTO.setPropiedadesElemento(propiedadesElemento);

        return elementoDTO;
    }

    /**
     * Crea las propiedades de diseño para un elemento de texto
     */
    private ElementoDisenoDTO createPropiedadesDisenoFromTexto(ElementoTexto elemento) {
        ElementoDisenoDTO propiedades = new ElementoDisenoDTO();

        Integer posX = elemento.getPosicionX();
        Integer posY = elemento.getPosicionY();

        propiedades.setPosicionX(posX != null ? posX.floatValue() : null);
        propiedades.setPosicionY(posY != null ? posY.floatValue() : null);
        propiedades.setRotacion(elemento.getRotacion());

        return propiedades;
    }

    /**
     * Crea las propiedades de diseño para un elemento de imagen
     */
    private ElementoDisenoDTO createPropiedadesDisenoFromImagen(ElementoImagen elemento) {
        ElementoDisenoDTO propiedades = new ElementoDisenoDTO();

        Integer posX = elemento.getPosicionX();
        Integer posY = elemento.getPosicionY();
        Integer anchura = elemento.getAnchura();
        Integer altura = elemento.getAltura();

        propiedades.setPosicionX(posX != null ? posX.floatValue() : null);
        propiedades.setPosicionY(posY != null ? posY.floatValue() : null);
        propiedades.setAnchura(anchura != null ? anchura.floatValue() : null);
        propiedades.setAltura(altura != null ? altura.floatValue() : null);
        propiedades.setRotacion(elemento.getRotacion());

        return propiedades;
    }

    /**
     * Crea el mapa de propiedades para un elemento de texto
     */
    private Map<String, Object> createPropiedadesElementoFromTexto(ElementoTexto elemento) {
        Map<String, Object> propiedades = new HashMap<>();

        propiedades.put("contenido", elemento.getContenido());
        propiedades.put("id", elemento.getId());

        if (elemento.getFuente() != null) {
            propiedades.put("fontFamily", elemento.getFuente().getNombre());
        }

        propiedades.put("fontSize", elemento.getTamanioFuente());
        propiedades.put("lineHeight", elemento.getEspaciadoLinea());

        if (elemento.getColorTexto() != null) {
            propiedades.put("color", elemento.getColorTexto().getCodigoHex());
        }

        propiedades.put("fontWeight", elemento.getPeso());
        propiedades.put("fontStyle", elemento.getEstilo());
        propiedades.put("align", elemento.getAlineacion());

        return propiedades;
    }

    /**
     * Crea el mapa de propiedades para un elemento de imagen
     */
    private Map<String, Object> createPropiedadesElementoFromImagen(ElementoImagen elemento) {
        Map<String, Object> propiedades = new HashMap<>();

        propiedades.put("id", elemento.getId());
        propiedades.put("nombreArchivo", elemento.getNombreArchivo());
        propiedades.put("urlImagen", ""); // Valor por defecto

        // Si existe un CloudinaryResource, extraer su información
        if (elemento.getCloudinaryResource() != null) {
            propiedades.put("publicId", elemento.getCloudinaryResource().getPublicId());
            propiedades.put("urlImagen", elemento.getCloudinaryResource().getUrlImagen());
        }

        return propiedades;
    }

    /**
     * Convierte un Producto a su DTO
     */
    public ProductoDTO convertToProductoDTO(Producto producto) {
        if (producto == null) {
            return null;
        }

        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setActivo(producto.getActivo());
        dto.setCreadoEn(producto.getCreadoEn());
        dto.setActualizadoEn(producto.getActualizadoEn());

        // Manejar la relación con categoría
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        // Convertir diseño personalizado si existe
        if (producto.getDisenoPersonalizado() != null) {
            dto.setDisenoPersonalizado(convertToDisenoDTO(producto.getDisenoPersonalizado()));
        }

        return dto;
    }

    /**
     * Convierte una página de Productos a página de ProductoDTO
     */
    public Page<ProductoDTO> convertToProductoDTOPage(Page<Producto> productoPage) {
        List<ProductoDTO> productoDTOs = productoPage.getContent().stream()
                .map(this::convertToProductoDTO)
                .toList();

        return new PageImpl<>(productoDTOs, productoPage.getPageable(), productoPage.getTotalElements());
    }

    /**
     * Convierte un Carrito a CarritoDTO
     */
    public CarritoDTO convertToCarritoDTO(Carrito carrito) {
        if (carrito == null) {
            return null;
        }

        CarritoDTO dto = new CarritoDTO();
        dto.setId(carrito.getId());

        // Información del usuario
        if (carrito.getUsuario() != null) {
            dto.setUsuarioId(carrito.getUsuario().getId());
            dto.setNombreUsuario(carrito.getUsuario().getNombreCompleto());
        }

        // Fechas
        dto.setCreadoEn(carrito.getCreadoEn());
        dto.setActualizadoEn(carrito.getActualizadoEn());

        // Convertir ítems
        if (carrito.getItems() != null && !carrito.getItems().isEmpty()) {
            List<ItemCarritoDTO> itemsDTO = carrito.getItems().stream()
                    .map(this::convertToItemCarritoDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemsDTO);
        }

        return dto;
    }

    /**
     * Convierte un ItemCarrito a ItemCarritoDTO
     */
    public ItemCarritoDTO convertToItemCarritoDTO(ItemCarrito item) {
        if (item == null) {
            return null;
        }

        ItemCarritoDTO dto = new ItemCarritoDTO();
        dto.setId(item.getId());
        dto.setCarritoId(item.getCarrito() != null ? item.getCarrito().getId() : null);

        // Información del producto
        if (item.getProducto() != null) {
            dto.setProductoId(item.getProducto().getId());
            dto.setProductoNombre(item.getProducto().getNombre());
            dto.setProducto(convertToProductoDTO(item.getProducto()));
        }

        // Información del diseño personalizado
        if (item.getDisenoPersonalizado() != null) {
            dto.setDisenoPersonalizadoId(item.getDisenoPersonalizado().getId());
            dto.setDisenoPersonalizado(convertToDisenoDTO(item.getDisenoPersonalizado()));
        }

        // Información del color
        if (item.getColor() != null) {
            ColorDTO colorDTO = new ColorDTO();
            colorDTO.setId(item.getColor().getId());
            colorDTO.setNombre(item.getColor().getNombre());
            colorDTO.setValorHex(item.getColor().getCodigoHex());
            dto.setColor(colorDTO);
            dto.setColorId(item.getColor().getId());
        }

        // Información de la talla
        if (item.getTalla() != null) {
            TallaDTO tallaDTO = new TallaDTO();
            tallaDTO.setId(item.getTalla().getId());
            tallaDTO.setNombre(item.getTalla().getNombre());
            dto.setTalla(tallaDTO);
            dto.setTallaId(item.getTalla().getId());
        }

        // Cantidades y precios
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        // El subtotal se calcula automáticamente en el DTO mediante getSubtotal()

        // Fechas
        dto.setFechaCreacion(item.getFechaCreacion());
        dto.setUltimaActualizacion(item.getUltimaActualizacion());

        return dto;
    }

    /**
     * Convierte un ThumbnailItem a ThumbnailItemDTO
     */
    public ThumbnailItemDTO convertToThumbnailItemDTO(ThumbnailItem thumbnail) {
        if (thumbnail == null) {
            return null;
        }

        ThumbnailItemDTO dto = new ThumbnailItemDTO();
        dto.setId(thumbnail.getId());
        dto.setItemCarritoId(thumbnail.getItemCarritoId());
        dto.setItemOrdenId(thumbnail.getItemOrdenId());

        // Información del tipo de ángulo
        if (thumbnail.getTipoAngulo() != null) {
            dto.setTipoAnguloId(thumbnail.getTipoAngulo().getId());
            dto.setNombreAngulo(thumbnail.getTipoAngulo().getNombre());
        }

        // Información del recurso Cloudinary
        if (thumbnail.getCloudinaryResource() != null) {
            CloudinaryResourceDTO resourceDTO = new CloudinaryResourceDTO();
            resourceDTO.setId(thumbnail.getCloudinaryResource().getId());
            resourceDTO.setPublicId(thumbnail.getCloudinaryResource().getPublicId());
            resourceDTO.setUrlImagen(thumbnail.getCloudinaryResource().getUrlImagen());

            dto.setCloudinaryResource(resourceDTO);
            dto.setUrl(thumbnail.getCloudinaryResource().getUrlImagen());
        }

        return dto;
    }
}
