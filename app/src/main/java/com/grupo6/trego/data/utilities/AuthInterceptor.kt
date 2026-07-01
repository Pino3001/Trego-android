package com.grupo6.trego.data.utilities

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val token = tokenManager.getToken()

        // Inyectar token si existe
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // 1. Obtenemos la ruta en minúsculas
        val path = originalRequest.url.encodedPath.lowercase()

        // 2. Protegemos las rutas EXACTAS de tu backend basándonos en tu AuthController
        val isAuthRoute = path.contains("/api/auth/google") ||
                path.contains("/api/auth/sms") ||
                path.contains("/api/auth/registro") ||
                path.contains("/api/auth/vincular")

        // 3. Atrapamos el 401 y 403 SOLO si no estamos intentando iniciar sesión/registrarnos
        if ((response.code == 401 || response.code == 403) && !isAuthRoute) {

            if (!token.isNullOrEmpty()) {
                // El token venció o el usuario fue deshabilitado mientras usaba la app.
                // Limpiamos la sesión de la app y de Firebase
                tokenManager.clearToken()
                Firebase.auth.signOut()

                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
            }
        }

        return response
    }
}