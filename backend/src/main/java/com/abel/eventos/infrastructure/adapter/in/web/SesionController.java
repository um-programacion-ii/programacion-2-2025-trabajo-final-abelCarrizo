package com.abel.eventos.infrastructure.adapter.in.web;

import com.abel.eventos.application.port.in.GestionSesionUseCase;
import com.abel.eventos.domain.model.Sesion;
import com.abel.eventos.infrastructure.adapter.in.web.dto.AsientoResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.IniciarSesionRequest;
import com.abel.eventos.infrastructure.adapter.in.web.dto.SesionResponse;
import com.abel.eventos.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sesion")
@RequiredArgsConstructor
public class SesionController {

    private final GestionSesionUseCase gestionSesionUseCase;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<SesionResponse> obtenerSesionActual(
            @RequestHeader("Authorization") String authHeader) {

        Long usuarioId = extraerUsuarioId(authHeader);
        Sesion sesion = gestionSesionUseCase.obtenerSesionActual(usuarioId);

        if (sesion == null) {
            return ResponseEntity.ok(new SesionResponse(
                    null,
                    null,
                    "SIN_SESION",
                    new ArrayList<>(),
                    "No hay sesion activa"
            ));
        }

        return ResponseEntity.ok(toSesionResponse(sesion, "Sesion activa"));
    }

    @PostMapping("/iniciar")
    public ResponseEntity<SesionResponse> iniciarSesion(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody IniciarSesionRequest request) {

        Long usuarioId = extraerUsuarioId(authHeader);

        try {
            Sesion sesion = gestionSesionUseCase.iniciarSesionCompra(usuarioId, request.getEventoId());
            return ResponseEntity.ok(toSesionResponse(sesion, "Sesion iniciada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SesionResponse(
                    null,
                    null,
                    "ERROR",
                    new ArrayList<>(),
                    e.getMessage()
            ));
        }
    }

    @PostMapping("/finalizar")
    public ResponseEntity<SesionResponse> finalizarSesion(
            @RequestHeader("Authorization") String authHeader) {

        Long usuarioId = extraerUsuarioId(authHeader);
        gestionSesionUseCase.finalizarSesion(usuarioId);

        return ResponseEntity.ok(new SesionResponse(
                null,
                null,
                "FINALIZADA",
                new ArrayList<>(),
                "Sesion finalizada correctamente"
        ));
    }

    // === Métodos auxiliares ===

    private Long extraerUsuarioId(String authHeader) {
        // Por ahora retornamos un ID fijo.
        // En una implementación completa, extraeríamos el ID del token.
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.extraerUsername(token);
        // TODO: Obtener el ID real del usuario desde el username
        return 1L;
    }

    private SesionResponse toSesionResponse(Sesion sesion, String mensaje) {
        List<AsientoResponse> asientos = new ArrayList<>();

        if (sesion.getAsientosSeleccionados() != null) {
            asientos = sesion.getAsientosSeleccionados().stream()
                    .map(a -> new AsientoResponse(
                            a.getFila(),
                            a.getColumna(),
                            a.getEstado() != null ? a.getEstado().name() : null,
                            a.getPersona()
                    ))
                    .toList();
        }

        return new SesionResponse(
                sesion.getId(),
                sesion.getEventoId(),
                sesion.getEstado() != null ? sesion.getEstado().name() : null,
                asientos,
                mensaje
        );
    }
}