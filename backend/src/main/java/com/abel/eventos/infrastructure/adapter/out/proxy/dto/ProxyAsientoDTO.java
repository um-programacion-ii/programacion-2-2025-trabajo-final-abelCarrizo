package com.abel.eventos.infrastructure.adapter.out.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyAsientoDTO {

    private Integer fila;
    private Integer columna;
    private String estado;   // "Bloqueado" o "Vendido"
    private String expira;   // Solo si está bloqueado
}