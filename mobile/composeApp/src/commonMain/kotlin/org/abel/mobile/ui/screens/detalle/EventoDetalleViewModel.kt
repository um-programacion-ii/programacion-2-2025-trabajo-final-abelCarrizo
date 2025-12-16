package org.abel.mobile.ui.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.abel.mobile.data.api.EventosApiClient
import org.abel.mobile.data.model.EventoDetalle
import org.abel.mobile.util.SessionManager

/**
 * Estados posibles de la pantalla de detalle.
 */
sealed class DetalleUiState {
    object Loading : DetalleUiState()
    data class Success(val evento: EventoDetalle) : DetalleUiState()
    data class Error(val mensaje: String) : DetalleUiState()
}

/**
 * ViewModel para la pantalla de detalle de evento.
 */
class EventoDetalleViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Loading)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        SessionManager.token?.let { apiClient.setToken(it) }
    }

    /**
     * Carga los detalles de un evento específico.
     */
    fun cargarEvento(eventoId: Long) {
        _uiState.value = DetalleUiState.Loading

        viewModelScope.launch {
            try {
                val evento = apiClient.obtenerEventoDetalle(eventoId)
                _uiState.value = DetalleUiState.Success(evento)

            } catch (e: Exception) {
                _uiState.value = DetalleUiState.Error(
                    e.message ?: "Error al cargar el evento"
                )
            }
        }
    }
}