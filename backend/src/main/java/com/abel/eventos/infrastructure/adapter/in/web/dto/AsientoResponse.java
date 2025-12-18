package com.abel.eventos.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsientoResponse {

    private Integer fila;
    private Integer columna;
    private String estado;
    private String persona;
}