package com.grupo6.trego.data.notificaciones

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PushNotificationManager {
    // replay = 1 hace que guarde el último evento en memoria temporal
    private val _pushEvents = MutableSharedFlow<Map<String, String>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val pushEvents = _pushEvents.asSharedFlow()

    suspend fun emitirEvento(data: Map<String, String>) {
        _pushEvents.emit(data)
    }

    // Función para limpiar la memoria
    @OptIn(ExperimentalCoroutinesApi::class)
    fun limpiarEventos() {
        _pushEvents.resetReplayCache()
    }
}