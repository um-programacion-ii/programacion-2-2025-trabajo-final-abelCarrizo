package com.abel.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsientoDTO {

    private Integer fila;
    private Integer columna;
    private String estado;      // "Bloqueado" o "Vendido"
    private String expira;      // Solo presente si estado = "Bloqueado"
}