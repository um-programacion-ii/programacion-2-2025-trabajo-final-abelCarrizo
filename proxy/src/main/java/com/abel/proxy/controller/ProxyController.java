package com.abel.proxy.controller;

import com.abel.proxy.dto.EventoAsientosDTO;
import com.abel.proxy.dto.VerificacionRequest;
import com.abel.proxy.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final RedisService redisService;

    /**
     * Obtiene los asientos ocupados/bloqueados de un evento.
     * <p>
     * GET /api/proxy/eventos/{eventoId}/asientos
     */
    @GetMapping("/eventos/{eventoId}/asientos")
    public ResponseEntity<EventoAsientosDTO> obtenerAsientosOcupados(
            @PathVariable Long eventoId) {

        log.info("Solicitud de asientos ocupados para evento: {}", eventoId);

        EventoAsientosDTO resultado = redisService.obtenerAsientosOcupados(eventoId);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Verifica si una lista de asientos está disponible.
     * <p>
     * POST /api/proxy/eventos/{eventoId}/verificar
     * Body: {"asientos": [{"fila": 1, "columna": 2}, ...]}
     */
    @PostMapping("/eventos/{eventoId}/verificar")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @PathVariable Long eventoId,
            @RequestBody VerificacionRequest request) {

        log.info("Verificando disponibilidad de {} asientos para evento {}",
                request.getAsientos().size(), eventoId);

        boolean disponible = redisService.verificarDisponibilidad(eventoId, request.getAsientos());

        Map<String, Object> response = Map.of(
                "eventoId", eventoId,
                "disponible", disponible,
                "asientosConsultados", request.getAsientos().size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de health check para verificar que el proxy está activo.
     * <p>
     * GET /api/proxy/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "proxy"
        ));
    }
}