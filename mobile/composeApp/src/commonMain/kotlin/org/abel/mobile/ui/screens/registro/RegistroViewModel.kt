package org.abel.mobile.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.abel.mobile.data.api.EventosApiClient
import org.abel.mobile.util.SessionManager

/**
 * Estados de la pantalla de Registro.
 */
sealed class RegistroUiState {
    object Idle : RegistroUiState()
    object Loading : RegistroUiState()
    object Success : RegistroUiState()
    data class Error(val mensaje: String) : RegistroUiState()
}

/**
 * ViewModel para la pantalla de Registro.
 */
class RegistroViewModel : ViewModel() {

    private val apiClient = EventosApiClient()

    private val _uiState = MutableStateFlow<RegistroUiState>(RegistroUiState.Idle)
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // Campos del formulario
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellido = MutableStateFlow("")
    val apellido: StateFlow<String> = _apellido.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    // Errores de validación
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    private val _nombreError = MutableStateFlow<String?>(null)
    val nombreError: StateFlow<String?> = _nombreError.asStateFlow()

    private val _apellidoError = MutableStateFlow<String?>(null)
    val apellidoError: StateFlow<String?> = _apellidoError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    companion object {
        const val MIN_USERNAME_LENGTH = 3
        const val MIN_PASSWORD_LENGTH = 4
        const val MIN_NOMBRE_LENGTH = 2
    }

    // Funciones para actualizar campos
    fun onUsernameChange(value: String) {
        _username.value = value
        _usernameError.value = null
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _passwordError.value = null
        _confirmPasswordError.value = null
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
        _confirmPasswordError.value = null
    }

    fun onNombreChange(value: String) {
        _nombre.value = value
        _nombreError.value = null
    }

    fun onApellidoChange(value: String) {
        _apellido.value = value
        _apellidoError.value = null
    }

    fun onEmailChange(value: String) {
        _email.value = value
        _emailError.value = null
    }

    /**
     * Valida el formulario completo.
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
            _username.value.contains(" ") -> {
                _usernameError.value = "No puede contener espacios"
                esValido = false
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
        }

        // Validar confirmación de password
        when {
            _confirmPassword.value.isBlank() -> {
                _confirmPasswordError.value = "Confirma la contraseña"
                esValido = false
            }
            _confirmPassword.value != _password.value -> {
                _confirmPasswordError.value = "Las contraseñas no coinciden"
                esValido = false
            }
        }

        // Validar nombre
        when {
            _nombre.value.isBlank() -> {
                _nombreError.value = "El nombre es requerido"
                esValido = false
            }
            _nombre.value.trim().length < MIN_NOMBRE_LENGTH -> {
                _nombreError.value = "Mínimo $MIN_NOMBRE_LENGTH caracteres"
                esValido = false
            }
        }

        // Validar apellido
        when {
            _apellido.value.isBlank() -> {
                _apellidoError.value = "El apellido es requerido"
                esValido = false
            }
            _apellido.value.trim().length < MIN_NOMBRE_LENGTH -> {
                _apellidoError.value = "Mínimo $MIN_NOMBRE_LENGTH caracteres"
                esValido = false
            }
        }

        // Validar email
        when {
            _email.value.isBlank() -> {
                _emailError.value = "El email es requerido"
                esValido = false
            }
            !_email.value.contains("@") || !_email.value.contains(".") -> {
                _emailError.value = "Email inválido"
                esValido = false
            }
        }

        return esValido
    }

    /**
     * Ejecuta el registro.
     */
    fun registrar() {
        if (!validarFormulario()) {
            return
        }

        _uiState.value = RegistroUiState.Loading

        viewModelScope.launch {
            try {
                val response = apiClient.registro(
                    username = _username.value.trim(),
                    password = _password.value,
                    nombre = _nombre.value.trim(),
                    apellido = _apellido.value.trim(),
                    email = _email.value.trim()
                )

                if (response.token.isNotBlank()) {
                    // Registro exitoso - guardar sesión automáticamente
                    SessionManager.saveSession(response.token, response.username)
                    apiClient.setToken(response.token)
                    _uiState.value = RegistroUiState.Success
                } else {
                    _uiState.value = RegistroUiState.Error(response.mensaje)
                }

            } catch (e: Exception) {
                _uiState.value = RegistroUiState.Error(
                    e.message ?: "Error de conexión"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegistroUiState.Idle
    }
}