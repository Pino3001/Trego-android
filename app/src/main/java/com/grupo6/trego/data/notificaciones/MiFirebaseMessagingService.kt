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

/**
 * Este servicio es el que está atento a todo lo que llega de Firebase para gestionar las notificaciones.
 */
class MiFirebaseMessagingService : FirebaseMessagingService() {

    private val usuarioRepository: UsuarioRepository by inject()
    private val tokenManager: TokenManager by inject()
    private val pushManager: PushNotificationManager by inject()

    /**
     * Cuando el token de Firebase cambia, se lo mandamos al servidor para seguir vinculados,
     * pero solo si el usuario ya inició sesión.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "Firebase renovó el token: $token")

        val jwtToken = tokenManager.getToken()

        if (jwtToken.isNullOrBlank()) {
            Log.d("FCM_SERVICE", "Usuario no logueado. Se ignora el envío del token por ahora.")
            return
        }

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

    /**
     * Acá recibimos los datos del backend. Si es un aviso de pago, notificamos al manager
     * para que la UI se entere, y en cualquier caso mostramos la alerta visual en el celu.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        Log.d("FCM_SERVICE", "¡Mensaje recibido! Datos: $data")

        val titulo = data["title"] ?: "Trego"
        val cuerpo = data["body"] ?: ""

        if (data["estado"] == "PAGO_PROCESADO") {
            Log.d("FCM_SERVICE", "Aviso de pago. Avisando al ViewModel...")
            CoroutineScope(Dispatchers.IO).launch {
                pushManager.emitirEvento(data)
            }
        }

        if (cuerpo.isNotEmpty()) {
            mostrarNotificacionLocal(titulo, cuerpo, data)
        }
    }

    /**
     * Esta es la parte que arma la ventanita de notificación en el celu y configura
     * que se abra la MainActivity con todos los datos necesarios cuando el usuario la toca.
     */
    private fun mostrarNotificacionLocal(
        titulo: String,
        cuerpo: String,
        data: Map<String, String>
    ) {
        val channelId = "trego_default_channel" 
        Log.d("FCM_SERVICE", "Este es el cuerpo $cuerpo y este el titulo: $titulo")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bolsa)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

}