package com.abel.eventos.infrastructure.adapter.out.catedra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatedraAsientoDTO {

    private Integer fila;
    private Integer columna;
    private String estado;
    private String persona;
}