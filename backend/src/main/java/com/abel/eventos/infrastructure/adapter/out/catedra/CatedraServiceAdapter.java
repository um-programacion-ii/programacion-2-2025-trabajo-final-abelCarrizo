package com.abel.eventos.infrastructure.adapter.out.catedra;

import com.abel.eventos.application.port.out.CatedraServicePort;
import com.abel.eventos.domain.model.Asiento;
import com.abel.eventos.domain.model.Evento;
import com.abel.eventos.domain.model.EventoTipo;
import com.abel.eventos.domain.model.Integrante;
import com.abel.eventos.domain.model.Venta;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraAsientoDTO;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraBloqueoRequestDTO;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraBloqueoResponseDTO;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraEventoCompletoDTO;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraEventoResumidoDTO;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraVentaRequestDTO;
import com.abel.eventos.infrastructure.adapter.out.catedra.dto.CatedraVentaResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatedraServiceAdapter implements CatedraServicePort {

    private final RestTemplate restTemplate;

    @Value("${catedra.api.base-url}")
    private String baseUrl;

    @Override
    public List<Evento> obtenerEventosResumidos() {
        try {
            String url = baseUrl + "/api/endpoints/v1/eventos-resumidos";

            ResponseEntity<List<CatedraEventoResumidoDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (response.getBody() == null) {
                return new ArrayList<>();
            }

            return response.getBody().stream()
                    .map(this::toEventoResumido)
                    .toList();

        } catch (RestClientException e) {
            log.error("Error al obtener eventos resumidos de Catedra: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Evento> obtenerEventosCompletos() {
        try {
            String url = baseUrl + "/api/endpoints/v1/eventos";

            ResponseEntity<List<CatedraEventoCompletoDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (response.getBody() == null) {
                return new ArrayList<>();
            }

            return response.getBody().stream()
                    .map(this::toEventoCompleto)
                    .toList();

        } catch (RestClientException e) {
            log.error("Error al obtener eventos completos de Catedra: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Evento obtenerEventoPorId(Long id) {
        try {
            String url = baseUrl + "/api/endpoints/v1/evento/" + id;

            CatedraEventoCompletoDTO response = restTemplate.getForObject(
                    url,
                    CatedraEventoCompletoDTO.class
            );

            if (response == null) {
                return null;
            }

            return toEventoCompleto(response);

        } catch (RestClientException e) {
            log.error("Error al obtener evento {} de Catedra: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean bloquearAsientos(Long eventoId, List<Asiento> asientos) {
        try {
            String url = baseUrl + "/api/endpoints/v1/bloquear-asientos";

            CatedraBloqueoRequestDTO request = new CatedraBloqueoRequestDTO();
            request.setEventoId(eventoId);
            request.setAsientos(asientos.stream()
                    .map(this::toAsientoDTO)
                    .toList());

            CatedraBloqueoResponseDTO response = restTemplate.postForObject(
                    url,
                    request,
                    CatedraBloqueoResponseDTO.class
            );

            if (response == null) {
                return false;
            }

            if (response.getResultado()) {
                log.info("Asientos bloqueados exitosamente para evento {}", eventoId);
            } else {
                log.warn("No se pudieron bloquear asientos: {}", response.getDescripcion());
            }

            return response.getResultado();

        } catch (RestClientException e) {
            log.error("Error al bloquear asientos en Catedra: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Venta realizarVenta(Long eventoId, BigDecimal precioVenta, List<Asiento> asientos) {
        try {
            String url = baseUrl + "/api/endpoints/v1/realizar-venta";

            CatedraVentaRequestDTO request = new CatedraVentaRequestDTO();
            request.setEventoId(eventoId);
            request.setFecha(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            request.setPrecioVenta(precioVenta);
            request.setAsientos(asientos.stream()
                    .map(this::toAsientoDTOConPersona)
                    .toList());

            CatedraVentaResponseDTO response = restTemplate.postForObject(
                    url,
                    request,
                    CatedraVentaResponseDTO.class
            );

            if (response == null) {
                Venta ventaFallida = new Venta();
                ventaFallida.setResultado(false);
                ventaFallida.setDescripcion("No se recibio respuesta del servidor");
                return ventaFallida;
            }

            return toVenta(response);

        } catch (RestClientException e) {
            log.error("Error al realizar venta en Catedra: {}", e.getMessage());
            Venta ventaFallida = new Venta();
            ventaFallida.setResultado(false);
            ventaFallida.setDescripcion("Error de conexion: " + e.getMessage());
            return ventaFallida;
        }
    }

    @Override
    public List<Venta> listarVentas() {
        try {
            String url = baseUrl + "/api/endpoints/v1/listar-ventas";

            ResponseEntity<List<CatedraVentaResponseDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (response.getBody() == null) {
                return new ArrayList<>();
            }

            return response.getBody().stream()
                    .map(this::toVenta)
                    .toList();

        } catch (RestClientException e) {
            log.error("Error al listar ventas de Catedra: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Venta obtenerVentaPorId(Long id) {
        try {
            String url = baseUrl + "/api/endpoints/v1/listar-venta/" + id;

            CatedraVentaResponseDTO response = restTemplate.getForObject(
                    url,
                    CatedraVentaResponseDTO.class
            );

            if (response == null) {
                return null;
            }

            return toVenta(response);

        } catch (RestClientException e) {
            log.error("Error al obtener venta {} de Catedra: {}", id, e.getMessage());
            return null;
        }
    }

    // === Métodos de conversión ===

    private Evento toEventoResumido(CatedraEventoResumidoDTO dto) {
        Evento evento = new Evento();
        evento.setId(dto.getId());
        evento.setTitulo(dto.getTitulo());
        evento.setResumen(dto.getResumen());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(parseFecha(dto.getFecha()));
        evento.setPrecioEntrada(dto.getPrecioEntrada());

        if (dto.getEventoTipo() != null) {
            EventoTipo tipo = new EventoTipo();
            tipo.setNombre(dto.getEventoTipo().getNombre());
            tipo.setDescripcion(dto.getEventoTipo().getDescripcion());
            evento.setEventoTipo(tipo);
        }

        return evento;
    }

    private Evento toEventoCompleto(CatedraEventoCompletoDTO dto) {
        Evento evento = new Evento();
        evento.setId(dto.getId());
        evento.setTitulo(dto.getTitulo());
        evento.setResumen(dto.getResumen());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(parseFecha(dto.getFecha()));
        evento.setDireccion(dto.getDireccion());
        evento.setImagen(dto.getImagen());
        evento.setFilaAsientos(dto.getFilaAsientos());
        evento.setColumnaAsientos(dto.getColumnAsientos());
        evento.setPrecioEntrada(dto.getPrecioEntrada());

        if (dto.getEventoTipo() != null) {
            EventoTipo tipo = new EventoTipo();
            tipo.setNombre(dto.getEventoTipo().getNombre());
            tipo.setDescripcion(dto.getEventoTipo().getDescripcion());
            evento.setEventoTipo(tipo);
        }

        if (dto.getIntegrantes() != null) {
            List<Integrante> integrantes = dto.getIntegrantes().stream()
                    .map(i -> {
                        Integrante integrante = new Integrante();
                        integrante.setNombre(i.getNombre());
                        integrante.setApellido(i.getApellido());
                        integrante.setIdentificacion(i.getIdentificacion());
                        return integrante;
                    })
                    .toList();
            evento.setIntegrantes(integrantes);
        }

        return evento;
    }

    private Venta toVenta(CatedraVentaResponseDTO dto) {
        Venta venta = new Venta();
        venta.setId(dto.getVentaId());
        venta.setEventoId(dto.getEventoId());
        venta.setFechaVenta(parseFecha(dto.getFechaVenta()));
        venta.setPrecioVenta(dto.getPrecioVenta());
        venta.setResultado(dto.getResultado());
        venta.setDescripcion(dto.getDescripcion());

        if (dto.getAsientos() != null) {
            List<Asiento> asientos = dto.getAsientos().stream()
                    .map(this::toAsiento)
                    .toList();
            venta.setAsientos(asientos);
        }

        return venta;
    }

    private Asiento toAsiento(CatedraAsientoDTO dto) {
        Asiento asiento = new Asiento();
        asiento.setFila(dto.getFila());
        asiento.setColumna(dto.getColumna());
        asiento.setPersona(dto.getPersona());
        return asiento;
    }

    private CatedraAsientoDTO toAsientoDTO(Asiento asiento) {
        CatedraAsientoDTO dto = new CatedraAsientoDTO();
        dto.setFila(asiento.getFila());
        dto.setColumna(asiento.getColumna());
        return dto;
    }

    private CatedraAsientoDTO toAsientoDTOConPersona(Asiento asiento) {
        CatedraAsientoDTO dto = new CatedraAsientoDTO();
        dto.setFila(asiento.getFila());
        dto.setColumna(asiento.getColumna());
        dto.setPersona(asiento.getPersona());
        return dto;
    }

    private Instant parseFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(fecha);
        } catch (Exception e) {
            log.warn("Error al parsear fecha: {}", fecha);
            return null;
        }
    }
}