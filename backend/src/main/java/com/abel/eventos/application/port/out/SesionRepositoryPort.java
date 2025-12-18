package com.abel.eventos.application.port.out;

import com.abel.eventos.domain.model.Sesion;

import java.util.Optional;

public interface SesionRepositoryPort {

    Sesion guardar(Sesion sesion);

    Optional<Sesion> buscarPorId(Long id);

    Optional<Sesion> buscarPorUsuarioId(Long usuarioId);

    void eliminarPorId(Long id);

    void eliminarSesionesExpiradas();
}