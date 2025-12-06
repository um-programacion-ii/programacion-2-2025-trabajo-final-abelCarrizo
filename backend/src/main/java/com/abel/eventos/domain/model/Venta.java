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
public class Venta {

    private Long id;
    private Long eventoId;
    private Instant fechaVenta;
    private BigDecimal precioVenta;
    private Boolean resultado;
    private String descripcion;
    private List<Asiento> asientos;
}