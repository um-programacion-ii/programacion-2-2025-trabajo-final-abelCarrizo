package org.abel.mobile.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestiona la sesión del usuario (token JWT).
 *
 * Usa StateFlow para que las pantallas reaccionen
 * automáticamente cuando cambia el estado de autenticación.
 */
object SessionManager {

    // Token JWT actual (null si no hay sesión)
    private var _token: String? = null
    val token: String? get() = _token

    // Username del usuario actual
    private var _username: String? = null
    val username: String? get() = _username

    // Estado observable: ¿está autenticado?
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /**
     * Guarda la sesión después de un login exitoso.
     */
    fun saveSession(token: String, username: String) {
        _token = token
        _username = username
        _isAuthenticated.value = true
    }

    /**
     * Limpia la sesión (logout).
     */
    fun clearSession() {
        _token = null
        _username = null
        _isAuthenticated.value = false
    }

    /**
     * Verifica si hay una sesión activa.
     */
    fun hasActiveSession(): Boolean {
        return _token != null
    }
}