package com.abel.eventos.infrastructure.adapter.out.persistence.repository;

import com.abel.eventos.infrastructure.adapter.out.persistence.entity.SesionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionJpaRepository extends JpaRepository<SesionEntity, Long> {

    Optional<SesionEntity> findByUsuarioId(Long usuarioId);

    List<SesionEntity> findByUltimaActividadBefore(Instant fecha);

    void deleteByUsuarioId(Long usuarioId);
}