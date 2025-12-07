package com.abel.eventos.infrastructure.adapter.in.web;

import com.abel.eventos.application.port.in.GestionEventosUseCase;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.infrastructure.adapter.in.web.dto.AsientoResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.EventoDetalleResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.EventoResumenResponse;
import com.abel.eventos.infrastructure.adapter.in.web.dto.IntegranteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final GestionEventosUseCase gestionEventosUseCase;

    @GetMapping
    public ResponseEntity<List<EventoResumenResponse>> listarEventos() {
        List<Evento> eventos = gestionEventosUseCase.listarEventosResumidos();

        List<EventoResumenResponse> response = eventos.stream()
                .map(this::toResumenResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDetalleResponse> obtenerEvento(@PathVariable Long id) {
        try {
            Evento evento = gestionEventosUseCase.obtenerEventoDetalle(id);
            return ResponseEntity.ok(toDetalleResponse(evento));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/asientos")
    public ResponseEntity<List<AsientoResponse>> obtenerAsientos(@PathVariable Long id) {
        try {
            List<Asiento> asientos = gestionEventosUseCase.obtenerAsientosEvento(id);

            List<AsientoResponse> response = asientos.stream()
                    .map(this::toAsientoResponse)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === Métodos de conversión ===

    private EventoResumenResponse toResumenResponse(Evento evento) {
        return new EventoResumenResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getResumen(),
                evento.getFecha(),
                evento.getPrecioEntrada(),
                evento.getEventoTipo() != null ? evento.getEventoTipo().getNombre() : null
        );
    }

    private EventoDetalleResponse toDetalleResponse(Evento evento) {
        List<IntegranteResponse> integrantes = null;

        if (evento.getIntegrantes() != null) {
            integrantes = evento.getIntegrantes().stream()
                    .map(i -> new IntegranteResponse(
                            i.getNombre(),
                            i.getApellido(),
                            i.getIdentificacion()
                    ))
                    .toList();
        }

        return new EventoDetalleResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getResumen(),
                evento.getDescripcion(),
                evento.getFecha(),
                evento.getDireccion(),
                evento.getImagen(),
                evento.getFilaAsientos(),
                evento.getColumnaAsientos(),
                evento.getPrecioEntrada(),
                evento.getEventoTipo() != null ? evento.getEventoTipo().getNombre() : null,
                integrantes
        );
    }

    private AsientoResponse toAsientoResponse(Asiento asiento) {
        return new AsientoResponse(
                asiento.getFila(),
                asiento.getColumna(),
                asiento.getEstado() != null ? asiento.getEstado().name() : "LIBRE",
                asiento.getPersona()
        );
    }
}