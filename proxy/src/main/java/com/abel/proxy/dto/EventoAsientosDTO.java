package com.abel.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoAsientosDTO {

    private Long eventoId;
    private List<AsientoDTO> asientos = new ArrayList<>();

    // Constructor conveniente para cuando no hay asientos en Redis
    public EventoAsientosDTO(Long eventoId) {
        this.eventoId = eventoId;
        this.asientos = new ArrayList<>();
    }
}