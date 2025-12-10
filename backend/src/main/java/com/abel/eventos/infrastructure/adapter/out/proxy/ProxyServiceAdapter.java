package com.abel.eventos.infrastructure.adapter.out.proxy;

import com.abel.eventos.application.port.out.ProxyServicePort;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.AsientoEstado;
import com.abel.eventos.infrastructure.adapter.out.proxy.dto.ProxyAsientoDTO;
import com.abel.eventos.infrastructure.adapter.out.proxy.dto.ProxyEventoAsientosDTO;
import com.abel.eventos.infrastructure.adapter.out.proxy.dto.ProxyVerificacionRequest;
import com.abel.eventos.infrastructure.adapter.out.proxy.dto.ProxyVerificacionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProxyServiceAdapter implements ProxyServicePort {

    private final RestTemplate restTemplate;

    @Value("${proxy.url}")
    private String proxyUrl;

    @Override
    public List<Asiento> obtenerAsientosOcupados(Long eventoId) {
        String url = proxyUrl + "/api/proxy/eventos/" + eventoId + "/asientos";

        log.info("Consultando asientos ocupados al Proxy: {}", url);

        try {
            ResponseEntity<ProxyEventoAsientosDTO> response = restTemplate.getForEntity(
                    url,
                    ProxyEventoAsientosDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ProxyEventoAsientosDTO dto = response.getBody();
                log.info("Proxy respondió con {} asientos ocupados", dto.getAsientos().size());

                return convertirADominio(dto.getAsientos());
            }

            log.warn("Proxy respondió sin datos");
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Error consultando al Proxy: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean verificarDisponibilidad(Long eventoId, List<Asiento> asientos) {
        String url = proxyUrl + "/api/proxy/eventos/" + eventoId + "/verificar";

        log.info("Verificando disponibilidad de {} asientos en Proxy", asientos.size());

        try {
            // Convertir asientos de dominio a DTOs del Proxy
            List<ProxyAsientoDTO> asientosDTO = asientos.stream()
                    .map(a -> new ProxyAsientoDTO(a.getFila(), a.getColumna(), null, null))
                    .collect(Collectors.toList());

            ProxyVerificacionRequest request = new ProxyVerificacionRequest(asientosDTO);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ProxyVerificacionRequest> httpEntity = new HttpEntity<>(request, headers);

            ResponseEntity<ProxyVerificacionResponse> response = restTemplate.postForEntity(
                    url,
                    httpEntity,
                    ProxyVerificacionResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                boolean disponible = response.getBody().getDisponible();
                log.info("Verificación completada. Disponible: {}", disponible);
                return disponible;
            }

            log.warn("Proxy respondió sin datos de verificación");
            return false;

        } catch (Exception e) {
            log.error("Error verificando disponibilidad en Proxy: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Convierte DTOs del Proxy a objetos de dominio.
     */
    private List<Asiento> convertirADominio(List<ProxyAsientoDTO> dtos) {
        return dtos.stream()
                .map(dto -> {
                    Asiento asiento = new Asiento();
                    asiento.setFila(dto.getFila());
                    asiento.setColumna(dto.getColumna());
                    asiento.setEstado(convertirEstado(dto.getEstado()));
                    return asiento;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convierte el estado String del Proxy al enum del dominio.
     */
    private AsientoEstado convertirEstado(String estado) {
        if (estado == null) {
            return AsientoEstado.LIBRE;
        }
        return switch (estado.toUpperCase()) {
            case "BLOQUEADO" -> AsientoEstado.BLOQUEADO;
            case "VENDIDO" -> AsientoEstado.VENDIDO;
            default -> AsientoEstado.LIBRE;
        };
    }
}