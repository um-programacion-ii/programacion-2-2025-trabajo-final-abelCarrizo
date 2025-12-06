package com.abel.eventos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asiento {

    private Long id;
    private Integer fila;
    private Integer columna;
    private AsientoEstado estado;
    private String persona;
}