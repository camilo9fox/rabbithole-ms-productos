package com.rabbithole.productos.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rabbithole.productos.dto.AnguloDTO;
import com.rabbithole.productos.dto.DisenoPersonalizadoDTO;
import com.rabbithole.productos.dto.ElementoDTO;
import com.rabbithole.productos.dto.ElementoDisenoDTO;
import com.rabbithole.productos.model.AnguloDiseno;
import com.rabbithole.productos.model.DisenoPersonalizado;
import com.rabbithole.productos.model.ElementoImagen;
import com.rabbithole.productos.model.ElementoTexto;

@Component
public class DisenoPersonalizadoMapper {

    public DisenoPersonalizadoDTO toDTO(DisenoPersonalizado disenoPersonalizado) {
        if (disenoPersonalizado == null) {
            return null;
        }
        
        return toDTO(disenoPersonalizado, disenoPersonalizado.getAngulos() != null ? 
                     new ArrayList<>(disenoPersonalizado.getAngulos()) : null);
    }
    
    public DisenoPersonalizadoDTO toDTO(DisenoPersonalizado disenoPersonalizado, List<AnguloDiseno> angulos) {
        if (disenoPersonalizado == null) {
            return null;
        }
        
        DisenoPersonalizadoDTO dto = new DisenoPersonalizadoDTO();
        dto.setId(disenoPersonalizado.getId());
        dto.setNombre(disenoPersonalizado.getNombre());
        dto.setUsuarioId(disenoPersonalizado.getUsuarioId());

        dto.setColorId(disenoPersonalizado.getColor() != null ? disenoPersonalizado.getColor().getId() : null);
        dto.setTallaId(disenoPersonalizado.getTalla() != null ? disenoPersonalizado.getTalla().getId() : null);
        
        dto.setPrecio(disenoPersonalizado.getPrecio());
        dto.setEstadoId(disenoPersonalizado.getEstado() != null ? disenoPersonalizado.getEstado().getId() : null);
        dto.setMotivoRechazo(disenoPersonalizado.getMotivoRechazo());
        dto.setNotasModificacion(disenoPersonalizado.getNotasModificacion());
        dto.setCreadoPorAdmin(disenoPersonalizado.getCreadoPorAdmin());
        dto.setCreadoEn(disenoPersonalizado.getCreadoEn());
        dto.setActualizadoEn(disenoPersonalizado.getActualizadoEn());
        
        if (angulos != null) {
            dto.setAngulos(angulos.stream()
                .map(this::anguloToDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private AnguloDTO anguloToDTO(AnguloDiseno angulo) {
        if (angulo == null) {
            return null;
        }
        
        AnguloDTO dto = new AnguloDTO();
        dto.setId(angulo.getId());
        dto.setTipoAnguloId(angulo.getTipoAngulo() != null ? angulo.getTipoAngulo().getId() : null);
        dto.setThumbnailUrl(angulo.getThumbnailUrl());
        
        if (angulo.getElementoTexto() != null) {
            dto.setElemento(elementoTextoToElementoDTO(angulo.getElementoTexto()));
        } else if (angulo.getElementoImagen() != null) {
            dto.setElemento(elementoImagenToElementoDTO(angulo.getElementoImagen()));
        }
        
        return dto;
    }
    
    private ElementoDTO elementoTextoToElementoDTO(ElementoTexto elementoTexto) {
        if (elementoTexto == null) {
            return null;
        }
        
        ElementoDTO dto = new ElementoDTO();
        dto.setTipo("TEXTO");
        
        ElementoDisenoDTO propDiseno = new ElementoDisenoDTO();
        propDiseno.setPosicionX(elementoTexto.getPosicionX() != null ? elementoTexto.getPosicionX().floatValue() : null);
        propDiseno.setPosicionY(elementoTexto.getPosicionY() != null ? elementoTexto.getPosicionY().floatValue() : null);
        propDiseno.setAnchura(elementoTexto.getAnchura() != null ? elementoTexto.getAnchura().floatValue() : null);
        propDiseno.setAltura(elementoTexto.getAltura() != null ? elementoTexto.getAltura().floatValue() : null);
        propDiseno.setRotacion(elementoTexto.getRotacion());
        propDiseno.setProfundidad(elementoTexto.getProfundidad());
        dto.setPropiedadesDiseno(propDiseno);
        
        Map<String, Object> propTexto = new HashMap<>();
        propTexto.put("id", elementoTexto.getId());
        propTexto.put("contenido", elementoTexto.getContenido());
        propTexto.put("fontFamily", elementoTexto.getFuente() != null ? elementoTexto.getFuente().getNombre() : null);
        propTexto.put("fontSize", elementoTexto.getTamanoFuente());
        propTexto.put("color", elementoTexto.getColorTexto() != null ? elementoTexto.getColorTexto().getCodigoHex() : null);
        propTexto.put("fontWeight", elementoTexto.getPeso());
        propTexto.put("fontStyle", elementoTexto.getEstilo());
        propTexto.put("lineHeight", elementoTexto.getEspaciadoLinea());
        propTexto.put("align", elementoTexto.getAlineacion());
        dto.setPropiedadesElemento(propTexto);
        
        return dto;
    }
    
    private ElementoDTO elementoImagenToElementoDTO(ElementoImagen elementoImagen) {
        if (elementoImagen == null) {
            return null;
        }
        
        ElementoDTO dto = new ElementoDTO();
        dto.setTipo("IMAGEN");
        
        ElementoDisenoDTO propDiseno = new ElementoDisenoDTO();
        propDiseno.setPosicionX(elementoImagen.getPosicionX() != null ? elementoImagen.getPosicionX().floatValue() : null);
        propDiseno.setPosicionY(elementoImagen.getPosicionY() != null ? elementoImagen.getPosicionY().floatValue() : null);
        propDiseno.setAnchura(elementoImagen.getAnchura() != null ? elementoImagen.getAnchura().floatValue() : null);
        propDiseno.setAltura(elementoImagen.getAltura() != null ? elementoImagen.getAltura().floatValue() : null);  
        propDiseno.setRotacion(elementoImagen.getRotacion());
        propDiseno.setProfundidad(elementoImagen.getProfundidad());
        dto.setPropiedadesDiseno(propDiseno);
        
        Map<String, Object> propImagen = new HashMap<>();
        propImagen.put("id", elementoImagen.getId());
        
        propImagen.put("urlImagen", elementoImagen.getUrlImagen());
        propImagen.put("publicId", elementoImagen.getPublicId());
        propImagen.put("tipoImagen", elementoImagen.getTipoImagen());
        propImagen.put("nombreArchivo", elementoImagen.getNombreArchivo());
        propImagen.put("tamanoArchivo", elementoImagen.getTamanoArchivo());
        
        dto.setPropiedadesElemento(propImagen);
        
        return dto;
    }

}
