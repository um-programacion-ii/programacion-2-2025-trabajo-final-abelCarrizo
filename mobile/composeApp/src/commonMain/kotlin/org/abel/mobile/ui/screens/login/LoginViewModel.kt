package org.abel.mobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.abel.mobile.data.api.EventosApiClient
import org.abel.mobile.util.SessionManager

/**
 * Representa los posibles estados de la pantalla de Login.
 */
sealed class LoginUiState {
    object Idle : LoginUiState()                    // Estado inicial, esperando input
    object Loading : LoginUiState()                 // Haciendo login...
    object Success : LoginUiState()                 // Login exitoso
    data class Error(val mensaje: String) : LoginUiState()  // Error con mensaje
}

/**
 * ViewModel para la pantalla de Login.
 * Maneja el estado y la lógica de autenticación.
 */
class LoginViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    // Estado actual de la UI
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Campos del formulario
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    /**
     * Actualiza el username cuando el usuario escribe.
     */
    fun onUsernameChange(value: String) {
        _username.value = value
    }

    /**
     * Actualiza el password cuando el usuario escribe.
     */
    fun onPasswordChange(value: String) {
        _password.value = value
    }

    /**
     * Ejecuta el login contra el Backend.
     */
    fun login() {
        // Validación básica
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = LoginUiState.Error("Usuario y contraseña son requeridos")
            return
        }

        // Cambiar a estado Loading
        _uiState.value = LoginUiState.Loading

        // Ejecutar en coroutine (no bloquea la UI)
        viewModelScope.launch {
            try {
                // Llamar al Backend
                val response = apiClient.login(_username.value, _password.value)

                // Verificar respuesta
                if (response.token.isNotBlank()) {
                    // Guardar sesión
                    SessionManager.saveSession(response.token, response.username)
                    apiClient.setToken(response.token)

                    // Éxito
                    _uiState.value = LoginUiState.Success
                } else {
                    // Error del servidor
                    _uiState.value = LoginUiState.Error(response.mensaje)
                }

            } catch (e: Exception) {
                // Error de red u otro
                _uiState.value = LoginUiState.Error(
                    e.message ?: "Error de conexión"
                )
            }
        }
    }

    /**
     * Resetea el estado a Idle (útil después de mostrar un error).
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

