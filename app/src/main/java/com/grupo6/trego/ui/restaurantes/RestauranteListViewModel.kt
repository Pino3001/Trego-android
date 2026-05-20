package com.grupo6.trego.ui.restaurantes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.mock.MockData
import com.grupo6.trego.data.model.RestaurantDTO
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.launch

data class FilterState(
    val categoria: String? = null,
    val calificacionMinima: Double? = null,
    val soloAbiertos: Boolean = false
)

sealed class RestaurantUiState {
    object Loading : RestaurantUiState()
    object Empty : RestaurantUiState()
    object LocationDisabled : RestaurantUiState()
    data class Success(val restaurants: List<RestaurantDTO>) : RestaurantUiState()
    data class Error(val message: String) : RestaurantUiState()
}

class RestaurantListViewModel(private val repository: RestauranteRepository = RestauranteRepository()) :
    ViewModel() {

    companion object {
        const val USE_MOCK = false  // cambiá a false cuando quieras conectar con el backend
    }

    var uiState by mutableStateOf<RestaurantUiState>(RestaurantUiState.Loading)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var filterState by mutableStateOf(FilterState())
        private set

    var isSearchMode by mutableStateOf(false)
        private set

    private var allRestaurants: List<RestaurantDTO> = emptyList()

    // --- Ubicación ---

    fun onLocationAvailable(latitud: Double, longitud: Double) {
        loadRestaurants(latitud, longitud)
    }

    fun onLocationDisabled() {
        uiState = RestaurantUiState.LocationDisabled
    }

    // --- Carga principal ---

    fun loadRestaurants(latitud: Double, longitud: Double) {
        if (USE_MOCK) {
            allRestaurants = MockData.restaurantes
            applyFilters()
            return
        }
        viewModelScope.launch {
            uiState = RestaurantUiState.Loading
            repository.getRestaurantsByZone(latitud, longitud)
                .onSuccess { list ->
                    allRestaurants = list
                    applyFilters()
                }
                .onFailure { e ->
                    uiState = RestaurantUiState.Error(e.message ?: "Error desconocido")
                }
        }
    }

    // --- Búsqueda por nombre ---

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onSearchSubmit() {
        if (searchQuery.isBlank()) return
        isSearchMode = true
        if (USE_MOCK) {
            // Filtro local en modo mock
            val results =
                allRestaurants.filter { it.nombre.contains(searchQuery, ignoreCase = true) }
            uiState = if (results.isEmpty()) RestaurantUiState.Empty else RestaurantUiState.Success(
                results
            )
            return
        }
        viewModelScope.launch {
            uiState = RestaurantUiState.Loading
            repository.searchRestaurantsByName(searchQuery)
                .onSuccess { list ->
                    uiState =
                        if (list.isEmpty()) RestaurantUiState.Empty else RestaurantUiState.Success(
                            list
                        )
                }
                .onFailure { e ->
                    uiState = RestaurantUiState.Error(e.message ?: "Error desconocido")
                }
        }
    }

    fun onClearSearch() {
        searchQuery = ""
        isSearchMode = false
        applyFilters()
    }

    // --- Filtros ---

    fun onApplyFilter(newFilter: FilterState) {
        filterState = newFilter
        applyFilters()
    }

    fun onClearFilters() {
        filterState = FilterState()
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allRestaurants

        filterState.categoria?.let { cat ->
            filtered = filtered.filter { it.categoria.equals(cat, ignoreCase = true) }
        }
        filterState.calificacionMinima?.let { min ->
            filtered = filtered.filter { it.calificacion >= min }
        }
        if (filterState.soloAbiertos) {
            filtered = filtered.filter { it.abierto }
        }

        uiState = if (filtered.isEmpty()) RestaurantUiState.Empty
        else RestaurantUiState.Success(filtered)
    }
}