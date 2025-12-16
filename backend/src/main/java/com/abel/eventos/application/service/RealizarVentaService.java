package com.abel.eventos.application.service;

import com.abel.eventos.application.port.in.RealizarVentaUseCase;
import com.abel.eventos.application.port.out.CatedraServicePort;
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
    private final ProxyServicePort proxyServicePort;
    private final CatedraServicePort catedraServicePort;

    private static final int MAX_ASIENTOS = 4;

    @Override
    public boolean seleccionarAsientos(Long usuarioId, Long eventoId, List<Asiento> asientos) {
        // Obtener sesión activa primero
        Sesion sesion = obtenerSesionActiva(usuarioId);

        // Validar que el evento coincida con la sesión
        if (!sesion.getEventoId().equals(eventoId)) {
            throw new RuntimeException(
                    String.format("El evento %d no coincide con la sesión activa (evento %d)",
                            eventoId, sesion.getEventoId()));
        }

        // Validar cantidad maxima de asientos
        if (asientos.size() > MAX_ASIENTOS) {
            throw new RuntimeException("No se pueden seleccionar mas de " + MAX_ASIENTOS + " asientos");
        }

        // Obtener evento para validar dimensiones
        Evento evento = catedraServicePort.obtenerEventoPorId(eventoId);
        if (evento == null) {
            throw new RuntimeException("Evento no encontrado: " + eventoId);
        }

        // Validar que los asientos esten dentro del rango del evento
        validarRangoAsientos(asientos, evento);

        // Verificar disponibilidad
        if (!proxyServicePort.verificarDisponibilidad(eventoId, asientos)) {
            return false;
        }

        // Actualizar sesion con asientos seleccionados
        sesion.setAsientosSeleccionados(asientos);
        sesion.setEstado(SesionEstado.ASIENTOS_SELECCIONADOS);
        sesion.setUltimaActividad(Instant.now());
        sesionRepositoryPort.guardar(sesion);

        return true;
    }

    private void validarRangoAsientos(List<Asiento> asientos, Evento evento) {
        int maxFilas = evento.getFilaAsientos();
        int maxColumnas = evento.getColumnaAsientos();

        for (Asiento asiento : asientos) {
            if (asiento.getFila() < 1 || asiento.getFila() > maxFilas) {
                throw new RuntimeException(
                        String.format("Fila %d fuera de rango. El evento tiene filas 1-%d",
                                asiento.getFila(), maxFilas));
            }
            if (asiento.getColumna() < 1 || asiento.getColumna() > maxColumnas) {
                throw new RuntimeException(
                        String.format("Columna %d fuera de rango. El evento tiene columnas 1-%d",
                                asiento.getColumna(), maxColumnas));
            }
        }
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
            throw new RuntimeException("La cantidad de asientos no coincide con los seleccionados");
        }

        // Validar que los asientos sean los mismos que los seleccionados
        validarAsientosCoinciden(asientosConPersona, sesion.getAsientosSeleccionados());

        // Validar que todos tengan persona asignada
        for (Asiento asiento : asientosConPersona) {
            if (asiento.getPersona() == null || asiento.getPersona().trim().isEmpty()) {
                throw new RuntimeException(
                        String.format("El asiento fila %d, columna %d no tiene persona asignada",
                                asiento.getFila(), asiento.getColumna()));
            }
        }

        // Actualizar asientos con nombres de personas
        sesion.setAsientosSeleccionados(asientosConPersona);
        sesion.setEstado(SesionEstado.CONFIRMANDO_VENTA);
        sesion.setUltimaActividad(Instant.now());
        sesionRepositoryPort.guardar(sesion);

        return true;
    }

    private void validarAsientosCoinciden(List<Asiento> asientosConPersona, List<Asiento> asientosSeleccionados) {
        for (Asiento conPersona : asientosConPersona) {
            boolean encontrado = asientosSeleccionados.stream()
                    .anyMatch(sel -> sel.getFila().equals(conPersona.getFila())
                            && sel.getColumna().equals(conPersona.getColumna()));
            if (!encontrado) {
                throw new RuntimeException(
                        String.format("El asiento fila %d, columna %d no fue seleccionado previamente",
                                conPersona.getFila(), conPersona.getColumna()));
            }
        }
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

        // Asignar el usuario que realizó la venta
        venta.setUsuarioId(usuarioId);

        // Si la venta falló en Cátedra, retornar sin guardar localmente
        if (venta.getResultado() == null || !venta.getResultado()) {
            return venta;
        }

        // Intentar guardar venta localmente
        try {
            venta.setFechaVenta(Instant.now());
            Venta ventaGuardada = ventaRepositoryPort.guardar(venta);

            // Finalizar sesion solo si todo salió bien
            sesionRepositoryPort.eliminarPorId(sesion.getId());

            return ventaGuardada;
        } catch (Exception e) {
            // La venta YA se realizó en Cátedra, pero falló el guardado local
            venta.setDescripcion("VENTA EXITOSA en Cátedra (ID: " + venta.getVentaIdCatedra() +
                    "). Error al guardar localmente: " + e.getMessage() +
                    ". Los asientos fueron vendidos correctamente.");

            // Limpiar la sesión de todas formas
            sesionRepositoryPort.eliminarPorId(sesion.getId());

            return venta;
        }
    }

    @Override
    public void cancelarProceso(Long usuarioId) {
        Sesion sesion = obtenerSesionActiva(usuarioId);
        sesionRepositoryPort.eliminarPorId(sesion.getId());
    }

    private Sesion obtenerSesionActiva(Long usuarioId) {
        return sesionRepositoryPort.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("No hay sesion activa para el usuario"));
    }
}