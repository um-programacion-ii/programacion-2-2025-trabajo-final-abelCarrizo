package com.abel.eventos.application.port.out;

import com.abel.eventos.domain.model.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaRepositoryPort {

    Venta guardar(Venta venta);

    Optional<Venta> buscarPorId(Long id);

    List<Venta> buscarPorEventoId(Long eventoId);

    List<Venta> buscarPorUsuarioId(Long usuarioId);

    List<Venta> buscarTodas();
}