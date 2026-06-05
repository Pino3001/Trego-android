package com.grupo6.trego.ui.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.List
import kotlin.collections.distinct
import kotlin.collections.filter
import kotlin.collections.listOf
import kotlin.collections.plus
import kotlin.collections.sorted
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending

class MenuViewModel(
    private val repository: RestauranteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    fun loadMenu(restaurantId: Long) {
        Log.e("Load", restaurantId.toString())
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            repository.getRestaurantMenu(restaurantId)
                .onSuccess { restaurante ->
                    val productos = restaurante.productos ?: emptyList()

                    // Ofertas: productos que tienen el campo oferta != null
                    val ofertas = productos.filter { it.oferta != null }

                    _uiState.value = if (productos.isEmpty()) {
                        MenuUiState.SinProductos
                    } else {
                        MenuUiState.Success(
                            restaurante = restaurante,
                            productosOriginales = productos,
                            categorias = listOf("Todos") +
                                    productos.mapNotNull { it.categoria?.name }.distinct().sorted(),
                            subcategorias = listOf("Todos") +
                                    productos.mapNotNull { it.subcategoria?.nombre }.distinct().sorted(),
                            ofertas = ofertas   // ← lista cargada
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = MenuUiState.Error(
                        e.message ?: "Error al cargar el menú"
                    )
                }
        }
    }

    fun selectCategoria(categoria: String) = updateSuccess {
        copy(categoriaSeleccionada = categoria, subcategoriaSeleccionada = "Todos")
    }

    fun selectSubcategoria(subcategoria: String) = updateSuccess {
        copy(subcategoriaSeleccionada = subcategoria)
    }

    fun resetFiltros() = updateSuccess {
        copy(
            categoriaSeleccionada = "Todos",
            subcategoriaSeleccionada = "Todos",
            ordenPrecio = OrdenPrecio.NINGUNO
        )
    }

    fun showOrdenDialog() = updateSuccess { copy(showOrdenDialog = true) }
    fun dismissOrdenDialog() = updateSuccess { copy(showOrdenDialog = false) }

    // selectOrden también cierra el dialog
    fun selectOrden(orden: OrdenPrecio) = updateSuccess {
        copy(ordenPrecio = orden, showOrdenDialog = false)
    }

    // Helper privado — evita el cast repetido en cada acción
    private fun updateSuccess(block: MenuUiState.Success.() -> MenuUiState.Success) {
        val current = _uiState.value as? MenuUiState.Success ?: return
        _uiState.value = current.block()
    }
}

enum class OrdenPrecio { NINGUNO, MENOR, MAYOR }

sealed class MenuUiState {
    object Loading : MenuUiState()
    object SinProductos : MenuUiState()
    data class Error(val message: String) : MenuUiState()
    data class Success(
        val restaurante: DTORestaurante,
        val productosOriginales: List<DTOProducto>,   // nunca se toca
        val categorias: List<String>,
        val categoriaSeleccionada: String = "Todos",
        val subcategorias: List<String> = emptyList(),
        val subcategoriaSeleccionada: String = "Todos",
        val ordenPrecio: OrdenPrecio = OrdenPrecio.NINGUNO,
        val showOrdenDialog: Boolean = false,
        val ofertas: List<DTOProducto> = emptyList() // hasta que implementes ofertas
    ) : MenuUiState() {
        // Lista derivada — siempre consistente
        val productosFiltrados: List<DTOProducto>
            get() {
                var filtrados = if (categoriaSeleccionada == "Todos") {
                    productosOriginales
                } else {
                    productosOriginales.filter {
                        it.categoria?.name == categoriaSeleccionada
                    }
                }

                if (subcategoriaSeleccionada != "Todos") {
                    filtrados = filtrados.filter {
                        it.subcategoria?.nombre == subcategoriaSeleccionada
                    }
                }

                return when (ordenPrecio) {
                    OrdenPrecio.MENOR -> filtrados.sortedBy {
                        it.calcularPrecioConDescuento()
                    }
                    OrdenPrecio.MAYOR -> filtrados.sortedByDescending {
                        it.calcularPrecioConDescuento()
                    }
                    OrdenPrecio.NINGUNO -> filtrados
                }
            }
    }
}