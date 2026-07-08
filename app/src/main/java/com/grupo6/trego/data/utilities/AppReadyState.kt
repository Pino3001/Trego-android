package com.grupo6.trego.data.utilities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Este objeto nos sirve para controlar si la app ya terminó de cargar todos los datos 
 * iniciales y está lista para mostrarse al usuario sin problemas.
 */
object AppReadyState {
    private val _isDataReady = MutableStateFlow(false)
    val isDataReady = _isDataReady.asStateFlow()

    fun setDataReady(ready: Boolean) {
        _isDataReady.value = ready
    }
}
