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
 * Representa un asiento con su campo de nombre editable.
 */
data class AsientoConNombre(
    val fila: Int,
    val columna: Int,
    var nombre: String = ""
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
                // Obtener sesión actual con los asientos bloqueados
                val sesion = apiClient.obtenerSesionActual()

                if (sesion.asientosSeleccionados.isNullOrEmpty()) {
                    _uiState.value = DatosUiState.Error("No hay asientos seleccionados")
                    return@launch
                }

                // Convertir a modelo con nombre editable
                asientosConNombre.clear()
                sesion.asientosSeleccionados.forEach { asiento ->
                    asientosConNombre.add(
                        AsientoConNombre(
                            fila = asiento.fila,
                            columna = asiento.columna,
                            nombre = asiento.persona ?: ""
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
     * Actualiza el nombre de un asiento.
     */
    fun onNombreChange(fila: Int, columna: Int, nombre: String) {
        val index = asientosConNombre.indexOfFirst {
            it.fila == fila && it.columna == columna
        }

        if (index != -1) {
            asientosConNombre[index] = asientosConNombre[index].copy(nombre = nombre)
            _uiState.value = DatosUiState.Success(asientosConNombre.toList())
        }
    }

    /**
     * Valida que todos los asientos tengan nombre.
     */
    fun validarNombres(): Boolean {
        return asientosConNombre.all { it.nombre.isNotBlank() }
    }

    /**
     * Asigna las personas a los asientos y continúa.
     */
    fun confirmarDatos(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!validarNombres()) {
            onError("Todos los asientos deben tener un nombre asignado")
            return
        }

        viewModelScope.launch {
            try {
                // Convertir a modelo de API
                val asientosApi = asientosConNombre.map {
                    Asiento(
                        fila = it.fila,
                        columna = it.columna,
                        persona = it.nombre
                    )
                }

                // Llamar al endpoint de asignar personas
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