package com.abel.eventos.infrastructure.adapter.in.web;

import com.abel.eventos.application.port.in.RealizarVentaUseCase;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.Venta;
import com.abel.eventos.infrastructure.adapter.in.web.dto.AsientoRequest;
import com.abel.eventos.infrastructure.adapter.in.web.dto.AsientoResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.AsignarPersonasRequest;
import com.abel.eventos.infrastructure.adapter.in.web.dto.MensajeResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.SeleccionarAsientosRequest;
import com.abel.eventos.infrastructure.adapter.in.web.dto.VentaResponse;
import com.abel.eventos.infrastructure.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final RealizarVentaUseCase realizarVentaUseCase;
    private final JwtService jwtService;

    @PostMapping("/seleccionar")
    public ResponseEntity<MensajeResponse> seleccionarAsientos(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SeleccionarAsientosRequest request) {

        Long usuarioId = extraerUsuarioId(authHeader);

        List<Asiento> asientos = request.getAsientos().stream()
                .map(this::toAsientoDomain)
                .toList();

        try {
            boolean resultado = realizarVentaUseCase.seleccionarAsientos(
                    usuarioId,
                    request.getEventoId(),
                    asientos
            );

            if (resultado) {
                return ResponseEntity.ok(new MensajeResponse(true, "Asientos seleccionados correctamente"));
            } else {
                return ResponseEntity.badRequest().body(new MensajeResponse(false, "Algunos asientos no estan disponibles"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensajeResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/bloquear")
    public ResponseEntity<MensajeResponse> bloquearAsientos(
            @RequestHeader("Authorization") String authHeader) {

        Long usuarioId = extraerUsuarioId(authHeader);

        try {
            boolean resultado = realizarVentaUseCase.bloquearAsientos(usuarioId);

            if (resultado) {
                return ResponseEntity.ok(new MensajeResponse(true, "Asientos bloqueados por 5 minutos"));
            } else {
                return ResponseEntity.badRequest().body(new MensajeResponse(false, "No se pudieron bloquear los asientos"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensajeResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/asignar-personas")
    public ResponseEntity<MensajeResponse> asignarPersonas(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AsignarPersonasRequest request) {

        Long usuarioId = extraerUsuarioId(authHeader);

        List<Asiento> asientos = request.getAsientos().stream()
                .map(this::toAsientoDomain)
                .toList();

        try {
            boolean resultado = realizarVentaUseCase.asignarPersonasAAsientos(usuarioId, asientos);

            if (resultado) {
                return ResponseEntity.ok(new MensajeResponse(true, "Personas asignadas correctamente"));
            } else {
                return ResponseEntity.badRequest().body(new MensajeResponse(false, "Error al asignar personas"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensajeResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/confirmar")
    public ResponseEntity<VentaResponse> confirmarVenta(
            @RequestHeader("Authorization") String authHeader) {

        Long usuarioId = extraerUsuarioId(authHeader);

        try {
            Venta venta = realizarVentaUseCase.confirmarVenta(usuarioId);
            return ResponseEntity.ok(toVentaResponse(venta));
        } catch (RuntimeException e) {
            VentaResponse errorResponse = new VentaResponse(
                    null,
                    null,
                    null,
                    null,
                    false,
                    e.getMessage(),
                    new ArrayList<>()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/cancelar")
    public ResponseEntity<MensajeResponse> cancelarProceso(
            @RequestHeader("Authorization") String authHeader) {

        Long usuarioId = extraerUsuarioId(authHeader);

        realizarVentaUseCase.cancelarProceso(usuarioId);
        return ResponseEntity.ok(new MensajeResponse(true, "Proceso cancelado correctamente"));
    }

    // === Métodos auxiliares ===

    private Long extraerUsuarioId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.extraerUsername(token);
        // TODO: Obtener el ID real del usuario desde el username
        return 1L;
    }

    private Asiento toAsientoDomain(AsientoRequest request) {
        Asiento asiento = new Asiento();
        asiento.setFila(request.getFila());
        asiento.setColumna(request.getColumna());
        asiento.setPersona(request.getPersona());
        return asiento;
    }

    private VentaResponse toVentaResponse(Venta venta) {
        List<AsientoResponse> asientos = new ArrayList<>();

        if (venta.getAsientos() != null) {
            asientos = venta.getAsientos().stream()
                    .map(a -> new AsientoResponse(
                            a.getFila(),
                            a.getColumna(),
                            a.getEstado() != null ? a.getEstado().name() : null,
                            a.getPersona()
                    ))
                    .toList();
        }

        return new VentaResponse(
                venta.getId(),
                venta.getEventoId(),
                venta.getFechaVenta(),
                venta.getPrecioVenta(),
                venta.getResultado(),
                venta.getDescripcion(),
                asientos
        );
    }
}