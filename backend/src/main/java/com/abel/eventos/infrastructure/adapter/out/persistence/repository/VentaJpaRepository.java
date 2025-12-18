package com.abel.eventos.infrastructure.adapter.out.persistence.repository;

import com.abel.eventos.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaJpaRepository extends JpaRepository<VentaEntity, Long> {

    List<VentaEntity> findByEventoId(Long eventoId);

    List<VentaEntity> findByUsuarioId(Long usuarioId);

    List<VentaEntity> findByResultado(Boolean resultado);
}