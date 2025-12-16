package org.abel.mobile.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.abel.mobile.data.model.*

class EventosApiClient {

    // URL base del Backend - CAMBIAR según tu entorno
    private val baseUrl = "http://10.0.2.2:8080"  // Para emulador Android
    // private val baseUrl = "http://192.168.x.x:8080"  // Para dispositivo físico

    // Token JWT que se obtiene después del login
    private var authToken: String? = null

    // Cliente HTTP configurado
    private val client = HttpClient {

        // Plugin para convertir JSON automáticamente
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // Ignora campos del JSON que no están en el DTO
                isLenient = true          // Más flexible al parsear
                prettyPrint = true        // Para debug
            })
        }

        // Plugin para ver logs de las peticiones (útil para debug)
        install(Logging) {
            level = LogLevel.BODY
        }

        // Configuración por defecto para todas las peticiones
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    // Establecer el token después del login
    fun setToken(token: String) {
        authToken = token
    }

    // Limpiar el token al cerrar sesión
    fun clearToken() {
        authToken = null
    }

    // ==================== AUTENTICACIÓN ====================

    suspend fun login(username: String, password: String): LoginResponse {
        return client.post("$baseUrl/api/auth/login") {
            setBody(LoginRequest(username, password))
        }.body()
    }

    suspend fun registro(
        username: String,
        password: String,
        nombre: String,
        apellido: String,
        email: String
    ): LoginResponse {
        return client.post("$baseUrl/api/auth/registro") {
            setBody(mapOf(
                "username" to username,
                "password" to password,
                "nombre" to nombre,
                "apellido" to apellido,
                "email" to email
            ))
        }.body()
    }

    // ==================== EVENTOS ====================

    suspend fun obtenerEventos(): List<EventoResumen> {
        return client.get("$baseUrl/api/eventos") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    suspend fun obtenerEventoDetalle(eventoId: Long): EventoDetalle {
        return client.get("$baseUrl/api/eventos/$eventoId") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    suspend fun obtenerAsientosEvento(eventoId: Long): List<Asiento> {
        return client.get("$baseUrl/api/eventos/$eventoId/asientos") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    // ==================== SESIÓN ====================

    suspend fun obtenerSesionActual(): SesionResponse {
        return client.get("$baseUrl/api/sesion") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    suspend fun iniciarSesion(eventoId: Long): SesionResponse {
        return client.post("$baseUrl/api/sesion/iniciar") {
            authToken?.let { bearerAuth(it) }
            setBody(IniciarSesionRequest(eventoId))
        }.body()
    }

    suspend fun finalizarSesion(): SesionResponse {
        return client.post("$baseUrl/api/sesion/finalizar") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    // ==================== VENTAS ====================

    suspend fun seleccionarAsientos(eventoId: Long, asientos: List<Asiento>): MensajeResponse {
        return client.post("$baseUrl/api/ventas/seleccionar") {
            authToken?.let { bearerAuth(it) }
            setBody(SeleccionarAsientosRequest(eventoId, asientos))
        }.body()
    }

    suspend fun bloquearAsientos(): MensajeResponse {
        return client.post("$baseUrl/api/ventas/bloquear") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    suspend fun asignarPersonas(asientos: List<Asiento>): MensajeResponse {
        return client.post("$baseUrl/api/ventas/asignar-personas") {
            authToken?.let { bearerAuth(it) }
            setBody(AsignarPersonasRequest(asientos))
        }.body()
    }

    suspend fun confirmarVenta(): VentaResponse {
        return client.post("$baseUrl/api/ventas/confirmar") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }

    suspend fun cancelarProceso(): MensajeResponse {
        return client.post("$baseUrl/api/ventas/cancelar") {
            authToken?.let { bearerAuth(it) }
        }.body()
    }
}