package com.abel.eventos.application.port.out;

import com.abel.eventos.domain.model.Evento;

import java.util.List;
import java.util.Optional;

public interface EventoRepositoryPort {

    Evento guardar(Evento evento);

    Optional<Evento> buscarPorId(Long id);

    List<Evento> buscarTodos();

    void eliminarPorId(Long id);

    boolean existePorId(Long id);
}
