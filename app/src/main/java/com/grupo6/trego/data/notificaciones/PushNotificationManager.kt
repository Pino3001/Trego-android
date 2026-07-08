package com.grupo6.trego.data.notificaciones

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/*
 * Uso esta clase como un puente para avisarle a la interfaz cuando llega una notificación
 * en tiempo real sin tener que acoplar el servicio de Firebase con los ViewModels.
 */
class PushNotificationManager {
    /*
     * Uso un SharedFlow con replay para que el último mensaje se quede guardado 
     * un momento y la app pueda reaccionar apenas se abra si vino de una notificación.
     */
    private val _pushEvents = MutableSharedFlow<Map<String, String>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val pushEvents = _pushEvents.asSharedFlow()

    suspend fun emitirEvento(data: Map<String, String>) {
        _pushEvents.emit(data)
    }

    /*
     * Esto sirve para vaciar el historial de eventos y que no se vuelvan a disparar
     * acciones viejas por error cuando el usuario entra a una pantalla de nuevo.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun limpiarEventos() {
        _pushEvents.resetReplayCache()
    }
}