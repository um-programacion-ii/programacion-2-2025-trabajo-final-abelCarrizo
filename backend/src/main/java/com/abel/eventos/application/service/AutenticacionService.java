package com.abel.eventos.application.service;

import com.abel.eventos.application.port.in.AutenticacionUseCase;
import com.abel.eventos.application.port.out.UsuarioRepositoryPort;
import com.abel.eventos.domain.model.Usuario;
import com.abel.eventos.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticacionService implements AutenticacionUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public String login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            return jwtService.generarToken(username);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Credenciales invalidas");
        }
    }

    @Override
    public void logout(String token) {
        // En JWT stateless, el logout se maneja en el cliente
        // eliminando el token. Opcionalmente podriamos implementar
        // una lista negra de tokens en Redis/BD.
    }

    @Override
    public Usuario obtenerUsuarioActual(String token) {
        String username = jwtService.extraerUsername(token);
        return usuarioRepositoryPort.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public boolean validarToken(String token) {
        try {
            String username = jwtService.extraerUsername(token);
            return !jwtService.esTokenExpirado(token) &&
                    usuarioRepositoryPort.existePorUsername(username);
        } catch (Exception e) {
            return false;
        }
    }
}