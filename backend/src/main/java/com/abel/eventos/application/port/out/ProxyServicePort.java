package com.abel.eventos.application.port.out;

import com.abel.eventos.domain.model.Asiento;

import java.util.List;

public interface ProxyServicePort {

    List<Asiento> obtenerAsientosOcupados(Long eventoId);

    boolean verificarDisponibilidad(Long eventoId, List<Asiento> asientos);
}