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

    val currentAddress: StateFlow<DTODireccion?> = locationRepository.currentAddress

    private val _isFetchingAddress = MutableStateFlow(false)
    val isFetchingAddress: StateFlow<Boolean> = _isFetchingAddress.asStateFlow()

    private val _direccionesState =
        MutableStateFlow<HomeDireccionesState>(HomeDireccionesState.Cargando)
    val direccionesState: StateFlow<HomeDireccionesState> = _direccionesState.asStateFlow()
    private var isLocationInitialized = false

    fun resetLocationInitialization() {
        isLocationInitialized = false
        _locationState.value = LocationState.Idle
    }

    fun setManualAddress(direccion: DTODireccion) {
        isLocationInitialized = true
        locationRepository.updateAddress(direccion, manual = true)
        _locationState.value = LocationState.Available(direccion.latitud, direccion.longitud)
        _currentLocation.value = Pair(direccion.latitud, direccion.longitud)
    }

    fun cargarDirecciones() {
        // OPTIMIZACIÓN: No recargar si ya tenemos datos, a menos que sea necesario
        if (_direccionesState.value is HomeDireccionesState.Cargadas) return

        viewModelScope.launch {
            _direccionesState.value = HomeDireccionesState.Cargando
            usuarioRepository.obtenerDirecciones()
                .onSuccess { direcciones ->
                    _direccionesState.value =
                        HomeDireccionesState.Cargadas(direcciones ?: emptyList())
                }
                .onFailure { e ->
                    _direccionesState.value =
                        HomeDireccionesState.Error(e.message ?: "Error desconocido")
                }
        }
    }

    fun onLocationAvailable(lat: Double, lon: Double) {
        if (isLocationInitialized) return

        isLocationInitialized = true
        _locationState.value = LocationState.Available(lat, lon)
        _currentLocation.value = Pair(lat, lon)

        // OPTIMIZACIÓN: Solo geocodificar si no es manual
        if (!locationRepository.isManual.value) {
            fetchReverseGeocode(lat, lon)
        }
    }

    private fun fetchReverseGeocode(lat: Double, lon: Double) {
        // Aseguramos que no se lancen múltiples peticiones
        if (_isFetchingAddress.value) return

        viewModelScope.launch {
            _isFetchingAddress.value = true
            try {
                val address = reverseGeocode(lat, lon)
                if (address != null) {
                    locationRepository.updateAddress(address, manual = false)
                }
            } finally {
                _isFetchingAddress.value = false
            }
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
