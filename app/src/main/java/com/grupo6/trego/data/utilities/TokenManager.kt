package com.grupo6.trego.data.utilities

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Esta clase se encarga de guardar y recuperar el token de sesión en el almacenamiento
 * local del teléfono. Así evitamos que el usuario tenga que loguearse cada vez que abre la app.
 */
class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Inicializamos directamente leyendo de SharedPreferences, sin llamar a getToken()
    private val _isTokenAvailable = MutableStateFlow(
        prefs.getString(JWT_TOKEN_KEY, null) != null
    )
    val isTokenAvailable: StateFlow<Boolean> = _isTokenAvailable.asStateFlow()

    companion object {
        private const val JWT_TOKEN_KEY = "jwt_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(JWT_TOKEN_KEY, token).apply()
        _isTokenAvailable.value = true
    }

    fun getToken(): String? {
        return prefs.getString(JWT_TOKEN_KEY, null)
    }

    fun clearToken() {
        prefs.edit().remove(JWT_TOKEN_KEY).apply()
        _isTokenAvailable.value = false
    }
}