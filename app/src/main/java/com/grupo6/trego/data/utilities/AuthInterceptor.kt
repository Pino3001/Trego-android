package com.grupo6.trego.data.utilities

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Este interceptor es como un guardia de seguridad para nuestras peticiones al servidor.
 * Se encarga de poner el token de usuario en cada llamada y, si detecta que la sesión
 * venció, limpia todo y manda al usuario de vuelta al inicio.
 */
class AuthInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    // Evita que múltiples respuestas 401/403 en paralelo (varias requests al abrir la app
    // con el token vencido) disparen el reinicio más de una vez. Sin esto, cada request
    // encola su propio reinicio y uno tardío puede caer después del re-login,
    // obligando a iniciar sesión dos veces.
    private val isSessionCleanupInProgress = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val token = tokenManager.getToken()

        // Inyectar token si existe
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // Obtenemos la ruta en minúsculas
        val path = originalRequest.url.encodedPath.lowercase()

        // Protegemos las rutas EXACTAS de tu backend basándonos en tu AuthController
        val isAuthRoute = path.contains("/api/auth/google") ||
                path.contains("/api/auth/sms") ||
                path.contains("/api/auth/registro") ||
                path.contains("/api/auth/vincular")

        // Atrapamos el 401 y 403 SOLO si no estamos intentando iniciar sesión/registrarnos
        if ((response.code == 401 || response.code == 403) && !isAuthRoute) {

            // Solo el primer 401/403 dispara la limpieza y el reinicio; el resto de
            // respuestas en paralelo lo ignoran.
            if (!token.isNullOrEmpty() && isSessionCleanupInProgress.compareAndSet(false, true)) {
                // El token venció o el usuario fue deshabilitado mientras usaba la app.
                // Limpiamos la sesión de la app y de Firebase
                tokenManager.clearToken()
                Firebase.auth.signOut()

                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
            }
        } else if (response.isSuccessful && !token.isNullOrEmpty()) {
            // La sesión volvió a estar sana (respuesta OK con token válido). Re-armamos
            // el guard para que un vencimiento futuro vuelva a disparar la limpieza.
            // El interceptor es un singleton de Koin y sobrevive al reinicio de la Activity.
            isSessionCleanupInProgress.set(false)
        }

        return response
    }
}