package com.grupo6.trego.ui.home.ofertas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProductoZona
import com.grupo6.trego.data.model.SortOrder
import com.grupo6.trego.data.repository.ProductoRepository
import kotlinx.coroutines.launch

sealed class OfertaUIState {
    object Idle : OfertaUIState()
    object Loading : OfertaUIState()
    object Empty : OfertaUIState()
    data class Success(val platos: List<DTOProductoZona>) : OfertaUIState()
    data class Error(val message: String) : OfertaUIState()
}

class OfertaViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    private var allOfertas = emptyList<DTOProductoZona>()

    var uiState by mutableStateOf<OfertaUIState>(OfertaUIState.Idle)
        private set

    var restaurantQuery by mutableStateOf("")
        private set

    var minRating by mutableStateOf(0f)
        private set

    var sortOrder by mutableStateOf(SortOrder.DEFAULT)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    fun loadOfertas(direccion: DTODireccion) {
        viewModelScope.launch {
            if (allOfertas.isEmpty()) uiState = OfertaUIState.Loading
            else isRefreshing = true

            try {
                val result = repository.listarOfertas(direccion)
                if (result != null) {
                    allOfertas = result
                    applyFiltersAndSort()
                } else {
                    uiState = OfertaUIState.Error("No se pudieron cargar las ofertas")
                }
            } catch (e: Exception) {
                uiState = OfertaUIState.Error(e.message ?: "Error desconocido")
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
        var filteredList = allOfertas.filter { item ->
            val matchesRestaurant =
                item.nombreRestaurante.contains(restaurantQuery, ignoreCase = true)
            val matchesRating = item.calificacionProm >= minRating
            matchesRestaurant && matchesRating
        }

        filteredList = when (sortOrder) {
            SortOrder.PRICE_ASC -> filteredList.sortedBy { it.producto.precio ?: 0f }
            SortOrder.PRICE_DESC -> filteredList.sortedByDescending {
                it.producto.precio ?: 0f
            }

            SortOrder.DEFAULT -> filteredList
        }

        uiState = if (filteredList.isEmpty()) {
            OfertaUIState.Empty
        } else {
            OfertaUIState.Success(filteredList)
        }
    }
}
