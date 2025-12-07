package com.abel.eventos.application.service;

import com.abel.eventos.application.port.in.RealizarVentaUseCase;
import com.abel.eventos.application.port.out.CatedraServicePort;
import com.abel.eventos.application.port.out.EventoRepositoryPort;
import com.abel.eventos.application.port.out.ProxyServicePort;
import com.abel.eventos.application.port.out.SesionRepositoryPort;
import com.abel.eventos.application.port.out.VentaRepositoryPort;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.domain.model.Sesion;
import com.abel.eventos.domain.model.SesionEstado;
import com.abel.eventos.domain.model.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RealizarVentaService implements RealizarVentaUseCase {

    private final SesionRepositoryPort sesionRepositoryPort;
    private final VentaRepositoryPort ventaRepositoryPort;
    private final EventoRepositoryPort eventoRepositoryPort;
    private final ProxyServicePort proxyServicePort;
    private final CatedraServicePort catedraServicePort;

    private static final int MAX_ASIENTOS = 4;

    @Override
    public boolean seleccionarAsientos(Long usuarioId, Long eventoId, List<Asiento> asientos) {
        // Validar cantidad maxima de asientos
        if (asientos.size() > MAX_ASIENTOS) {
            throw new RuntimeException("No se pueden seleccionar mas de " + MAX_ASIENTOS + " asientos");
        }

        // Verificar disponibilidad
        if (!proxyServicePort.verificarDisponibilidad(eventoId, asientos)) {
            return false;
        }

        // Actualizar sesion con asientos seleccionados
        Sesion sesion = obtenerSesionActiva(usuarioId);
        sesion.setEventoId(eventoId);
        sesion.setAsientosSeleccionados(asientos);
        sesion.setEstado(SesionEstado.ASIENTOS_SELECCIONADOS);
        sesion.setUltimaActividad(Instant.now());
        sesionRepositoryPort.guardar(sesion);

        return true;
    }

    @Override
    public boolean bloquearAsientos(Long usuarioId) {
        Sesion sesion = obtenerSesionActiva(usuarioId);

        // Bloquear asientos en el servicio de catedra
        boolean bloqueado = catedraServicePort.bloquearAsientos(
                sesion.getEventoId(),
                sesion.getAsientosSeleccionados()
        );

        if (bloqueado) {
            sesion.setEstado(SesionEstado.CARGANDO_DATOS);
            sesion.setUltimaActividad(Instant.now());
            sesionRepositoryPort.guardar(sesion);
        }

        return bloqueado;
    }

    @Override
    public boolean asignarPersonasAAsientos(Long usuarioId, List<Asiento> asientosConPersona) {
        Sesion sesion = obtenerSesionActiva(usuarioId);

        // Validar que la cantidad de asientos coincida
        if (asientosConPersona.size() != sesion.getAsientosSeleccionados().size()) {
            return false;
        }

        // Actualizar asientos con nombres de personas
        sesion.setAsientosSeleccionados(asientosConPersona);
        sesion.setEstado(SesionEstado.CONFIRMANDO_VENTA);
        sesion.setUltimaActividad(Instant.now());
        sesionRepositoryPort.guardar(sesion);

        return true;
    }

    @Override
    public Venta confirmarVenta(Long usuarioId) {
        Sesion sesion = obtenerSesionActiva(usuarioId);

        // Obtener precio del evento desde Catedra
        Evento evento = catedraServicePort.obtenerEventoPorId(sesion.getEventoId());
        if (evento == null) {
            throw new RuntimeException("Evento no encontrado");
        }

        // Realizar venta en el servicio de catedra
        Venta venta = catedraServicePort.realizarVenta(
                sesion.getEventoId(),
                evento.getPrecioEntrada(),
                sesion.getAsientosSeleccionados()
        );

        // Guardar venta localmente
        venta.setFechaVenta(Instant.now());
        Venta ventaGuardada = ventaRepositoryPort.guardar(venta);

        // Finalizar sesion
        sesionRepositoryPort.eliminarPorId(sesion.getId());

        return ventaGuardada;
    }

    @Override
    public void cancelarProceso(Long usuarioId) {
        sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .ifPresent(sesion -> sesionRepositoryPort.eliminarPorId(sesion.getId()));
    }

    private Sesion obtenerSesionActiva(Long usuarioId) {
        return sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("No hay sesion activa para el usuario"));
    }
}