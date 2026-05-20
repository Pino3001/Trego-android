package com.grupo6.trego.data.utilities

import okhttp3.Interceptor
import okhttp3.Response

// Se encargara de enviar el token de inicio el las funciones que no sean publicas
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Si tenemos un token guardado, lo inyectamos automáticamente
        tokenManager.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}