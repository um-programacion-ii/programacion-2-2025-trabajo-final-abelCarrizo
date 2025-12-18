package com.abel.eventos.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegranteResponse {

    private String nombre;
    private String apellido;
    private String identificacion;
}