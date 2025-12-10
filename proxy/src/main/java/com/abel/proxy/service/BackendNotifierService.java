package com.abel.proxy.service;

import com.abel.proxy.dto.NotificacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackendNotifierService {

    private final RestTemplate restTemplate;

    @Value("${backend.url}")
    private String backendUrl;

    /**
     * Notifica al Backend que hubo un cambio en los eventos.
     */
    public void notificarCambio(String mensajeKafka) {
        String url = backendUrl + "/api/internal/sync";

        log.info("Notificando al Backend: {}", url);

        try {
            NotificacionDTO notificacion = new NotificacionDTO(
                    "EVENTO_ACTUALIZADO",
                    mensajeKafka
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotificacionDTO> request = new HttpEntity<>(notificacion, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Backend notificado exitosamente. Respuesta: {}", response.getBody());
            } else {
                log.warn("Backend respondió con código: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error notificando al Backend: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el consumo de Kafka
        }
    }
}