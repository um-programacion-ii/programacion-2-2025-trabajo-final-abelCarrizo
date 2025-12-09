package com.abel.proxy.service;

import com.abel.proxy.dto.AsientoDTO;
import com.abel.proxy.dto.EventoAsientosDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Obtiene los asientos ocupados/bloqueados de un evento desde Redis.
     * Si no hay datos en Redis, retorna un DTO con lista vacía (todos libres).
     */
    public EventoAsientosDTO obtenerAsientosOcupados(Long eventoId) {
        String key = "evento_" + eventoId;

        log.debug("Consultando Redis con clave: {}", key);

        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isEmpty()) {
            log.debug("No hay datos en Redis para evento {}. Todos los asientos están libres.", eventoId);
            return new EventoAsientosDTO(eventoId);
        }

        try {
            EventoAsientosDTO resultado = objectMapper.readValue(json, EventoAsientosDTO.class);
            log.debug("Encontrados {} asientos ocupados/bloqueados para evento {}",
                    resultado.getAsientos().size(), eventoId);
            return resultado;
        } catch (JsonProcessingException e) {
            log.error("Error parseando JSON de Redis para evento {}: {}", eventoId, e.getMessage());
            return new EventoAsientosDTO(eventoId);
        }
    }

    /**
     * Verifica si una lista de asientos está disponible (no están en Redis).
     * Retorna true si TODOS los asientos están disponibles.
     */
    public boolean verificarDisponibilidad(Long eventoId, List<AsientoDTO> asientosAVerificar) {
        EventoAsientosDTO ocupados = obtenerAsientosOcupados(eventoId);

        for (AsientoDTO asientoSolicitado : asientosAVerificar) {
            for (AsientoDTO asientoOcupado : ocupados.getAsientos()) {
                if (asientoSolicitado.getFila().equals(asientoOcupado.getFila()) &&
                        asientoSolicitado.getColumna().equals(asientoOcupado.getColumna())) {

                    log.debug("Asiento [{},{}] no disponible. Estado: {}",
                            asientoSolicitado.getFila(),
                            asientoSolicitado.getColumna(),
                            asientoOcupado.getEstado());
                    return false;
                }
            }
        }

        log.debug("Todos los asientos solicitados están disponibles para evento {}", eventoId);
        return true;
    }
}