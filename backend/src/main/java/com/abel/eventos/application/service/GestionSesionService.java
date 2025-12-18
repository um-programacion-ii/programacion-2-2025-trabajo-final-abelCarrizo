package com.abel.eventos.application.service;

import com.abel.eventos.application.port.in.GestionSesionUseCase;
import com.abel.eventos.application.port.out.SesionRepositoryPort;
import com.abel.eventos.domain.model.Sesion;
import com.abel.eventos.domain.model.SesionEstado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GestionSesionService implements GestionSesionUseCase {

    private final SesionRepositoryPort sesionRepositoryPort;

    private static final int MINUTOS_EXPIRACION = 30;

    @Override
    public Sesion obtenerSesionActual(Long usuarioId) {
        return sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .filter(sesion -> !sesionExpirada(sesion))
                .orElse(null);
    }

    @Override
    public Sesion iniciarSesionCompra(Long usuarioId, Long eventoId) {
        // Eliminar sesion anterior si existe
        sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .ifPresent(sesion -> sesionRepositoryPort.eliminarPorId(sesion.getId()));

        // Crear nueva sesion
        Sesion nuevaSesion = new Sesion();
        nuevaSesion.setUsuarioId(usuarioId);
        nuevaSesion.setEventoId(eventoId);
        nuevaSesion.setEstado(SesionEstado.EVENTO_SELECCIONADO);
        nuevaSesion.setAsientosSeleccionados(new ArrayList<>());
        nuevaSesion.setCreadoEn(Instant.now());
        nuevaSesion.setUltimaActividad(Instant.now());

        return sesionRepositoryPort.guardar(nuevaSesion);
    }

    @Override
    public Sesion actualizarEstadoSesion(Long usuarioId, Sesion sesion) {
        sesion.setUltimaActividad(Instant.now());
        return sesionRepositoryPort.guardar(sesion);
    }

    @Override
    public void finalizarSesion(Long usuarioId) {
        sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .ifPresent(sesion -> sesionRepositoryPort.eliminarPorId(sesion.getId()));
    }

    @Override
    public boolean sesionExpirada(Long usuarioId) {
        return sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .map(this::sesionExpirada)
                .orElse(true);
    }

    private boolean sesionExpirada(Sesion sesion) {
        Instant limite = Instant.now().minus(MINUTOS_EXPIRACION, ChronoUnit.MINUTES);
        return sesion.getUltimaActividad().isBefore(limite);
    }
}