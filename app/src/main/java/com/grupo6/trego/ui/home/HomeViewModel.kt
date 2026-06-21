package com.grupo6.trego.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.repository.LocationRepository
import com.grupo6.trego.data.repository.UsuarioRepository
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.data.utilities.reverseGeocode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeDireccionesState {
    object Cargando : HomeDireccionesState()
    data class Cargadas(val items: List<DTODireccion>) : HomeDireccionesState()
    data class Error(val message: String) : HomeDireccionesState()
}

class HomeViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation.asStateFlow()

    val currentAddress: StateFlow<DTODireccion?> = locationRepository.currentAddress

    private val _isFetchingAddress = MutableStateFlow(false)
    val isFetchingAddress: StateFlow<Boolean> = _isFetchingAddress.asStateFlow()

    private val _direccionesState = MutableStateFlow<HomeDireccionesState>(HomeDireccionesState.Cargando)
    val direccionesState: StateFlow<HomeDireccionesState> = _direccionesState.asStateFlow()

    fun setManualAddress(direccion: DTODireccion) {
        locationRepository.updateAddress(direccion)
        _locationState.value = LocationState.Available(direccion.latitud, direccion.longitud)
        _currentLocation.value = Pair(direccion.latitud, direccion.longitud)
    }

    fun cargarDirecciones() {
        viewModelScope.launch {
            _direccionesState.value = HomeDireccionesState.Cargando
            val result = usuarioRepository.obtenerDirecciones()
            if (result.isSuccess) {
                val direcciones = result.getOrNull() ?: emptyList()
                _direccionesState.value = HomeDireccionesState.Cargadas(direcciones)
            } else {
                val error = result.exceptionOrNull()
                _direccionesState.value =
                    HomeDireccionesState.Error(error?.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Llamar cuando se obtienen coordenadas válidas desde [RequestLocation].
     */
    fun onLocationAvailable(lat: Double, lon: Double) {
        _locationState.value = LocationState.Available(lat, lon)
        _currentLocation.value = Pair(lat, lon)
        viewModelScope.launch {
            _isFetchingAddress.value = true
            val address = reverseGeocode(lat, lon)
            locationRepository.updateAddress(address)
            _isFetchingAddress.value = false
        }
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
