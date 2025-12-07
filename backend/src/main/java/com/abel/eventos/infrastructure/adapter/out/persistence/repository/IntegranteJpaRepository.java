package com.abel.eventos.infrastructure.adapter.out.persistence.repository;

import com.abel.eventos.infrastructure.adapter.out.persistence.entity.IntegranteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntegranteJpaRepository extends JpaRepository<IntegranteEntity, Long> {

    Optional<IntegranteEntity> findByNombreAndApellido(String nombre, String apellido);
}