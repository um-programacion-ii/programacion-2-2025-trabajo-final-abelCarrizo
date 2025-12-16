package com.abel.eventos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoEntity {

    @Id
    // SIN @GeneratedValue - Los IDs vienen de Cátedra
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 500)
    private String resumen;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Instant fecha;

    @Column(length = 300)
    private String direccion;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false)
    private Integer filaAsientos;

    @Column(nullable = false)
    private Integer columnaAsientos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioEntrada;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "evento_tipo_id")
    private EventoTipoEntity eventoTipo;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "evento_integrantes",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "integrante_id")
    )
    private List<IntegranteEntity> integrantes;
}