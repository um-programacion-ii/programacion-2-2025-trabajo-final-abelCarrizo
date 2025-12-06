package com.abel.eventos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private Instant fecha;
    private String direccion;
    private String imagen;
    private Integer filaAsientos;
    private Integer columnaAsientos;
    private BigDecimal precioEntrada;
    private EventoTipo eventoTipo;
    private List<Integrante> integrantes;
}