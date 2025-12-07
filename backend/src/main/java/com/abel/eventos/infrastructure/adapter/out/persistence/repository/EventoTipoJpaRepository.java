package com.abel.eventos.infrastructure.adapter.out.persistence.repository;

import com.abel.eventos.infrastructure.adapter.out.persistence.entity.EventoTipoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventoTipoJpaRepository extends JpaRepository<EventoTipoEntity, Long> {

    Optional<EventoTipoEntity> findByNombre(String nombre);
}