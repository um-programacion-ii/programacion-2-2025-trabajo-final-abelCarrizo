package org.abel.mobile.ui.screens.datos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.abel.mobile.data.api.EventosApiClient
import org.abel.mobile.data.model.Asiento
import org.abel.mobile.util.SessionManager

/**
 * Representa un asiento con su campo de nombre editable y error de validación.
 */
data class AsientoConNombre(
    val fila: Int,
    val columna: Int,
    var nombre: String = "",
    var error: String? = null  // Error de validación
)

/**
 * Estados de la pantalla.
 */
sealed class DatosUiState {
    object Loading : DatosUiState()
    data class Success(val asientos: List<AsientoConNombre>) : DatosUiState()
    data class Error(val mensaje: String) : DatosUiState()
}

/**
 * ViewModel para la pantalla de datos personales.
 */
class DatosPersonalesViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    private val _uiState = MutableStateFlow<DatosUiState>(DatosUiState.Loading)
    val uiState: StateFlow<DatosUiState> = _uiState.asStateFlow()

    private val asientosConNombre = mutableListOf<AsientoConNombre>()

    companion object {
        const val MIN_NOMBRE_LENGTH = 3
    }

    init {
        SessionManager.token?.let { apiClient.setToken(it) }
        cargarAsientosSeleccionados()
    }

    /**
     * Carga los asientos seleccionados de la sesión activa.
     */
    private fun cargarAsientosSeleccionados() {
        _uiState.value = DatosUiState.Loading

        viewModelScope.launch {
            try {
                val sesion = apiClient.obtenerSesionActual()

                if (sesion.asientosSeleccionados.isNullOrEmpty()) {
                    _uiState.value = DatosUiState.Error("No hay asientos seleccionados")
                    return@launch
                }

                asientosConNombre.clear()
                sesion.asientosSeleccionados.forEach { asiento ->
                    asientosConNombre.add(
                        AsientoConNombre(
                            fila = asiento.fila,
                            columna = asiento.columna,
                            nombre = asiento.persona ?: "",
                            error = null
                        )
                    )
                }

                _uiState.value = DatosUiState.Success(asientosConNombre.toList())

            } catch (e: Exception) {
                _uiState.value = DatosUiState.Error(
                    e.message ?: "Error al cargar asientos"
                )
            }
        }
    }

    /**
     * Actualiza el nombre de un asiento y limpia su error.
     */
    fun onNombreChange(fila: Int, columna: Int, nombre: String) {
        val index = asientosConNombre.indexOfFirst {
            it.fila == fila && it.columna == columna
        }

        if (index != -1) {
            asientosConNombre[index] = asientosConNombre[index].copy(
                nombre = nombre,
                error = null  // Limpiar error al escribir
            )
            _uiState.value = DatosUiState.Success(asientosConNombre.toList())
        }
    }

    /**
     * Valida un nombre individual.
     */
    private fun validarNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre es requerido"
            nombre.trim().length < MIN_NOMBRE_LENGTH -> "Mínimo $MIN_NOMBRE_LENGTH caracteres"
            else -> null
        }
    }

    /**
     * Valida todos los nombres y actualiza los errores.
     * Retorna true si todos son válidos.
     */
    private fun validarTodosLosNombres(): Boolean {
        var todosValidos = true

        asientosConNombre.forEachIndexed { index, asiento ->
            val error = validarNombre(asiento.nombre)
            asientosConNombre[index] = asiento.copy(error = error)
            if (error != null) {
                todosValidos = false
            }
        }

        // Actualizar UI con los errores
        _uiState.value = DatosUiState.Success(asientosConNombre.toList())

        return todosValidos
    }

    /**
     * Asigna las personas a los asientos y continúa.
     */
    fun confirmarDatos(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Validar todos los nombres
        if (!validarTodosLosNombres()) {
            onError("Corrige los errores en los nombres")
            return
        }

        viewModelScope.launch {
            try {
                val asientosApi = asientosConNombre.map {
                    Asiento(
                        fila = it.fila,
                        columna = it.columna,
                        persona = it.nombre.trim()
                    )
                }

                val resultado = apiClient.asignarPersonas(asientosApi)

                if (resultado.exito) {
                    onSuccess()
                } else {
                    onError(resultado.mensaje)
                }

            } catch (e: Exception) {
                onError(e.message ?: "Error al asignar personas")
            }
        }
    }
}