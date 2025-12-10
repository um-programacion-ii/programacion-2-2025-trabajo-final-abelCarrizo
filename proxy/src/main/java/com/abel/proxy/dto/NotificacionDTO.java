package com.abel.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    private String tipo;           // Tipo de notificación (ej: "EVENTO_ACTUALIZADO")
    private String mensaje;        // Mensaje original de Kafka
    private LocalDateTime timestamp;

    // Constructor conveniente
    public NotificacionDTO(String tipo, String mensaje) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }
}