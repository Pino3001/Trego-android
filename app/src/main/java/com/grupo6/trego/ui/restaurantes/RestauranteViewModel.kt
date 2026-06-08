package com.grupo6.trego.ui.restaurantes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.model.EnumCategoriaRestaurante
import com.grupo6.trego.data.repository.RestauranteRepository
import com.grupo6.trego.data.utilities.LocationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Modelos de estado
// ---------------------------------------------------------------------------

data class FilterState(
    val categoria: EnumCategoriaRestaurante? = EnumCategoriaRestaurante.SinCategoria,
    val calificacionMinima: Double? = null,
    val soloAbiertos: Boolean = false
)

/**
 * Estado de la pantalla para búsquedas por nombre (no aplica a la lista paginada).
 */
sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class Success(val restaurants: List<DTORestaurante>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

sealed class AddressSearchUiState {
    object Idle : AddressSearchUiState()
    object Loading : AddressSearchUiState()
    object Empty : AddressSearchUiState()
    data class Success(val restaurants: List<DTORestaurante>) : AddressSearchUiState()
    data class Error(val message: String) : AddressSearchUiState()
}
// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class RestauranteViewModel(
    private val repository: RestauranteRepository
) : ViewModel() {

    // --- Estado de ubicación (expuesto para que la Screen decida qué mostrar) ---

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    // --- Estado de búsqueda por nombre ---

    var searchUiState by mutableStateOf<SearchUiState>(SearchUiState.Idle)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isSearchMode by mutableStateOf(false)
        private set

    // --- Filtros ---

    var filterState by mutableStateOf(FilterState())
        private set

    var addressSearchUiState by mutableStateOf<AddressSearchUiState>(AddressSearchUiState.Idle)
        private set

    // NUEVO: flag para saber si estamos mostrando resultados de dirección (opcional)
    var isAddressSearchMode by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // --- Ubicación y paginación ---

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val restaurantsFlow: Flow<PagingData<DTORestaurante>> = _currentLocation
        .filterNotNull()
        .flatMapLatest { (lat, lon) ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    prefetchDistance = 3,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { RestaurantPagingSource(repository, lat, lon) }
            ).flow
        }

    fun refresh() {
        if (isSearchMode) {
            onSearchSubmit()
        } else if (isAddressSearchMode) {
            _currentLocation.value?.let { (lat, lon) ->
                searchRestaurantsByAddress(DTODireccion(latitud = lat, longitud = lon))
            }
        }
    }

    /**
     * Llama al repositorio para obtener restaurantes que cubren la dirección dada.
     * @param direccion Objeto DTODireccion creado desde la UI (ej. con geocodificación o entrada manual)
     */
    fun searchRestaurantsByAddress(direccion: DTODireccion) {
        isAddressSearchMode = true
        viewModelScope.launch {
            _isRefreshing.value = true
            addressSearchUiState = AddressSearchUiState.Loading
            try {
                repository.getRestaurantsByAddress(direccion)
                    .onSuccess { list ->
                        addressSearchUiState = if (list.isEmpty()) AddressSearchUiState.Empty
                        else AddressSearchUiState.Success(list)
                    }
                    .onFailure { e ->
                        addressSearchUiState =
                            AddressSearchUiState.Error(e.message ?: "Error al buscar por dirección")
                    }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Limpia los resultados de búsqueda por dirección y vuelve al modo normal (lista paginada por ubicación).
     */
    fun clearAddressSearch() {
        isAddressSearchMode = false
        addressSearchUiState = AddressSearchUiState.Idle
    }


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

// --- Búsqueda por nombre ---

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onSearchSubmit() {
        if (searchQuery.isBlank()) return
        isSearchMode = true
        viewModelScope.launch {
            _isRefreshing.value = true
            searchUiState = SearchUiState.Loading
            try {
                repository.searchRestaurantsByName(searchQuery)
                    .onSuccess { list ->
                        searchUiState = if (list.isEmpty()) SearchUiState.Empty
                        else SearchUiState.Success(list)
                    }
                    .onFailure { e ->
                        searchUiState = SearchUiState.Error(e.message ?: "Error desconocido")
                    }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onClearSearch() {
        searchQuery = ""
        isSearchMode = false
        searchUiState = SearchUiState.Idle
    }

// --- Filtros ---

    fun onApplyFilter(newFilter: FilterState) {
        filterState = newFilter
    }

    fun onClearFilters() {
        filterState = FilterState()
    }
}