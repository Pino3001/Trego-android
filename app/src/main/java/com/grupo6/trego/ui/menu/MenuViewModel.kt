package com.grupo6.trego.ui.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOComentario
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Este ViewModel se encarga de gestionar toda la carta de un restaurante. 
 * Trae los platos, las ofertas y las opiniones de los usuarios, además de 
 * permitir filtrar la comida por categoría o precio y mandar reseñas nuevas.
 */
class MenuViewModel(
    private val repository: RestauranteRepository
) : ViewModel() {

    private var productoASeleccionar: DTOProducto? = null

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MenuUiEvent>(extraBufferCapacity = 64)
    val uiEvent: SharedFlow<MenuUiEvent> = _uiEvent.asSharedFlow()

    private inline fun updateSuccess(crossinline update: MenuUiState.Success.() -> MenuUiState.Success) {
        _uiState.update { currentState ->
            if (currentState is MenuUiState.Success) {
                currentState.update()
            } else {
                currentState
            }
        }
    }

    /* Le pedimos al servidor todo el menú del restaurante y separamos los platos normales de las ofertas. */
    fun loadMenu(restaurantId: Long) {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading

            repository.getRestaurantMenu(restaurantId)
                .onSuccess { restaurante ->
                    val productos = restaurante.productos ?: emptyList()
                    val ofertas = productos.filter {
                        it.oferta != null && it.ofertaActiva == true
                    }

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
                        Log.e("Load", restaurante.productos.toString())
                        loadResenas(restaurantId)

                        productoASeleccionar?.let { prod ->
                            if (prod.idRestaurante?.toLong() == restaurantId) {
                                viewModelScope.launch {
                                    _uiEvent.emit(MenuUiEvent.AbrirModalProducto(prod))
                                }
                                productoASeleccionar = null
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.value = MenuUiState.Error(
                        e.message ?: "Error al cargar el menú"
                    )
                }
        }
    }

    /* Traemos los comentarios que otros usuarios dejaron sobre este lugar para mostrarlos en la pestaña de opiniones. */
    fun loadResenas(idRestaurante: Long? = null) {
        viewModelScope.launch {
            repository.getListarComentarios(idRestaurante?.toInt() ?: 0)
                .onSuccess { resenas ->
                    updateSuccess { copy(resenas = resenas) }
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: "No se pudieron cargar las reseñas"
                    _uiEvent.tryEmit(MenuUiEvent.ShowSnackbar(errorMsg))
                }
        }
    }


    /* Enviamos una nueva opinión al backend, asegurándonos primero de que el usuario no haya comentado antes. */
    fun enviarResena(restauranteId: Long, calificacion: Int, texto: String) {
        updateSuccess { copy(enviandoResena = true) }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val fechaActual = LocalDateTime.now().format(formatter)
        val nuevoComentario = DTOComentario(
            null,
            texto,
            restauranteId,
            calificacion,
            fechaActual
        )

        viewModelScope.launch {
            repository.yaComentoUsuario(restauranteId)
                .onSuccess { comento ->
                    if (!comento) {
                        repository.setComentario(nuevoComentario)
                            .onSuccess { comentarioCreado ->
                                updateSuccess {
                                    copy(
                                        resenas = listOf(comentarioCreado) + resenas,
                                        enviandoResena = false
                                    )
                                }
                                _uiEvent.tryEmit(
                                    MenuUiEvent.ShowSnackbar(
                                        "Reseña enviada correctamente"
                                    )
                                )
                                actualizarPromedio(restauranteId)
                            }
                            .onFailure { exception ->
                                updateSuccess { copy(enviandoResena = false) }
                                _uiEvent.tryEmit(
                                    MenuUiEvent.ShowSnackbar(
                                        exception.message ?: "Error al enviar la reseña"
                                    )
                                )
                            }
                    } else {
                        updateSuccess { copy(enviandoResena = false) }
                        _uiEvent.tryEmit(
                            MenuUiEvent.ShowSnackbar("Ya has comentado en este restaurante")
                        )
                    }
                }
                .onFailure { exception ->
                    updateSuccess { copy(enviandoResena = false) }
                    _uiEvent.tryEmit(
                        MenuUiEvent.ShowSnackbar(
                            exception.message ?: "Error al comprobar el usuario"
                        )
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

    /* Si el usuario escribió una reseña, refrescamos el puntaje total del restaurante para que se vea el cambio al toque. */
    fun actualizarPromedio(restauranteId: Long) {
        viewModelScope.launch {
            repository.getCalificacionPromedio(restauranteId.toInt())
                .onSuccess { nuevoPromedio ->
                    updateSuccess {
                        copy(restaurante = restaurante.copy(calificacionProm = nuevoPromedio))
                    }
                }
                .onFailure { exception ->
                    _uiEvent.tryEmit(
                        MenuUiEvent.ShowSnackbar(
                            exception.message ?: "Error al actualizar el promedio"
                        )
                    )
                }
        }
    }

    fun abrirMenuConProducto(producto: DTOProducto) {
        productoASeleccionar = producto
        _uiEvent.tryEmit(MenuUiEvent.NavigateToMenu(producto.idRestaurante?.toLong() ?: 0L))
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
    data class NavigateToMenu(val restauranteId: Long) : MenuUiEvent
    data class AbrirModalProducto(val producto: DTOProducto) : MenuUiEvent
}