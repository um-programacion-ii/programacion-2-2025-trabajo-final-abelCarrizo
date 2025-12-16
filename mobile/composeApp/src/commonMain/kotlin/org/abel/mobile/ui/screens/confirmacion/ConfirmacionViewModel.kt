package org.abel.mobile.ui.screens.confirmacion

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
 * Datos del resumen de compra.
 */
data class ResumenCompra(
    val evento: EventoDetalle,
    val asientos: List<Asiento>,
    val precioUnitario: Double,
    val precioTotal: Double
)

/**
 * Estados de la pantalla.
 */
sealed class ConfirmacionUiState {
    object Loading : ConfirmacionUiState()
    data class Success(val resumen: ResumenCompra) : ConfirmacionUiState()
    data class Error(val mensaje: String) : ConfirmacionUiState()
    object Procesando : ConfirmacionUiState()
    data class Completado(val mensaje: String) : ConfirmacionUiState()
}

/**
 * ViewModel para la pantalla de confirmación.
 */
class ConfirmacionViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    private val _uiState = MutableStateFlow<ConfirmacionUiState>(ConfirmacionUiState.Loading)
    val uiState: StateFlow<ConfirmacionUiState> = _uiState.asStateFlow()

    init {
        SessionManager.token?.let { apiClient.setToken(it) }
        cargarResumen()
    }

    /**
     * Carga el resumen de la compra.
     */
    private fun cargarResumen() {
        _uiState.value = ConfirmacionUiState.Loading

        viewModelScope.launch {
            try {
                // Obtener sesión actual
                val sesion = apiClient.obtenerSesionActual()

                if (sesion.eventoId == null || sesion.asientosSeleccionados.isNullOrEmpty()) {
                    _uiState.value = ConfirmacionUiState.Error("No hay datos de compra")
                    return@launch
                }

                // Obtener detalles del evento
                val evento = apiClient.obtenerEventoDetalle(sesion.eventoId)

                // Calcular precio
                val precioUnitario = evento.precioEntrada?.toDouble() ?: 0.0
                val cantidadAsientos = sesion.asientosSeleccionados.size
                val precioTotal = precioUnitario * cantidadAsientos

                val resumen = ResumenCompra(
                    evento = evento,
                    asientos = sesion.asientosSeleccionados,
                    precioUnitario = precioUnitario,
                    precioTotal = precioTotal
                )

                _uiState.value = ConfirmacionUiState.Success(resumen)

            } catch (e: Exception) {
                _uiState.value = ConfirmacionUiState.Error(
                    e.message ?: "Error al cargar resumen"
                )
            }
        }
    }

    /**
     * Confirma la compra.
     */
    fun confirmarCompra(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.value = ConfirmacionUiState.Procesando

        viewModelScope.launch {
            try {
                val resultado = apiClient.confirmarVenta()

                if (resultado.resultado == true) {
                    _uiState.value = ConfirmacionUiState.Completado(
                        "¡Compra realizada con éxito!"
                    )
                    onSuccess()
                } else {
                    _uiState.value = ConfirmacionUiState.Error(
                        resultado.descripcion ?: "Error al procesar la compra"
                    )
                    onError(resultado.descripcion ?: "Error al procesar la compra")
                }

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error al confirmar compra"
                _uiState.value = ConfirmacionUiState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    /**
     * Cancela el proceso de compra.
     */
    fun cancelarCompra(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                apiClient.cancelarProceso()
            } catch (_: Exception) {
                // Ignorar errores de cancelación
            } finally {
                onComplete()
            }
        }
    }
}