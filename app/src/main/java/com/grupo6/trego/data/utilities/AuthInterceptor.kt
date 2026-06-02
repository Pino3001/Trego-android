package com.grupo6.trego.data.utilities

import android.content.Context
import android.content.Intent
import okhttp3.Interceptor
import okhttp3.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Inyectar token si existe
        tokenManager.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // 401 = No autorizado, 403 = Prohibido (común en JWT expirados)
        if (response.code == 401 || response.code == 403) {
            
            // 1. Limpiar datos locales
            tokenManager.clearToken()
            Firebase.auth.signOut() 

            // 2. Redirigir al Login de forma limpia
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }

        return response
    }
}
