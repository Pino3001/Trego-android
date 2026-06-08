package com.grupo6.trego.data.notificaciones

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.grupo6.trego.MainActivity
import com.grupo6.trego.R
import com.grupo6.trego.data.repository.UsuarioRepository
import com.grupo6.trego.data.utilities.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MiFirebaseMessagingService : FirebaseMessagingService() {

    private val usuarioRepository: UsuarioRepository by inject()
    private val tokenManager: TokenManager by inject()
    private val pushManager: PushNotificationManager by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "Firebase renovó el token: $token")

        // Verificamos si el usuario tiene una sesión activa (JWT existente)
        val jwtToken = tokenManager.getToken()

        if (jwtToken.isNullOrBlank()) {
            // Si no hay sesión, no hacemos la petición.
            Log.d("FCM_SERVICE", "Usuario no logueado. Se ignora el envío del token por ahora.")
            return
        }

        // Si el usuario SÍ está logueado, procedemos a actualizarlo en Java
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = usuarioRepository.actualizarFcmToken(token)
                if (result.isSuccess) {
                    Log.d(
                        "FCM_SERVICE",
                        "Desde FCMservice: Token vinculado al usuario exitosamente"
                    )
                } else {
                    Log.e(
                        "FCM_SERVICE",
                        "Error al actualizar token desde el servicio: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Excepción crítica en onNewToken", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        Log.d("FCM_SERVICE", "¡Mensaje recibido! Datos: $data")

        // 1. Extraemos los textos que pusimos en el mapa 'data' del backend
        val titulo = data["title"] ?: "Trego"
        val cuerpo = data["body"] ?: ""

        // 2. Lógica de redirección (Ej: Pago de Mercado Pago)
        // Este código ahora SÍ se ejecutará con la app cerrada
        if (data["estado"] == "PAGO_PROCESADO") {
            Log.d("FCM_SERVICE", "Aviso de pago. Avisando al ViewModel...")
            CoroutineScope(Dispatchers.IO).launch {
                pushManager.emitirEvento(data)
            }
            // Si no quieres mostrar notificación visual para pagos, puedes hacer 'return' aquí
        }

        // 3. Mostrar la notificación manualmente
        // Como el sistema no la muestra automáticamente (porque no enviamos 'notification'),
        // lo hacemos nosotros. Esto garantiza que el log de abajo se ejecute.
        if (cuerpo.isNotEmpty()) {
            mostrarNotificacionLocal(titulo, cuerpo, data)
        }
    }

    private fun mostrarNotificacionLocal(
        titulo: String,
        cuerpo: String,
        data: Map<String, String>
    ) {
        val channelId = "trego_default_channel" // ⚠️ USAMOS EL MISMO CANAL
        Log.d("FCM_SERVICE", "Este es el cuerpo $cuerpo y este el titulo: $titulo")
        // Preparar el Intent por si el usuario toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Pasamos los datos del backend a la Activity
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación atada al canal
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bolsa) // Asegúrate de tener este drawable
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Para que salga por arriba en primer plano
            .setAutoCancel(true) // Para que se borre de la lista al tocarla
            .setContentIntent(pendingIntent)

        // Lanzar la notificación al teléfono
        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // En Android 13+ puede fallar si el usuario no aceptó el permiso de notificaciones
            e.printStackTrace()
        }
    }

}