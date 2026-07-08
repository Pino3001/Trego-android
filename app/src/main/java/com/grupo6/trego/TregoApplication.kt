package com.grupo6.trego

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.cloudinary.android.MediaManager
import com.grupo6.trego.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Esta es la clase base de la aplicación donde configuramos las herramientas globales. 
 * Acá inicializamos la inyección de dependencias con Koin, configuramos el cargador 
 * de imágenes y creamos los canales de notificaciones apenas arranca la app.
 */
class TregoApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        MediaManager.init(this)
        startKoin {
            androidContext(this@TregoApplication)
            modules(appModule)
        }
        // Llamar a la función para crear el canal de notificaciones al iniciar la app
        createNotificationChannel()
    }

    /* Creamos el canal de notificaciones necesario para que los avisos del servidor se muestren correctamente en el celular. */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "trego_default_channel" // Este ID es importante
            val channelName = "Notificaciones Generales"
            val descriptionText = "Canal para alertas y mensajes de Trego"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun crearCanalDeNotificaciones() {
        // Los canales solo son obligatorios a partir de Android 8.0 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "trego_default_channel" // ⚠️ DEBE COINCIDIR EXACTAMENTE CON EL BACKEND
            val name = "Notificaciones de Pedidos"
            val descriptionText = "Canal principal para avisos de Trego"
            val importance = NotificationManager.IMPORTANCE_HIGH // Obligatorio para el banner flotante

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            // Registramos el canal en el sistema Android
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /* Configuramos cómo se cargan las fotos en la app, agregando soporte para SVGs y manejando la memoria caché para que todo ande más rápido. */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .build()
    }
}