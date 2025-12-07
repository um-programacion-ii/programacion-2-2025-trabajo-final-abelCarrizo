package com.abel.eventos.infrastructure.adapter.out.catedra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatedraVentaRequestDTO {

    private Long eventoId;
    private String fecha;
    private BigDecimal precioVenta;
    private List<CatedraAsientoDTO> asientos;
}