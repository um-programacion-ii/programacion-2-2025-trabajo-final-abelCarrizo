package com.abel.eventos.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionResponse {

    private Long id;
    private Long eventoId;
    private String estado;
    private List<AsientoResponse> asientosSeleccionados;
    private String mensaje;
}