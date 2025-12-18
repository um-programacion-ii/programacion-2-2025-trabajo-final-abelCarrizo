package com.abel.eventos.infrastructure.adapter.out.persistence.adapter;

import com.abel.eventos.application.port.out.VentaRepositoryPort;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.AsientoEstado;
import com.abel.eventos.domain.model.Venta;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.VentaAsientoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.repository.VentaJpaRepository;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final VentaJpaRepository ventaJpaRepository;

    @Override
    public Venta guardar(Venta venta) {
        VentaEntity entity = toEntity(venta);
        VentaEntity savedEntity = ventaJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Venta> buscarPorId(Long id) {
        return ventaJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Venta> buscarPorEventoId(Long eventoId) {
        return ventaJpaRepository.findByEventoId(eventoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Venta> buscarPorUsuarioId(Long usuarioId) {
        return ventaJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Venta> buscarTodas() {
        return ventaJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    // === Métodos de conversión ===

    private VentaEntity toEntity(Venta venta) {
        VentaEntity entity = new VentaEntity();

        if (venta.getId() != null) {
            entity.setId(venta.getId());
        }
        entity.setVentaIdCatedra(venta.getVentaIdCatedra());
        entity.setEventoId(venta.getEventoId());
        entity.setFechaVenta(venta.getFechaVenta());
        entity.setPrecioVenta(venta.getPrecioVenta());
        entity.setResultado(venta.getResultado());
        entity.setDescripcion(venta.getDescripcion());

        // Mapear usuario
        if (venta.getUsuarioId() != null) {
            UsuarioEntity usuarioRef = new UsuarioEntity();
            usuarioRef.setId(venta.getUsuarioId());
            entity.setUsuario(usuarioRef);
        }

        // Convertir asientos
        if (venta.getAsientos() != null) {
            List<VentaAsientoEntity> asientosEntity = new ArrayList<>();
            for (Asiento asiento : venta.getAsientos()) {
                VentaAsientoEntity asientoEntity = new VentaAsientoEntity();
                asientoEntity.setFila(asiento.getFila());
                asientoEntity.setColumna(asiento.getColumna());
                asientoEntity.setPersona(asiento.getPersona());
                asientoEntity.setVenta(entity);
                asientosEntity.add(asientoEntity);
            }
            entity.setAsientos(asientosEntity);
        }

        return entity;
    }

    private Venta toDomain(VentaEntity entity) {
        Venta venta = new Venta();
        venta.setId(entity.getId());
        venta.setVentaIdCatedra(entity.getVentaIdCatedra());
        venta.setEventoId(entity.getEventoId());
        venta.setFechaVenta(entity.getFechaVenta());
        venta.setPrecioVenta(entity.getPrecioVenta());
        venta.setResultado(entity.getResultado());
        venta.setDescripcion(entity.getDescripcion());

        // Mapear usuario
        if (entity.getUsuario() != null) {
            venta.setUsuarioId(entity.getUsuario().getId());
        }

        // Convertir asientos
        if (entity.getAsientos() != null) {
            List<Asiento> asientos = entity.getAsientos().stream()
                    .map(this::toAsientoDomain)
                    .toList();
            venta.setAsientos(asientos);
        }

        return venta;
    }

    private Asiento toAsientoDomain(VentaAsientoEntity entity) {
        Asiento asiento = new Asiento();
        asiento.setFila(entity.getFila());
        asiento.setColumna(entity.getColumna());
        asiento.setEstado(AsientoEstado.VENDIDO);
        asiento.setPersona(entity.getPersona());
        return asiento;
    }
}