package com.abel.eventos.application.service;

import com.abel.eventos.application.port.in.GestionEventosUseCase;
import com.abel.eventos.application.port.out.CatedraServicePort;
import com.abel.eventos.application.port.out.EventoRepositoryPort;
import com.abel.eventos.application.port.out.ProxyServicePort;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.AsientoEstado;
import com.abel.eventos.domain.model.Evento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionEventosService implements GestionEventosUseCase {

    private final EventoRepositoryPort eventoRepositoryPort;
    private final CatedraServicePort catedraServicePort;
    private final ProxyServicePort proxyServicePort;

    @Override
    public List<Evento> listarEventosResumidos() {
        // Obtener eventos directamente desde la API de Catedra
        return catedraServicePort.obtenerEventosResumidos();
    }

    @Override
    public Evento obtenerEventoDetalle(Long eventoId) {
        // Obtener detalle desde la API de Catedra
        Evento evento = catedraServicePort.obtenerEventoPorId(eventoId);

        if (evento == null) {
            throw new RuntimeException("Evento no encontrado: " + eventoId);
        }

        return evento;
    }

    @Override
    public List<Asiento> obtenerAsientosEvento(Long eventoId) {
        Evento evento = obtenerEventoDetalle(eventoId);

        // Obtener asientos ocupados/bloqueados desde el Proxy (Redis de catedra)
        List<Asiento> asientosOcupados = proxyServicePort.obtenerAsientosOcupados(eventoId);

        // Generar mapa completo de asientos
        List<Asiento> todosLosAsientos = new ArrayList<>();

        for (int fila = 1; fila <= evento.getFilaAsientos(); fila++) {
            for (int columna = 1; columna <= evento.getColumnaAsientos(); columna++) {
                final int filaActual = fila;
                final int columnaActual = columna;

                // Buscar si este asiento esta ocupado/bloqueado
                Asiento asientoExistente = asientosOcupados.stream()
                        .filter(a -> a.getFila().equals(filaActual) && a.getColumna().equals(columnaActual))
                        .findFirst()
                        .orElse(null);

                if (asientoExistente != null) {
                    todosLosAsientos.add(asientoExistente);
                } else {
                    // Asiento libre
                    Asiento asientoLibre = new Asiento();
                    asientoLibre.setFila(fila);
                    asientoLibre.setColumna(columna);
                    asientoLibre.setEstado(AsientoEstado.LIBRE);
                    todosLosAsientos.add(asientoLibre);
                }
            }
        }

        return todosLosAsientos;
    }

    @Override
    public void sincronizarEventos() {
        // Obtener todos los eventos desde Catedra
        List<Evento> eventosCatedra = catedraServicePort.obtenerEventosCompletos();

        // Guardar/actualizar en la BD local (para cache/respaldo)
        for (Evento evento : eventosCatedra) {
            eventoRepositoryPort.guardar(evento);
        }
    }
}