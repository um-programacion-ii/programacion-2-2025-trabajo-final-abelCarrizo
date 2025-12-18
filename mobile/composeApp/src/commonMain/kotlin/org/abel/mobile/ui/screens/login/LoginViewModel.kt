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
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val mensaje: String) : LoginUiState()
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

    // Errores de validación por campo
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    companion object {
        const val MIN_USERNAME_LENGTH = 3
        const val MIN_PASSWORD_LENGTH = 4
    }

    /**
     * Actualiza el username cuando el usuario escribe.
     */
    fun onUsernameChange(value: String) {
        _username.value = value
        // Limpiar error al escribir
        _usernameError.value = null
    }

    /**
     * Actualiza el password cuando el usuario escribe.
     */
    fun onPasswordChange(value: String) {
        _password.value = value
        // Limpiar error al escribir
        _passwordError.value = null
    }

    /**
     * Valida el formulario y retorna true si es válido.
     */
    private fun validarFormulario(): Boolean {
        var esValido = true

        // Validar username
        when {
            _username.value.isBlank() -> {
                _usernameError.value = "El usuario es requerido"
                esValido = false
            }
            _username.value.trim().length < MIN_USERNAME_LENGTH -> {
                _usernameError.value = "Mínimo $MIN_USERNAME_LENGTH caracteres"
                esValido = false
            }
            else -> {
                _usernameError.value = null
            }
        }

        // Validar password
        when {
            _password.value.isBlank() -> {
                _passwordError.value = "La contraseña es requerida"
                esValido = false
            }
            _password.value.length < MIN_PASSWORD_LENGTH -> {
                _passwordError.value = "Mínimo $MIN_PASSWORD_LENGTH caracteres"
                esValido = false
            }
            else -> {
                _passwordError.value = null
            }
        }

        return esValido
    }

    /**
     * Ejecuta el login contra el Backend.
     */
    fun login() {
        // Validar formulario primero
        if (!validarFormulario()) {
            return
        }

        // Cambiar a estado Loading
        _uiState.value = LoginUiState.Loading

        // Ejecutar en coroutine
        viewModelScope.launch {
            try {
                val response = apiClient.login(_username.value.trim(), _password.value)

                if (response.token.isNotBlank()) {
                    SessionManager.saveSession(response.token, response.username)
                    apiClient.setToken(response.token)
                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.Error(response.mensaje)
                }

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.message ?: "Error de conexión"
                )
            }
        }
    }

    /**
     * Resetea el estado a Idle.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}