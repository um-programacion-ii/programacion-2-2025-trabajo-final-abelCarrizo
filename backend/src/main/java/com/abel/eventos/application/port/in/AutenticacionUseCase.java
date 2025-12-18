package com.abel.eventos.application.port.in;

import com.abel.eventos.domain.model.Usuario;

public interface AutenticacionUseCase {

    String login(String username, String password);

    void logout(String token);

    Usuario obtenerUsuarioActual(String token);

    boolean validarToken(String token);
}