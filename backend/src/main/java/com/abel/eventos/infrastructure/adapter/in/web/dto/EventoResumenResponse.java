package com.abel.eventos.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoResumenResponse {

    private Long id;
    private String titulo;
    private String resumen;
    private Instant fecha;
    private BigDecimal precioEntrada;
    private String tipoEvento;
}