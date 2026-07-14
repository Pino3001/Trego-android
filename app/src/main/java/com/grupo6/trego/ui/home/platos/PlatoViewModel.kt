package com.grupo6.trego.ui.home.platos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProductoZona
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.model.SortOrder
import com.grupo6.trego.data.repository.ProductoRepository
import kotlinx.coroutines.launch

sealed class PlatoUIState {
    object Idle : PlatoUIState()
    object Loading : PlatoUIState()
    object Empty : PlatoUIState()
    object NoCoverage : PlatoUIState()
    data class Success(val platos: List<DTOProductoZona>) : PlatoUIState()
    data class Error(val message: String) : PlatoUIState()
}

/**
 * Este ViewModel se encarga de gestionar la lista de platos de una subcategoría.
 * Se ocupa de pedir los datos al repositorio, filtrar por nombre de restaurante, 
 * por calificación mínima y de ordenar los platos por precio.
 */
class PlatoViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    private var allPlatos = emptyList<DTOProductoZona>()

    var uiState by mutableStateOf<PlatoUIState>(PlatoUIState.Idle)
        private set

    var restaurantQuery by mutableStateOf("")
        private set

    var minRating by mutableStateOf(0f)
        private set

    var sortOrder by mutableStateOf(SortOrder.DEFAULT)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    /* Carga la lista de platos desde el servidor según la subcategoría y la ubicación del usuario. */
    fun loadPlatos(subCategoria: DTOSubCategoria, direccion: DTODireccion) {
        viewModelScope.launch {
            if (allPlatos.isEmpty()) uiState = PlatoUIState.Loading
            else isRefreshing = true

            try {
                repository.listarPlatos(subCategoria, direccion)
                    .onSuccess { result ->
                        allPlatos = result
                        applyFiltersAndSort()
                    }
                    .onFailure { e ->
                        uiState = PlatoUIState.Error(e.message ?: "No se pudieron cargar los platos")
                    }
            } catch (e: Exception) {
                uiState = PlatoUIState.Error(e.message ?: "Error desconocido")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun onRestaurantQueryChange(query: String) {
        restaurantQuery = query
        applyFiltersAndSort()
    }

    fun onRatingFilterChange(rating: Float) {
        minRating = rating
        applyFiltersAndSort()
    }

    fun onSortOrderChange(order: SortOrder) {
        sortOrder = order
        applyFiltersAndSort()
    }

    /* Aplica todos los filtros seleccionados (búsqueda, estrellas y orden) a la lista original de platos. */
    private fun applyFiltersAndSort() {
        if (allPlatos.isEmpty()) {
            uiState = PlatoUIState.NoCoverage
            return
        }

        var filteredList = allPlatos.filter { item ->
            val matchesRestaurant = item.nombreRestaurante.contains(restaurantQuery, ignoreCase = true)
            val matchesRating = item.calificacionProm >= minRating
            matchesRestaurant && matchesRating
        }

        filteredList = when (sortOrder) {
            SortOrder.PRICE_ASC -> filteredList.sortedBy { it.producto.precio ?: 0f }
            SortOrder.PRICE_DESC -> filteredList.sortedByDescending { it.producto.precio ?: 0f }
            SortOrder.DEFAULT -> filteredList
        }

        uiState = if (filteredList.isEmpty()) {
            PlatoUIState.Empty
        } else {
            PlatoUIState.Success(filteredList)
        }
    }
}
