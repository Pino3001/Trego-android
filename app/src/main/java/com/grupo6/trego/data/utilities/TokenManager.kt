package com.grupo6.trego.data.utilities

import android.content.Context
import android.content.SharedPreferences

/**
 * Esta clase se encarga de guardar y recuperar el token de sesión en el almacenamiento
 * local del teléfono. Así evitamos que el usuario tenga que loguearse cada vez que abre la app.
 */
class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val JWT_TOKEN_KEY = "jwt_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(JWT_TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(JWT_TOKEN_KEY, null)
    }

    fun clearToken() {
        prefs.edit().remove(JWT_TOKEN_KEY).apply()
    }
}