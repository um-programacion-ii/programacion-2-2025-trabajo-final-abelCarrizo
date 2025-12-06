package com.abel.eventos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String email;
}