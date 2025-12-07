package com.abel.eventos.infrastructure.adapter.in.web;

import com.abel.eventos.application.port.in.AutenticacionUseCase;
import com.abel.eventos.application.port.out.UsuarioRepositoryPort;
import com.abel.eventos.domain.model.Usuario;
import com.abel.eventos.infrastructure.adapter.in.web.dto.LoginRequest;
import com.abel.eventos.infrastructure.adapter.in.web.dto.LoginResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.RegistroRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticacionUseCase autenticacionUseCase;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = autenticacionUseCase.login(request.getUsername(), request.getPassword());

            LoginResponse response = new LoginResponse(
                    token,
                    request.getUsername(),
                    "Login exitoso"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponse response = new LoginResponse(
                    "",
                    request.getUsername(),
                    "Credenciales invalidas"
            );
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@RequestBody RegistroRequest request) {
        // Verificar si el usuario ya existe
        if (usuarioRepositoryPort.existePorUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new LoginResponse(
                    "",
                    request.getUsername(),
                    "El usuario ya existe"
            ));
        }

        if (usuarioRepositoryPort.existePorEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new LoginResponse(
                    "",
                    request.getUsername(),
                    "El email ya esta registrado"
            ));
        }

        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.getUsername());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setEmail(request.getEmail());

        usuarioRepositoryPort.guardar(nuevoUsuario);

        // Hacer login automático
        String token = autenticacionUseCase.login(request.getUsername(), request.getPassword());

        return ResponseEntity.ok(new LoginResponse(
                token,
                request.getUsername(),
                "Registro exitoso"
        ));
    }
}