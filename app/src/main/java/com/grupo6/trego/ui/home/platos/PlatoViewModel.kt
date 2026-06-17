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
    data class Success(val platos: List<DTOProductoZona>) : PlatoUIState()
    data class Error(val message: String) : PlatoUIState()
}

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

    fun loadPlatos(subCategoria: DTOSubCategoria, direccion: DTODireccion) {
        viewModelScope.launch {
            if (allPlatos.isEmpty()) uiState = PlatoUIState.Loading
            else isRefreshing = true

            try {
                val result = repository.listarPlatos(subCategoria, direccion)
                if (result != null) {
                    allPlatos = result
                    applyFiltersAndSort()
                } else {
                    uiState = PlatoUIState.Error("No se pudieron cargar los platos")
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

    private fun applyFiltersAndSort() {
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
