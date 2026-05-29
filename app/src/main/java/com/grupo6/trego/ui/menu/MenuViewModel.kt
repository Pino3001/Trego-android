package com.grupo6.trego.ui.menu


import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTORestaurante

enum class OrdenPrecio { NINGUNO, MENOR, MAYOR }

sealed class MenuUiState {
    object Loading : MenuUiState()
    object SinProductos : MenuUiState()
    data class Success(val restaurante: DTORestaurante) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}

class MenuViewModel : ViewModel() {

    var uiState by mutableStateOf<MenuUiState>(MenuUiState.Loading)
        private set

    var categoriaSeleccionada by mutableStateOf("Todos")
        private set

    var ordenPrecio by mutableStateOf(OrdenPrecio.NINGUNO)
        private set

    var showOrdenDialog by mutableStateOf(false)
        private set

    private var todosLosProductos: List<DTOProducto> = emptyList()

    var productosFiltrados by mutableStateOf<List<DTOProducto>>(emptyList())
        private set

    var ofertas by mutableStateOf<List<DTOProducto>>(emptyList())
        private set}

/*    val categorias = listOf("Todos", "Bebidas", "Postres", "Ensaladas", "Entradas", "P. Plato")*/

/*    fun cargarMenu(restauranteId: Long) {
        // TODO: reemplazar con llamada real al backend
        val restaurante = MockData.restaurantes.find { it.id == restauranteId }
        if (restaurante == null || restaurante.productos.isNullOrEmpty()) {
            uiState = MenuUiState.SinProductos
        } else {
            todosLosProductos = restaurante.productos
            ofertas = restaurante.productos.filter { it.tieneOferta }
            aplicarFiltroYOrden()
            uiState = MenuUiState.Success(
                RestaurantDTO(
                    id = restaurante.id,
                    nombre = restaurante.nombre,
                    categoria = restaurante.categoria,
                    zona = restaurante.zona,
                    calificacion = restaurante.calificacion,
                    horarioApertura = restaurante.horarioApertura,
                    horarioCierre = restaurante.horarioCierre,
                    abierto = restaurante.abierto,
                    tieneOfertas = restaurante.tieneOfertas,
                    productos = restaurante.productos ?: emptyList()
                )
            )
        }
    }*/
/*    fun cargarMenu(restauranteId: Long) {

       viewModelScope.launch {
            uiState = MenuUiState.Loading
            try {
                val response = RetrofitClient.restaurantService
                    .verMenuRestaurante(restauranteId)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body == null) {
                        uiState = MenuUiState.Error("Error al procesar el menú")
                    } else {
                        // 💡 AQUÍ ESTÁ EL TRUCO:
                        // 1. Convertimos a una lista segura inmediatamente.
                        // 2. Si es nulo, le asignamos una lista vacía.
                        val productosSeguros = body.productos ?: emptyList()

                        if (productosSeguros.isEmpty()) {
                            uiState = MenuUiState.SinProductos
                        } else {
                            // Ahora productosSeguros NO es nulo, así que no dará error de asignación
                            todosLosProductos = productosSeguros
                            ofertas = productosSeguros.filter { it.tieneOferta }
                            aplicarFiltroYOrden()
                            uiState = MenuUiState.Success(body)
                        }
                    }
                } else {
                    uiState = MenuUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = MenuUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }*/
/*
    fun onCategoriaSeleccionada(categoria: String) {
        categoriaSeleccionada = categoria
        aplicarFiltroYOrden()
    }

    fun onOrdenSeleccionado(orden: OrdenPrecio) {
        ordenPrecio = orden
        showOrdenDialog = false
        aplicarFiltroYOrden()
    }

    fun onShowOrdenDialog() { showOrdenDialog = true }
    fun onDismissOrdenDialog() { showOrdenDialog = false }

    private fun aplicarFiltroYOrden() {
        var resultado = if (categoriaSeleccionada == "Todos") {
            todosLosProductos
        } else {
            todosLosProductos.filter {
                it.categoria.equals(categoriaSeleccionada, ignoreCase = true)
            }
        }

        resultado = when (ordenPrecio) {
            OrdenPrecio.MENOR -> resultado.sortedBy { it.precioOferta ?: it.precio }
            OrdenPrecio.MAYOR -> resultado.sortedByDescending { it.precioOferta ?: it.precio }
            OrdenPrecio.NINGUNO -> resultado
        }

        productosFiltrados = resultado
    }
}*/
