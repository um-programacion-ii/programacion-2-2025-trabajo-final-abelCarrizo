package com.abel.eventos.infrastructure.adapter.out.catedra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatedraEventoCompletoDTO {

    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private String fecha;
    private String direccion;
    private String imagen;
    private Integer filaAsientos;
    private Integer columnAsientos;
    private BigDecimal precioEntrada;
    private CatedraEventoTipoDTO eventoTipo;
    private List<CatedraIntegranteDTO> integrantes;
}