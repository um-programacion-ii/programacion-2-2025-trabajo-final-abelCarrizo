package com.abel.eventos.application.port.in;

import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.domain.model.Asiento;

import java.util.List;

public interface GestionEventosUseCase {

    List<Evento> listarEventosResumidos();

    Evento obtenerEventoDetalle(Long eventoId);

    List<Asiento> obtenerAsientosEvento(Long eventoId);

    void sincronizarEventos();
}