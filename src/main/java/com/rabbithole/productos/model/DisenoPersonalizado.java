package com.rabbithole.productos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un diseño personalizado creado por un usuario.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "DISENOS_PERSONALIZADOS")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DisenoPersonalizado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "USUARIO_ID")
    private Long usuarioId;
    
    @Lob
    @Column(name = "DETALLE")
    private String detalle = "Polera Personalizada";
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER para evitar problemas de serialización
    @JoinColumn(name = "COLOR_ID", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Color color; // Color ya implementa Serializable
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER para evitar problemas de serialización
    @JoinColumn(name = "TALLA_ID", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Talla talla; // Talla ya implementa Serializable
    
    @NotNull
    @Positive
    @Column(name = "PRECIO", nullable = false)
    private BigDecimal precio;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER para evitar problemas de serialización
    @JoinColumn(name = "ESTADO_ID", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private EstadoDiseno estado; // EstadoDiseno ya implementa Serializable
    
    @Lob
    @Column(name = "MOTIVO_RECHAZO")
    private String motivoRechazo;
    
    @Lob
    @Column(name = "NOTAS_MODIFICACION")
    private String notasModificacion;
    
    @NotNull
    @Column(name = "CREADO_POR_ADMIN", nullable = false)
    private Boolean creadoPorAdmin = false;
    
    @NotNull
    @Column(name = "CREADO_EN", nullable = false)
    private LocalDateTime creadoEn;
    
    @NotNull
    @Column(name = "ACTUALIZADO_EN", nullable = false)
    private LocalDateTime actualizadoEn;
    
    // Nota: Los campos publico y activo se eliminaron porque no existen en el esquema de la base de datos
    
    @JsonManagedReference
    @OneToMany(mappedBy = "disenoPersonalizado", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AnguloDiseno> angulos = new HashSet<>();
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Añade un ángulo al diseño personalizado.
     * 
     * @param angulo El ángulo a añadir
     * @return El diseño personalizado con el ángulo añadido
     */
    public DisenoPersonalizado addAngulo(AnguloDiseno angulo) {
        angulos.add(angulo);
        angulo.setDisenoPersonalizado(this);
        return this;
    }
    
    /**
     * Elimina un ángulo del diseño personalizado.
     * 
     * @param angulo El ángulo a eliminar
     * @return El diseño personalizado con el ángulo eliminado
     */
    public DisenoPersonalizado removeAngulo(AnguloDiseno angulo) {
        angulos.remove(angulo);
        angulo.setDisenoPersonalizado(null);
        return this;
    }
    
    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
