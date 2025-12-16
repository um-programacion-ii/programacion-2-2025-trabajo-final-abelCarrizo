package com.abel.eventos.infrastructure.adapter.out.persistence.adapter;

import com.abel.eventos.application.port.out.EventoRepositoryPort;
import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.domain.model.EventoTipo;
import com.abel.eventos.domain.model.Integrante;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoTipoEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.IntegranteEntity;
import com.abel.eventos.infrastructure.adapter.out.persistence.repository.EventoJpaRepository;
import com.abel.eventos.infrastructure.adapter.out.persistence.repository.EventoTipoJpaRepository;
import com.abel.eventos.infrastructure.adapter.out.persistence.repository.IntegranteJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventoRepositoryAdapter implements EventoRepositoryPort {

    private final EventoJpaRepository eventoJpaRepository;
    private final EventoTipoJpaRepository eventoTipoJpaRepository;
    private final IntegranteJpaRepository integranteJpaRepository;

    @Override
    @Transactional
    public Evento guardar(Evento evento) {
        log.debug("Guardando evento: {} (ID: {})", evento.getTitulo(), evento.getId());

        // Buscar si ya existe
        Optional<EventoEntity> existente = eventoJpaRepository.findById(evento.getId());

        EventoEntity entity;

        if (existente.isPresent()) {
            // Actualizar entidad existente
            log.debug("Evento {} ya existe, actualizando...", evento.getId());
            entity = existente.get();
            actualizarCampos(entity, evento);
        } else {
            // Crear nueva entidad
            log.debug("Evento {} es nuevo, creando...", evento.getId());
            entity = toEntity(evento);
        }

        EventoEntity savedEntity = eventoJpaRepository.save(entity);
        log.debug("Evento guardado exitosamente: {}", savedEntity.getId());

        return toDomain(savedEntity);
    }

    /**
     * Actualiza los campos de una entidad existente con los datos del dominio.
     */
    private void actualizarCampos(EventoEntity entity, Evento evento) {
        entity.setTitulo(evento.getTitulo());
        entity.setResumen(evento.getResumen());
        entity.setDescripcion(evento.getDescripcion());
        entity.setFecha(evento.getFecha());
        entity.setDireccion(evento.getDireccion());
        entity.setImagen(evento.getImagen());
        entity.setFilaAsientos(evento.getFilaAsientos());
        entity.setColumnaAsientos(evento.getColumnaAsientos());
        entity.setPrecioEntrada(evento.getPrecioEntrada());

        // Actualizar EventoTipo
        if (evento.getEventoTipo() != null) {
            EventoTipoEntity tipoEntity = obtenerOCrearEventoTipo(evento.getEventoTipo());
            entity.setEventoTipo(tipoEntity);
        }

        // Actualizar Integrantes
        if (evento.getIntegrantes() != null) {
            List<IntegranteEntity> integrantesEntities = new ArrayList<>();
            for (Integrante integrante : evento.getIntegrantes()) {
                IntegranteEntity integranteEntity = obtenerOCrearIntegrante(integrante);
                integrantesEntities.add(integranteEntity);
            }
            entity.setIntegrantes(integrantesEntities);
        }
    }

    /**
     * Obtiene un EventoTipo existente o crea uno nuevo.
     * Busca por nombre ya que Cátedra no envía IDs para EventoTipo.
     */
    private EventoTipoEntity obtenerOCrearEventoTipo(EventoTipo tipo) {
        // Buscar por nombre (Cátedra no envía ID)
        Optional<EventoTipoEntity> existente = eventoTipoJpaRepository.findByNombre(tipo.getNombre());

        if (existente.isPresent()) {
            // Actualizar campos si es necesario
            EventoTipoEntity entity = existente.get();
            entity.setDescripcion(tipo.getDescripcion());
            return eventoTipoJpaRepository.save(entity);
        }

        // Crear nuevo (JPA generará el ID)
        EventoTipoEntity entity = new EventoTipoEntity();
        entity.setNombre(tipo.getNombre());
        entity.setDescripcion(tipo.getDescripcion());
        return eventoTipoJpaRepository.save(entity);
    }

    /**
     * Obtiene un Integrante existente o crea uno nuevo.
     * Busca por nombre y apellido ya que Cátedra no envía IDs para Integrantes.
     */
    private IntegranteEntity obtenerOCrearIntegrante(Integrante integrante) {
        // Buscar por nombre y apellido (Cátedra no envía ID)
        Optional<IntegranteEntity> existente = integranteJpaRepository
                .findByNombreAndApellido(integrante.getNombre(), integrante.getApellido());

        if (existente.isPresent()) {
            // Actualizar campos si es necesario
            IntegranteEntity entity = existente.get();
            entity.setIdentificacion(integrante.getIdentificacion());
            return integranteJpaRepository.save(entity);
        }

        // Crear nuevo (JPA generará el ID)
        IntegranteEntity entity = new IntegranteEntity();
        entity.setNombre(integrante.getNombre());
        entity.setApellido(integrante.getApellido());
        entity.setIdentificacion(integrante.getIdentificacion());
        return integranteJpaRepository.save(entity);
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
        actualizarCampos(entity, evento);  // Reutilizar el método existente
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

    private EventoTipo toEventoTipoDomain(EventoTipoEntity entity) {
        return new EventoTipo(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion()
        );
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