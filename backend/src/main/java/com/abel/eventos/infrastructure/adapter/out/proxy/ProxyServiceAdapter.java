package com.abel.eventos.infrastructure.adapter.out.proxy;

import com.abel.eventos.application.port.out.ProxyServicePort;
import com.abel.eventos.domain.model.Asiento;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProxyServiceAdapter implements ProxyServicePort {

    @Override
    public List<Asiento> obtenerAsientosOcupados(Long eventoId) {
        // TODO: Implementar conexion real con el servicio Proxy en el milestone 3
        // Por ahora retorna lista vacia (todos los asientos disponibles)
        return new ArrayList<>();
    }

    @Override
    public boolean verificarDisponibilidad(Long eventoId, List<Asiento> asientos) {
        // TODO: Implementar conexion real con el servicio Proxy en el milestone 3
        // Por ahora siempre retorna true (disponible)
        return true;
    }
}