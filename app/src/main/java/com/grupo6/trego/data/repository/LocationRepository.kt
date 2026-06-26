package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTODireccion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository {
    private val _currentAddress = MutableStateFlow<DTODireccion?>(null)
    val currentAddress: StateFlow<DTODireccion?> = _currentAddress.asStateFlow()

    private val _isManual = MutableStateFlow(false)
    val isManual: StateFlow<Boolean> = _isManual.asStateFlow()

    fun updateAddress(address: DTODireccion?, manual: Boolean = false) {
        _currentAddress.value = address
        _isManual.value = manual
    }
}
