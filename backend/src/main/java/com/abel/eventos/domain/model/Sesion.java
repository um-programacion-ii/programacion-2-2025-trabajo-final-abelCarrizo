package com.abel.eventos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sesion {

    private Long id;
    private Long usuarioId;
    private Long eventoId;
    private SesionEstado estado;
    private List<Asiento> asientosSeleccionados;
    private Instant creadoEn;
    private Instant ultimaActividad;
}