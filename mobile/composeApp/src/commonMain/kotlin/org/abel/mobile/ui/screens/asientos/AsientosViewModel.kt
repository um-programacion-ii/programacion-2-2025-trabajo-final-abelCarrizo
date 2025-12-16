package org.abel.mobile.ui.screens.asientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.abel.mobile.data.api.EventosApiClient
import org.abel.mobile.data.model.Asiento
import org.abel.mobile.data.model.EventoDetalle
import org.abel.mobile.util.SessionManager

/**
 * Estados posibles del asiento en la UI.
 */
enum class EstadoAsientoUi {
    LIBRE,          // Disponible para seleccionar
    VENDIDO,        // Ya vendido
    BLOQUEADO,      // Bloqueado por otro usuario
    SELECCIONADO    // Seleccionado por el usuario actual
}

/**
 * Modelo de asiento para la UI.
 */
data class AsientoUi(
    val fila: Int,
    val columna: Int,
    val estado: EstadoAsientoUi
)

/**
 * Estados de la pantalla.
 */
sealed class AsientosUiState {
    object Loading : AsientosUiState()
    data class Success(
        val evento: EventoDetalle,
        val asientos: List<AsientoUi>,
        val seleccionados: List<AsientoUi>
    ) : AsientosUiState()
    data class Error(val mensaje: String) : AsientosUiState()
}

/**
 * ViewModel para la pantalla de selección de asientos.
 */
class AsientosViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    private val _uiState = MutableStateFlow<AsientosUiState>(AsientosUiState.Loading)
    val uiState: StateFlow<AsientosUiState> = _uiState.asStateFlow()

    // Asientos seleccionados por el usuario (máximo 4)
    private val asientosSeleccionados = mutableListOf<AsientoUi>()

    private var eventoActual: EventoDetalle? = null

    init {
        SessionManager.token?.let { apiClient.setToken(it) }
    }

    /**
     * Carga el evento y sus asientos.
     */
    fun cargarAsientos(eventoId: Long) {
        _uiState.value = AsientosUiState.Loading
        asientosSeleccionados.clear()

        viewModelScope.launch {
            try {
                // Cargar evento para saber las dimensiones
                val evento = apiClient.obtenerEventoDetalle(eventoId)
                eventoActual = evento

                // Cargar estado de asientos desde el Backend
                val asientosOcupados = apiClient.obtenerAsientosEvento(eventoId)

                // Iniciar sesión de compra
                apiClient.iniciarSesion(eventoId)

                // Generar grilla completa de asientos
                val asientosUi = generarGrillaAsientos(evento, asientosOcupados)

                _uiState.value = AsientosUiState.Success(
                    evento = evento,
                    asientos = asientosUi,
                    seleccionados = asientosSeleccionados.toList()
                )

            } catch (e: Exception) {
                _uiState.value = AsientosUiState.Error(
                    e.message ?: "Error al cargar asientos"
                )
            }
        }
    }

    /**
     * Genera la grilla completa de asientos combinando
     * las dimensiones del evento con el estado de ocupación.
     */
    private fun generarGrillaAsientos(
        evento: EventoDetalle,
        asientosOcupados: List<Asiento>
    ): List<AsientoUi> {
        val filas = evento.filaAsientos ?: 0
        val columnas = evento.columnaAsientos ?: 0
        val asientos = mutableListOf<AsientoUi>()

        for (fila in 1..filas) {
            for (columna in 1..columnas) {
                // Buscar si este asiento está ocupado
                val ocupado = asientosOcupados.find {
                    it.fila == fila && it.columna == columna
                }

                val estado = when (ocupado?.estado?.uppercase()) {
                    "VENDIDO" -> EstadoAsientoUi.VENDIDO
                    "BLOQUEADO" -> EstadoAsientoUi.BLOQUEADO
                    else -> EstadoAsientoUi.LIBRE
                }

                asientos.add(AsientoUi(fila, columna, estado))
            }
        }

        return asientos
    }

    /**
     * Maneja el click en un asiento.
     */
    fun onAsientoClick(asiento: AsientoUi) {
        // Solo se pueden seleccionar asientos libres
        if (asiento.estado != EstadoAsientoUi.LIBRE &&
            asiento.estado != EstadoAsientoUi.SELECCIONADO) {
            return
        }

        val yaSeleccionado = asientosSeleccionados.any {
            it.fila == asiento.fila && it.columna == asiento.columna
        }

        if (yaSeleccionado) {
            // Deseleccionar
            asientosSeleccionados.removeAll {
                it.fila == asiento.fila && it.columna == asiento.columna
            }
        } else {
            // Seleccionar (máximo 4)
            if (asientosSeleccionados.size < 4) {
                asientosSeleccionados.add(
                    asiento.copy(estado = EstadoAsientoUi.SELECCIONADO)
                )
            }
        }

        // Actualizar UI
        actualizarEstadoAsientos()
    }

    /**
     * Actualiza el estado de la UI con los asientos seleccionados.
     */
    private fun actualizarEstadoAsientos() {
        val currentState = _uiState.value
        if (currentState is AsientosUiState.Success) {
            val asientosActualizados = currentState.asientos.map { asiento ->
                val estaSeleccionado = asientosSeleccionados.any {
                    it.fila == asiento.fila && it.columna == asiento.columna
                }

                if (estaSeleccionado) {
                    asiento.copy(estado = EstadoAsientoUi.SELECCIONADO)
                } else if (asiento.estado == EstadoAsientoUi.SELECCIONADO) {
                    asiento.copy(estado = EstadoAsientoUi.LIBRE)
                } else {
                    asiento
                }
            }

            _uiState.value = currentState.copy(
                asientos = asientosActualizados,
                seleccionados = asientosSeleccionados.toList()
            )
        }
    }

    /**
     * Confirma la selección y bloquea los asientos.
     */
    fun confirmarSeleccion(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (asientosSeleccionados.isEmpty()) {
            onError("Debes seleccionar al menos un asiento")
            return
        }

        viewModelScope.launch {
            try {
                val evento = eventoActual ?: throw Exception("Evento no cargado")

                // Convertir a modelo de API
                val asientosApi = asientosSeleccionados.map {
                    Asiento(fila = it.fila, columna = it.columna)
                }

                // Seleccionar asientos en el Backend
                val resultadoSeleccion = apiClient.seleccionarAsientos(
                    eventoId = evento.id,
                    asientos = asientosApi
                )

                if (!resultadoSeleccion.exito) {
                    onError(resultadoSeleccion.mensaje)
                    return@launch
                }

                // Bloquear asientos
                val resultadoBloqueo = apiClient.bloquearAsientos()

                if (resultadoBloqueo.exito) {
                    onSuccess()
                } else {
                    onError(resultadoBloqueo.mensaje)
                }

            } catch (e: Exception) {
                onError(e.message ?: "Error al confirmar selección")
            }
        }
    }
}
