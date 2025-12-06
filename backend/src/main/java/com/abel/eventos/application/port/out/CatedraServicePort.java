package com.abel.eventos.application.port.out;

import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.domain.model.Venta;
import com.abel.eventos.domain.model.Asiento;

import java.math.BigDecimal;
import java.util.List;

public interface CatedraServicePort {

    List<Evento> obtenerEventosResumidos();

    List<Evento> obtenerEventosCompletos();

    Evento obtenerEventoPorId(Long id);

    boolean bloquearAsientos(Long eventoId, List<Asiento> asientos);

    Venta realizarVenta(Long eventoId, BigDecimal precioVenta, List<Asiento> asientos);

    List<Venta> listarVentas();

    Venta obtenerVentaPorId(Long id);
}