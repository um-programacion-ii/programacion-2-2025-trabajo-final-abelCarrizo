package com.abel.eventos.application.port.out;

import com.abel.eventos.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorUsername(String username);

    boolean existePorUsername(String username);

    boolean existePorEmail(String email);
}