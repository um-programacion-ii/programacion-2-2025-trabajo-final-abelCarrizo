package com.abel.proxy.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private static final String TOPIC = "eventos-actualizacion";

    private final BackendNotifierService backendNotifierService;  // ← AGREGAR

    @PostConstruct
    public void init() {
        log.info("=======================================================");
        log.info("  KafkaConsumerService INICIADO                        ");
        log.info("  Escuchando topic: {}                                 ", TOPIC);
        log.info("=======================================================");
    }

    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void escucharActualizaciones(String mensaje) {
        log.info("=======================================================");
        log.info("MENSAJE RECIBIDO DE KAFKA");
        log.info("Topic: {}", TOPIC);
        log.info("Contenido: {}", mensaje);
        log.info("=======================================================");

        procesarMensaje(mensaje);
    }

    private void procesarMensaje(String mensaje) {
        try {
            // Notificar al Backend
            backendNotifierService.notificarCambio(mensaje);

        } catch (Exception e) {
            log.error("Error procesando mensaje de Kafka: {}", e.getMessage(), e);
        }
    }
}