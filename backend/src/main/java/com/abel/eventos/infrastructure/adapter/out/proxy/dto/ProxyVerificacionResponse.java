package com.abel.eventos.infrastructure.adapter.out.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyVerificacionResponse {

    private Long eventoId;
    private Boolean disponible;
    private Integer asientosConsultados;
}