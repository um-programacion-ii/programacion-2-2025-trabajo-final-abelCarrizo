package com.abel.eventos.application.port.in;

import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.Venta;

import java.util.List;

public interface RealizarVentaUseCase {

    boolean seleccionarAsientos(Long usuarioId, Long eventoId, List<Asiento> asientos);

    boolean bloquearAsientos(Long usuarioId);

    boolean asignarPersonasAAsientos(Long usuarioId, List<Asiento> asientosConPersona);

    Venta confirmarVenta(Long usuarioId);

    void cancelarProceso(Long usuarioId);
}