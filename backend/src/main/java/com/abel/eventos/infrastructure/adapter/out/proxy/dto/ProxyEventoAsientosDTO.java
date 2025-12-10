package com.abel.eventos.infrastructure.adapter.out.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyEventoAsientosDTO {

    private Long eventoId;
    private List<ProxyAsientoDTO> asientos = new ArrayList<>();
}