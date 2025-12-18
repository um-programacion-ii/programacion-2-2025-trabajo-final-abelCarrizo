package com.abel.eventos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evento_tipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoTipoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Los IDs se generan localmente (Cátedra NO envía IDs para EventoTipo)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;
}