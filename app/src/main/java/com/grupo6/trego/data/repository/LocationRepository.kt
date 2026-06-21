package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTODireccion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository {
    private val _currentAddress = MutableStateFlow<DTODireccion?>(null)
    val currentAddress: StateFlow<DTODireccion?> = _currentAddress.asStateFlow()

    fun updateAddress(address: DTODireccion?) {
        _currentAddress.value = address
    }
}
