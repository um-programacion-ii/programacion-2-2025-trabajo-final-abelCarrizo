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

    @PostConstruct
    public void init() {
        log.info("=======================================================");
        log.info("  KafkaConsumerService INICIADO                        ");
        log.info("  Escuchando topic: {}                                 ", TOPIC);
        log.info("=======================================================");
    }

    /**
     * Escucha mensajes del topic "eventos-actualizacion".
     * Se ejecuta automáticamente cada vez que llega un mensaje.
     */
    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void escucharActualizaciones(String mensaje) {
        log.info("=======================================================");
        log.info("MENSAJE RECIBIDO DE KAFKA");
        log.info("Topic: {}", TOPIC);
        log.info("Contenido: {}", mensaje);
        log.info("=======================================================");

        // Por ahora solo logueamos el mensaje
        procesarMensaje(mensaje);
    }

    /**
     * Procesa el mensaje recibido.
     * Por ahora solo lo loguea, después notificará al Backend.
     */
    private void procesarMensaje(String mensaje) {
        try {
            // TODO: En Parsear el mensaje y notificar al Backend
            log.debug("Procesando mensaje: {}", mensaje);

        } catch (Exception e) {
            log.error("Error procesando mensaje de Kafka: {}", e.getMessage(), e);
        }
    }
}