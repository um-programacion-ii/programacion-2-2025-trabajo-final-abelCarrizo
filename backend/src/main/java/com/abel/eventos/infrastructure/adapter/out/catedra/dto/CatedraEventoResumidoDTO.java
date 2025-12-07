package com.abel.eventos.infrastructure.adapter.out.catedra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatedraEventoResumidoDTO {

    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private String fecha;
    private BigDecimal precioEntrada;
    private CatedraEventoTipoDTO eventoTipo;
}