package com.abel.eventos.infrastructure.adapter.out.persistence.repository;

import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventoJpaRepository extends JpaRepository<EventoEntity, Long> {

    List<EventoEntity> findByFechaAfter(Instant fecha);

    List<EventoEntity> findByEventoTipoId(Long eventoTipoId);
}