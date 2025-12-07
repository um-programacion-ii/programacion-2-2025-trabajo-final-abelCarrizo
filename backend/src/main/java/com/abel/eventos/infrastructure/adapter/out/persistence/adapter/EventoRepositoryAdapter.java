package com.abel.eventos.infrastructure.adapter.out.persistence.adapter;

import com.abel.eventos.application.port.out.EventoRepositoryPort;
import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.domain.model.EventoTipo;
import com.abel.eventos.domain.model.Integrante;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoTipoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.IntegranteEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.repository.EventoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventoRepositoryAdapter implements EventoRepositoryPort {

    private final EventoJpaRepository eventoJpaRepository;

    @Override
    public Evento guardar(Evento evento) {
        EventoEntity entity = toEntity(evento);
        EventoEntity savedEntity = eventoJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Evento> buscarPorId(Long id) {
        return eventoJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Evento> buscarTodos() {
        return eventoJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void eliminarPorId(Long id) {
        eventoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existePorId(Long id) {
        return eventoJpaRepository.existsById(id);
    }

    // === Métodos de conversión ===

    private EventoEntity toEntity(Evento evento) {
        EventoEntity entity = new EventoEntity();
        entity.setId(evento.getId());
        entity.setTitulo(evento.getTitulo());
        entity.setResumen(evento.getResumen());
        entity.setDescripcion(evento.getDescripcion());
        entity.setFecha(evento.getFecha());
        entity.setDireccion(evento.getDireccion());
        entity.setImagen(evento.getImagen());
        entity.setFilaAsientos(evento.getFilaAsientos());
        entity.setColumnaAsientos(evento.getColumnaAsientos());
        entity.setPrecioEntrada(evento.getPrecioEntrada());

        if (evento.getEventoTipo() != null) {
            entity.setEventoTipo(toEventoTipoEntity(evento.getEventoTipo()));
        }

        if (evento.getIntegrantes() != null) {
            entity.setIntegrantes(evento.getIntegrantes().stream()
                    .map(this::toIntegranteEntity)
                    .toList());
        }

        return entity;
    }

    private Evento toDomain(EventoEntity entity) {
        Evento evento = new Evento();
        evento.setId(entity.getId());
        evento.setTitulo(entity.getTitulo());
        evento.setResumen(entity.getResumen());
        evento.setDescripcion(entity.getDescripcion());
        evento.setFecha(entity.getFecha());
        evento.setDireccion(entity.getDireccion());
        evento.setImagen(entity.getImagen());
        evento.setFilaAsientos(entity.getFilaAsientos());
        evento.setColumnaAsientos(entity.getColumnaAsientos());
        evento.setPrecioEntrada(entity.getPrecioEntrada());

        if (entity.getEventoTipo() != null) {
            evento.setEventoTipo(toEventoTipoDomain(entity.getEventoTipo()));
        }

        if (entity.getIntegrantes() != null) {
            evento.setIntegrantes(entity.getIntegrantes().stream()
                    .map(this::toIntegranteDomain)
                    .toList());
        }

        return evento;
    }

    private EventoTipoEntity toEventoTipoEntity(EventoTipo tipo) {
        EventoTipoEntity entity = new EventoTipoEntity();
        entity.setId(tipo.getId());
        entity.setNombre(tipo.getNombre());
        entity.setDescripcion(tipo.getDescripcion());
        return entity;
    }

    private EventoTipo toEventoTipoDomain(EventoTipoEntity entity) {
        return new EventoTipo(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion()
        );
    }

    private IntegranteEntity toIntegranteEntity(Integrante integrante) {
        IntegranteEntity entity = new IntegranteEntity();
        entity.setId(integrante.getId());
        entity.setNombre(integrante.getNombre());
        entity.setApellido(integrante.getApellido());
        entity.setIdentificacion(integrante.getIdentificacion());
        return entity;
    }

    private Integrante toIntegranteDomain(IntegranteEntity entity) {
        return new Integrante(
                entity.getId(),
                entity.getNombre(),
                entity.getApellido(),
                entity.getIdentificacion()
        );
    }
}