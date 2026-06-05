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

        val token = tokenManager.getToken()
        
        // Inyectar token si existe
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        // 401 = No autorizado, 403 = Prohibido
        if (response.code == 401 || response.code == 403) {
            
            // Solo redirigimos si realmente intentamos usar un token que falló.
            // Si 'token' era null, el 401 es porque la UI llamó a una ruta protegida sin estar logueado.
            if (token != null) {
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
