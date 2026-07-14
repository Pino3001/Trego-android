package com.grupo6.trego.ui.home.restaurantes

import android.util.Log
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
import com.grupo6.trego.data.utilities.AppReadyState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalTime

// ---------------------------------------------------------------------------
// Modelos de estado
// ---------------------------------------------------------------------------

data class FilterState(
    val categoria: EnumCategoriaRestaurante? = EnumCategoriaRestaurante.SinCategoria,
    val calificacionMinima: Double? = null,
    val horaDesde: LocalTime? = null,
    val horaHasta: LocalTime? = null
)

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

    // --- Estado de búsqueda por nombre ---
    private var rawSearchResults = listOf<DTORestaurante>()

    var searchUiState by mutableStateOf<SearchUiState>(SearchUiState.Idle)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isSearchMode by mutableStateOf(false)
        private set

    // --- Filtros ---

    var filterState by mutableStateOf(FilterState())
        private set

    private var rawAddressResults = listOf<DTORestaurante>()

    var addressSearchUiState by mutableStateOf<AddressSearchUiState>(AddressSearchUiState.Idle)
        private set

    var isAddressSearchMode by mutableStateOf(false)
        private set

    // OPTIMIZACIÓN: Guardamos en memoria la última dirección consultada al backend
    private var lastSearchedAddress: DTODireccion? = null

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // --- Ubicación y paginación ---

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val restaurantsFlow: Flow<PagingData<DTORestaurante>> = _currentLocation
        .filterNotNull()
        .distinctUntilChanged() // OPTIMIZACIÓN: Solo reacciona si el par de coordenadas cambia
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
            onSearchSubmit(isForceRefresh = true)
        } else if (isAddressSearchMode) {
            _currentLocation.value?.let { (lat, lon) ->
                searchRestaurantsByAddress(
                    direccion = DTODireccion(latitud = lat, longitud = lon),
                    isForceRefresh = true
                )
            }
        }
    }

    /**
     * Llama al repositorio para obtener restaurantes que cubren la dirección dada.
     * @param direccion Objeto DTODireccion creado desde la UI
     * @param isForceRefresh Obliga a hacer la petición al servidor ignorando la caché de memoria
     */
    fun searchRestaurantsByAddress(direccion: DTODireccion, isForceRefresh: Boolean = false) {
        Log.e("RestauranteViewModel", "searchRestaurantsByAddress llamada con lat=${direccion.latitud}, lon=${direccion.longitud}")
        // OPTIMIZACIÓN: Si NO es un refresh forzado, ya consultamos esta misma dirección y tenemos éxito, cortamos acá.
        if (!isForceRefresh && lastSearchedAddress == direccion && addressSearchUiState is AddressSearchUiState.Success) {
            isAddressSearchMode = true
            return
        }

        isAddressSearchMode = true
        lastSearchedAddress = direccion

        viewModelScope.launch {
            _isRefreshing.value = true

            // EVITA EL PARPADEO: Solo pasamos a Loading si no estamos recargando la vista actual
            if (!isForceRefresh) {
                addressSearchUiState = AddressSearchUiState.Loading
            }

            try {
                repository.getRestaurantsByAddress(direccion)
                    .onSuccess { list ->
                        rawAddressResults = list ?: emptyList()
                        applyFiltersToStates()
                        AppReadyState.setDataReady(true)
                    }
                    .onFailure { e ->
                        addressSearchUiState =
                            AddressSearchUiState.Error(e.message ?: "Error al buscar por dirección")
                        AppReadyState.setDataReady(true)
                    }
            } catch (e: Exception) {
                addressSearchUiState =
                    AddressSearchUiState.Error("Fallo de conexión o respuesta vacía")
                AppReadyState.setDataReady(true)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * @param isForceRefresh Obliga a hacer la petición al servidor manteniendo la lista actual visible mientras carga
     */
    fun onSearchSubmit(isForceRefresh: Boolean = false) {
        if (searchQuery.isBlank()) return
        isSearchMode = true
        viewModelScope.launch {
            _isRefreshing.value = true

            // EVITA EL PARPADEO: Solo pasamos a Loading si no estamos recargando la vista actual
            if (!isForceRefresh) {
                searchUiState = SearchUiState.Loading
            }

            try {
                repository.searchRestaurantsByName(searchQuery)
                    .onSuccess { list ->
                        rawSearchResults = list ?: emptyList()
                        applyFiltersToStates()
                        AppReadyState.setDataReady(true)
                    }
                    .onFailure { e ->
                        searchUiState = SearchUiState.Error(e.message ?: "Error desconocido")
                        AppReadyState.setDataReady(true)
                    }
            } catch (e: Exception) {
                searchUiState = SearchUiState.Error("No se pudo completar la búsqueda")
                AppReadyState.setDataReady(true)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearAddressSearch() {
        isAddressSearchMode = false
        addressSearchUiState = AddressSearchUiState.Idle
        rawAddressResults = emptyList()
        lastSearchedAddress = null // Limpiamos la caché
    }

    /**
     * Llamar para actualizar la ubicación interna y disparar la paginación.
     */
    fun updateLocation(lat: Double, lon: Double) {
        val newLocation = Pair(lat, lon)
        // OPTIMIZACIÓN: Evita reasignar el valor y disparar corrutinas si las coordenadas son idénticas
        if (_currentLocation.value != newLocation) {
            _currentLocation.value = newLocation
        }
    }

    // --- Búsqueda por nombre ---

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onClearSearch() {
        searchQuery = ""
        isSearchMode = false
        searchUiState = SearchUiState.Idle
        rawSearchResults = emptyList()
    }

    // --- Filtros ---

    fun onApplyFilter(newFilter: FilterState) {
        filterState = newFilter
        applyFiltersToStates()
    }

    fun onClearFilters() {
        filterState = FilterState()
        applyFiltersToStates()
    }

    private fun applyFiltersToStates() {
        if (isSearchMode) {
            val filtered = filterRestaurants(rawSearchResults)
            searchUiState =
                if (filtered.isEmpty()) SearchUiState.Empty else SearchUiState.Success(filtered)
        }
        if (isAddressSearchMode) {
            val filtered = filterRestaurants(rawAddressResults)
            addressSearchUiState =
                if (filtered.isEmpty()) AddressSearchUiState.Empty else AddressSearchUiState.Success(
                    filtered
                )
        }
    }

    private fun filterRestaurants(list: List<DTORestaurante>): List<DTORestaurante> {
        return list.filter { res ->
            val matchCategoria = filterState.categoria == null ||
                    filterState.categoria == EnumCategoriaRestaurante.SinCategoria ||
                    res.categoria == filterState.categoria

            val matchRating = filterState.calificacionMinima == null ||
                    (res.calificacionProm ?: 0f) >= filterState.calificacionMinima!!.toFloat()

            val matchHorario = matchesHorario(res, filterState.horaDesde, filterState.horaHasta)

            matchCategoria && matchRating && matchHorario
        }
    }
}

/**
 * Verifica si el restaurante atiende durante el rango pedido.
 */
private fun matchesHorario(
    restaurante: DTORestaurante,
    horaDesde: LocalTime?,
    horaHasta: LocalTime?
): Boolean {
    if (horaDesde == null && horaHasta == null) return true

    val apertura = restaurante.horaApertura ?: return true
    val cierre = restaurante.horaCierre ?: return true

    val desdeMin = horaDesde?.let { timeToMinutes(it) } ?: 0
    val hastaMin = horaHasta?.let { timeToMinutes(it) } ?: 1439   // 23:59

    return rangesOverlap(
        start1 = timeToMinutes(apertura),
        end1 = timeToMinutes(cierre),
        start2 = desdeMin,
        end2 = hastaMin
    )
}

private fun timeToMinutes(time: LocalTime): Int = time.hour * 60 + time.minute

private fun rangesOverlap(start1: Int, end1: Int, start2: Int, end2: Int): Boolean {
    val restauranteCruzaMedianoche = end1 < start1
    val filtroCruzaMedianoche = end2 < start2

    return when {
        !restauranteCruzaMedianoche && !filtroCruzaMedianoche ->
            start1 <= end2 && start2 <= end1

        restauranteCruzaMedianoche && !filtroCruzaMedianoche ->
            start2 <= end1 || end2 >= start1

        !restauranteCruzaMedianoche && filtroCruzaMedianoche ->
            start1 <= end2 || end1 >= start2

        else -> true
    }
}