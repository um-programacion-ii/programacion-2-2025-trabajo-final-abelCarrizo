package com.abel.eventos.infrastructure.adapter.out.persistence.repository;

import com.abel.eventos.domain.model.AsientoEstado;
import com.abel.eventos.infrastructure.adapter.out.persistence.entity.AsientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsientoJpaRepository extends JpaRepository<AsientoEntity, Long> {

    List<AsientoEntity> findByEventoId(Long eventoId);

    List<AsientoEntity> findByEventoIdAndEstado(Long eventoId, AsientoEstado estado);

    Optional<AsientoEntity> findByEventoIdAndFilaAndColumna(Long eventoId, Integer fila, Integer columna);
}