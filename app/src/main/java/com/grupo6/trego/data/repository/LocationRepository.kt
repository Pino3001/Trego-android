package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTODireccion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Este repositorio sirve para mantener actualizada la ubicación del usuario en toda la app, 
 * ya sea detectada por GPS o ingresada manualmente.
 */
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
