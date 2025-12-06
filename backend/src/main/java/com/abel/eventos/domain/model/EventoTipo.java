package com.abel.eventos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoTipo {

    private Long id;
    private String nombre;
    private String descripcion;
}