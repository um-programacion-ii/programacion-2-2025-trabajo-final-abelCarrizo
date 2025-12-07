package com.abel.eventos.infrastructure.adapter.out.persistence.adapter;

import com.abel.eventos.application.port.out.SesionRepositoryPort;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.Sesion;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.SesionAsientoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.SesionEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.repository.SesionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SesionRepositoryAdapter implements SesionRepositoryPort {

    private final SesionJpaRepository sesionJpaRepository;

    private static final int MINUTOS_EXPIRACION = 30;

    @Override
    public Sesion guardar(Sesion sesion) {
        SesionEntity entity = toEntity(sesion);
        SesionEntity savedEntity = sesionJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Sesion> buscarPorId(Long id) {
        return sesionJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Sesion> buscarPorUsuarioId(Long usuarioId) {
        return sesionJpaRepository.findByUsuarioId(usuarioId)
                .map(this::toDomain);
    }

    @Override
    public void eliminarPorId(Long id) {
        sesionJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void eliminarSesionesExpiradas() {
        Instant fechaLimite = Instant.now().minus(MINUTOS_EXPIRACION, ChronoUnit.MINUTES);
        List<SesionEntity> sesionesExpiradas = sesionJpaRepository.findByUltimaActividadBefore(fechaLimite);
        sesionJpaRepository.deleteAll(sesionesExpiradas);
    }

    // === Métodos de conversión ===

    private SesionEntity toEntity(Sesion sesion) {
        SesionEntity entity = new SesionEntity();
        entity.setId(sesion.getId());
        entity.setEstado(sesion.getEstado());
        entity.setCreadoEn(sesion.getCreadoEn());
        entity.setUltimaActividad(sesion.getUltimaActividad());

        // Referencia al usuario
        if (sesion.getUsuarioId() != null) {
            UsuarioEntity usuarioRef = new UsuarioEntity();
            usuarioRef.setId(sesion.getUsuarioId());
            entity.setUsuario(usuarioRef);
        }

        // Referencia al evento (puede ser null al inicio)
        if (sesion.getEventoId() != null) {
            EventoEntity eventoRef = new EventoEntity();
            eventoRef.setId(sesion.getEventoId());
            entity.setEvento(eventoRef);
        }

        // Convertir asientos seleccionados
        if (sesion.getAsientosSeleccionados() != null) {
            List<SesionAsientoEntity> asientosEntity = new ArrayList<>();
            for (Asiento asiento : sesion.getAsientosSeleccionados()) {
                SesionAsientoEntity asientoEntity = new SesionAsientoEntity();
                asientoEntity.setFila(asiento.getFila());
                asientoEntity.setColumna(asiento.getColumna());
                asientoEntity.setPersona(asiento.getPersona());
                asientoEntity.setSesion(entity);
                asientosEntity.add(asientoEntity);
            }
            entity.setAsientosSeleccionados(asientosEntity);
        }

        return entity;
    }

    private Sesion toDomain(SesionEntity entity) {
        Sesion sesion = new Sesion();
        sesion.setId(entity.getId());
        sesion.setEstado(entity.getEstado());
        sesion.setCreadoEn(entity.getCreadoEn());
        sesion.setUltimaActividad(entity.getUltimaActividad());

        if (entity.getUsuario() != null) {
            sesion.setUsuarioId(entity.getUsuario().getId());
        }

        if (entity.getEvento() != null) {
            sesion.setEventoId(entity.getEvento().getId());
        }

        // Convertir asientos seleccionados
        if (entity.getAsientosSeleccionados() != null) {
            List<Asiento> asientos = entity.getAsientosSeleccionados().stream()
                    .map(this::toAsientoDomain)
                    .toList();
            sesion.setAsientosSeleccionados(asientos);
        }

        return sesion;
    }

    private Asiento toAsientoDomain(SesionAsientoEntity entity) {
        Asiento asiento = new Asiento();
        asiento.setFila(entity.getFila());
        asiento.setColumna(entity.getColumna());
        asiento.setPersona(entity.getPersona());
        return asiento;
    }
}
