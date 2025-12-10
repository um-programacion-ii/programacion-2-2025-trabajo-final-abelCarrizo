package com.abel.eventos.infrastructure.adapter.in.web;

import com.abel.eventos.application.port.in.GestionEventosUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final GestionEventosUseCase gestionEventosUseCase;

    /**
     * Endpoint para recibir notificaciones del Proxy.
     * Cuando hay cambios en eventos (via Kafka), el Proxy llama aquí.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> sincronizar(@RequestBody Map<String, Object> notificacion) {

        log.info("=========================================================");
        log.info("* NOTIFICACIÓN RECIBIDA DEL PROXY *                      ");
        log.info("=========================================================");
        log.info("Tipo: {}", notificacion.get("tipo"));
        log.info("Mensaje: {}", notificacion.get("mensaje"));
        log.info("Timestamp: {}", notificacion.get("timestamp"));

        try {
            // Sincronizar eventos desde la API de Cátedra
            gestionEventosUseCase.sincronizarEventos();

            log.info("✅ Sincronización completada exitosamente");

            return ResponseEntity.ok(Map.of(
                    "status", "sincronizado",
                    "mensaje", "Eventos actualizados correctamente"
            ));

        } catch (Exception e) {
            log.error("❌ Error durante la sincronización: {}", e.getMessage());

            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "mensaje", "Error al sincronizar: " + e.getMessage()
            ));
        }
    }
}