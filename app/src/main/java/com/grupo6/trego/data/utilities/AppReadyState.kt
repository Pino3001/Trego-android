package com.grupo6.trego.data.utilities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppReadyState {
    private val _isDataReady = MutableStateFlow(false)
    val isDataReady = _isDataReady.asStateFlow()

    fun setDataReady(ready: Boolean) {
        _isDataReady.value = ready
    }
}
