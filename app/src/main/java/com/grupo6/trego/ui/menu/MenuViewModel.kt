package com.grupo6.trego.ui.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOComentario
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MenuViewModel(
    private val repository: RestauranteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<MenuUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private inline fun updateSuccess(crossinline update: MenuUiState.Success.() -> MenuUiState.Success) {
        _uiState.update { currentState ->
            if (currentState is MenuUiState.Success) {
                currentState.update()
            } else {
                currentState
            }
        }
    }

    fun loadMenu(restaurantId: Long) {
        Log.e("Load", restaurantId.toString())
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading

            repository.getRestaurantMenu(restaurantId)
                .onSuccess { restaurante ->
                    val productos = restaurante.productos ?: emptyList()
                    val ofertas = productos.filter { it.oferta != null }

                    if (productos.isEmpty()) {
                        _uiState.value = MenuUiState.SinProductos
                    } else {
                        _uiState.value = MenuUiState.Success(
                            restaurante = restaurante,
                            productosOriginales = productos,
                            categorias = listOf("Todos") +
                                    productos.mapNotNull { it.categoria?.name }.distinct().sorted(),
                            ofertas = ofertas,
                            resenas = emptyList()
                        )

                        loadResenas(restaurantId)
                    }
                }
                .onFailure { e ->
                    _uiState.value = MenuUiState.Error(
                        e.message ?: "Error al cargar el menú"
                    )
                }
        }
    }

    fun loadResenas(idRestaurante: Long? = null) {
        viewModelScope.launch {
            repository.getListarComentarios(idRestaurante?.toInt() ?: 0)
                .onSuccess { resenas ->
                    updateSuccess { copy(resenas = resenas) }
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: "No se pudieron cargar las reseñas"
                    _uiEvent.send(MenuUiEvent.ShowSnackbar(errorMsg))
                }
        }
    }


    fun enviarResena(restauranteId: Long, calificacion: Int, texto: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val fechaActual = LocalDateTime.now().format(formatter)
        val nuevoComentario = DTOComentario(
            null,
            texto,
            restauranteId,
            calificacion,   // puntuacion
            fechaActual
        )
        viewModelScope.launch {
            updateSuccess { copy(enviandoResena = true) }
            repository.setComentario(nuevoComentario)
                .onSuccess { comentarioCreado ->
                    updateSuccess {
                        copy(resenas = listOf(comentarioCreado) + resenas, enviandoResena = false)
                    }
                }
                .onFailure { exception ->
                    updateSuccess { copy(enviandoResena = false) }
                    _uiEvent.send(MenuUiEvent.ShowSnackbar(exception.message ?: "Error al enviar"))
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
}

enum class OrdenPrecio { NINGUNO, MENOR, MAYOR }

sealed class MenuUiState {
    object Loading : MenuUiState()
    object SinProductos : MenuUiState()
    data class Error(val message: String) : MenuUiState()
    data class Success(
        val restaurante: DTORestaurante,
        val productosOriginales: List<DTOProducto>,
        val categorias: List<String>,
        val categoriaSeleccionada: String = "Todos",
        val subcategoriaSeleccionada: String = "Todos",
        val ordenPrecio: OrdenPrecio = OrdenPrecio.NINGUNO,
        val showOrdenDialog: Boolean = false,
        val ofertas: List<DTOProducto> = emptyList(),
        val resenas: List<DTOComentario> = emptyList(),
        val enviandoResena: Boolean = false
    ) : MenuUiState() {
        val subcategoriasDisponibles: List<String>
            get() {
                val productosBase = if (categoriaSeleccionada == "Todos") {
                    productosOriginales
                } else {
                    productosOriginales.filter { it.categoria?.name == categoriaSeleccionada }
                }
                return listOf("Todos") + productosBase
                    .mapNotNull { it.subCategoria?.nombre }
                    .distinct()
                    .sorted()
            }
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
                        it.subCategoria?.nombre == subcategoriaSeleccionada
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

sealed interface MenuUiEvent {
    data class ShowSnackbar(val message: String) : MenuUiEvent
}