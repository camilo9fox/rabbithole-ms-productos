package com.rabbithole.productos.service;

import com.rabbithole.productos.dto.AnguloDTO;
import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import com.rabbithole.productos.dto.ElementoDisenoDTO;
import com.rabbithole.productos.dto.ElementoDTO;
import com.rabbithole.productos.dto.ProductoDTO;
import com.rabbithole.productos.model.AnguloDiseno;
import com.rabbithole.productos.model.DisenoPersonalizado;
import com.rabbithole.productos.model.ElementoImagen;
import com.rabbithole.productos.model.ElementoTexto;
import com.rabbithole.productos.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
