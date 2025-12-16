package org.abel.mobile.ui.screens.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.abel.mobile.data.api.EventosApiClient
import org.abel.mobile.data.model.EventoResumen
import org.abel.mobile.util.SessionManager

/**
 * Estados posibles de la pantalla de lista de eventos.
 */
sealed class EventosUiState {
    object Loading : EventosUiState()
    data class Success(val eventos: List<EventoResumen>) : EventosUiState()
    data class Error(val mensaje: String) : EventosUiState()
}

/**
 * ViewModel para la pantalla de lista de eventos.
 */
class EventosViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    private val _uiState = MutableStateFlow<EventosUiState>(EventosUiState.Loading)
    val uiState: StateFlow<EventosUiState> = _uiState.asStateFlow()

    init {
        // Configurar token del ApiClient
        SessionManager.token?.let { apiClient.setToken(it) }
        // Cargar eventos al crear el ViewModel
        cargarEventos()
    }

    /**
     * Carga la lista de eventos desde el Backend.
     */
    fun cargarEventos() {
        _uiState.value = EventosUiState.Loading

        viewModelScope.launch {
            try {
                val eventos = apiClient.obtenerEventos()
                _uiState.value = EventosUiState.Success(eventos)

            } catch (e: Exception) {
                _uiState.value = EventosUiState.Error(
                    e.message ?: "Error al cargar eventos"
                )
            }
        }
    }

    /**
     * Refresca la lista de eventos.
     */
    fun refrescar() {
        cargarEventos()
    }
}
