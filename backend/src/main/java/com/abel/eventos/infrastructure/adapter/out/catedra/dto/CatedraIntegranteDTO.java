package com.abel.eventos.infrastructure.adapter.out.catedra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatedraIntegranteDTO {

    private String nombre;
    private String apellido;
    private String identificacion;
}