package com.abel.eventos.application.port.in;

import com.abel.eventos.domain.model.Sesion;

public interface GestionSesionUseCase {

    Sesion obtenerSesionActual(Long usuarioId);

    Sesion iniciarSesionCompra(Long usuarioId, Long eventoId);

    Sesion actualizarEstadoSesion(Long usuarioId, Sesion sesion);

    void finalizarSesion(Long usuarioId);

    boolean sesionExpirada(Long usuarioId);
}
