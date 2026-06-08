/*
package com.grupo6.trego

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFcmService : FirebaseMessagingService() {

    // 1. Este método se dispara cuando el Backend (o la Consola) envía un mensaje
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Log para tu PoC: Verás los datos en la consola de Android Studio
        Log.d("FCM_POC", "Mensaje recibido de: ${message.from}")

        message.notification?.let {
            Log.d("FCM_POC", "Título: ${it.title}")
            Log.d("FCM_POC", "Cuerpo: ${it.body}")
            // Aquí podrías disparar una notificación local si la app está abierta
        }
    }

    // 2. Este método es el más importante para el futuro Backend
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // PARA TU POC: Copia este token de los logs de Android Studio.
        // Lo necesitarás para enviarte mensajes solo a TI.
        Log.d("FCM_POC", "TU TOKEN ES: $token")

        // COMENTARIO PARA EL FUTURO:
        // enviarTokenAlBackend(token)
    }
}*/
