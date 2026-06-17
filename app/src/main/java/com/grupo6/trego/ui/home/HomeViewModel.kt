package com.grupo6.trego.ui.home

import androidx.lifecycle.ViewModel
import com.grupo6.trego.data.utilities.LocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation.asStateFlow()

    /**
     * Llamar cuando se obtienen coordenadas válidas desde [RequestLocation].
     */
    fun onLocationAvailable(lat: Double, lon: Double) {
        _locationState.value = LocationState.Available(lat, lon)
        _currentLocation.value = Pair(lat, lon)
    }

    /**
     * Llamar cuando el GPS está activo pero no devolvió coordenadas (fix pendiente).
     */
    fun onLocationUnavailable() {
        _locationState.value = LocationState.LocationUnavailable
        _currentLocation.value = null
    }

    /**
     * Llamar cuando el usuario denegó el permiso de ubicación.
     */
    fun onPermissionDenied() {
        _locationState.value = LocationState.PermissionDenied
        _currentLocation.value = null
    }

    /**
     * Llamar mientras el diálogo de permiso está abierto.
     */
    fun onRequestingPermission() {
        _locationState.value = LocationState.RequestingPermission
    }
}
