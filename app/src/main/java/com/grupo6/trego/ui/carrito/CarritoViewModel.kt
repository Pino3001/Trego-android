package com.grupo6.trego.ui.carrito

import CarritoRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.data.model.DTOProductoSimplificado
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.model.DTODireccion
import kotlinx.coroutines.launch

sealed class CarritoUiState {
    object Cargando : CarritoUiState()               // nueva
    object Vacio : CarritoUiState()
    object RestauranteCerrado : CarritoUiState()
    object PagoExitoso : CarritoUiState()
    object PagoRechazado : CarritoUiState()
    data class Cargado(val items: List<DTOProductoPedido>) : CarritoUiState()

    data class Error(val mensaje: String) : CarritoUiState()
}

class CarritoViewModel(
    private val repository: CarritoRepository = CarritoRepository()
) : ViewModel() {

    var uiState by mutableStateOf<CarritoUiState>(CarritoUiState.Cargando)
        private set

    private val _items = mutableStateListOf<DTOProductoPedido>()
    val items: List<DTOProductoPedido> get() = _items

    var direccionSeleccionada by mutableStateOf<DTODireccion?>(null)
    var productoEnModal by mutableStateOf<DTOProductoPedido?>(null)
    var showModal by mutableStateOf(false)
    var nombreRestaurante by mutableStateOf("")

    var total by mutableStateOf(0.0)
        private set

    private var restauranteId: Long? = null

    val direcciones = listOf(
        DTODireccion(apartamento = 1, esquina = "", calle = "", numero = 2, latitud = 0.0, longitud = 0.0),
        DTODireccion(apartamento = 1, esquina = "", calle = "", numero = 2, latitud = 0.0, longitud = 0.0),
    )

    init {
        cargarCarrito()
    }

    private fun cargarCarrito() {
        viewModelScope.launch {
            uiState = CarritoUiState.Cargando
            repository.obtenerCarrito()
                .onSuccess { carrito ->
                    _items.clear()
                    if (carrito != null) {
                        _items.addAll(carrito.productos ?: emptyList())
                        total = carrito.total ?: 0.0
                        restauranteId = carrito.idRestaurante?.toLong()
                    } else {
                        total = 0.0
                    }
                    actualizarEstado()
                }
                .onFailure { e ->
                    uiState = CarritoUiState.Error(e.message ?: "Error al cargar carrito")
                }
        }
    }

    fun confirmarModal(item: DTOProductoPedido) {
        viewModelScope.launch {
            uiState = CarritoUiState.Cargando
            // Construir request para AGREGAR: necesita producto con idProducto e idRestaurante
            val request = DTOProductoPedido(
                producto = DTOProductoSimplificado(
                    idProducto = item.producto?.idProducto,
                    idRestaurante = item.producto?.idRestaurante ?: restauranteId?.toInt(),
                    precio = null,
                    nombre = null,
                    urlImagen = null,
                    precioOferta = null,
                    ingredientes = null,
                ),
                cantidad = item.cantidad ?: 1,
                ingredientes = item.ingredientes,
                subtotal = item.subtotal,
                observaciones = item.observaciones,
                cantidadDisponible = item.cantidadDisponible
            )
            repository.agregarProducto(request)
                .onSuccess { carrito ->
                    _items.clear()
                    _items.addAll(carrito.productos ?: emptyList())
                    total = carrito.total ?: 0.0
                    actualizarEstado()
                }
                .onFailure { e ->
                    uiState = CarritoUiState.Error(e.message ?: "Error al agregar producto")
                    cargarCarrito()
                }
            cerrarModal()
        }
    }

    fun cambiarCantidad(item: DTOProductoPedido, delta: Int) {
        val nuevaCantidad = (item.cantidad ?: 0) + delta
        if (nuevaCantidad <= 0) {
            eliminarItem(item)
            return
        }
        viewModelScope.launch {
            // Para modificar, enviamos idProducto en la raíz
            val request = DTOProductoPedido(
                producto = DTOProductoSimplificado(
                    idProducto = item.producto?.idProducto,
                    idRestaurante = item.producto?.idRestaurante ?: restauranteId?.toInt(),
                    precio = null,
                    nombre = null,
                    urlImagen = null,
                    precioOferta = null,
                    ingredientes = null,
                ),
                cantidad = item.cantidad ?: 1,
                ingredientes = item.ingredientes,
                subtotal = item.subtotal,
                observaciones = item.observaciones,
                cantidadDisponible = item.cantidadDisponible
            )
            repository.modificarProducto(request)
                .onSuccess { lineaActualizada ->
                    if (lineaActualizada != null) {
                        // actualizamos ese item en la lista local
                        val index =
                            _items.indexOfFirst { it.producto?.idProducto == lineaActualizada.producto?.idProducto }
                        if (index >= 0) {
                            _items[index] = lineaActualizada
                        } else {
                            // si no estaba, simplemente recargamos todo
                            cargarCarrito()
                        }
                    } else {
                        // si devolvió null es porque se eliminó (cantidad 0)
                        _items.removeAll { it.producto?.idProducto == item.producto?.idProducto }
                    }
                    actualizarEstado()
                }
                .onFailure {
                    uiState = CarritoUiState.Error(it.message ?: "Error al actualizar cantidad")
                }
        }
    }

    fun eliminarItem(item: DTOProductoPedido) {
        viewModelScope.launch {
            val request = DTOProductoPedido(
                producto = DTOProductoSimplificado(
                    idProducto = item.producto?.idProducto,
                    idRestaurante = item.producto?.idRestaurante ?: restauranteId?.toInt(),
                    precio = null,
                    nombre = null,
                    urlImagen = null,
                    precioOferta = null,
                    ingredientes = null,
                ),
                cantidad = item.cantidad ?: 1,
                ingredientes = item.ingredientes,
                subtotal = item.subtotal,
                observaciones = item.observaciones,
                cantidadDisponible = item.cantidadDisponible
            )
            repository.eliminarProducto(request)
                .onSuccess { carrito ->
                    if (carrito != null) {
                        _items.clear()
                        _items.addAll(carrito.productos ?: emptyList())
                        total = carrito.total ?: 0.0
                    } else {
                        _items.removeAll { it.producto?.idProducto == item.producto?.idProducto }
                    }
                    actualizarEstado()
                }
                .onFailure {
                    uiState = CarritoUiState.Error(it.message ?: "Error al eliminar producto")
                }
        }
    }

    fun limpiarCarrito() {
        viewModelScope.launch {
            repository.limpiarCarrito()
                .onSuccess {
                    _items.clear()
                    total = 0.0
                    uiState = CarritoUiState.Vacio
                }
                .onFailure {
                    uiState = CarritoUiState.Error(it.message ?: "Error al limpiar carrito")
                }
        }
    }

    // ─── MODAL ───
    fun abrirModalNuevoProducto(
        productoSimplificado: DTOProductoSimplificado,
        productoPedido: DTOProductoPedido,
        restaurante: DTORestaurante
    ) {
        nombreRestaurante = restaurante.nombre.toString()
        productoEnModal = DTOProductoPedido(
            producto = productoSimplificado,
            cantidad = 1,
            observaciones = productoPedido.observaciones,
            ingredientes = productoPedido.ingredientes,
            cantidadDisponible = productoPedido.cantidadDisponible,
            subtotal = productoPedido.subtotal
        )
        showModal = true
    }

    fun abrirModalEditar(item: DTOProductoPedido) {
        productoEnModal = item
        showModal = true
    }

    fun cerrarModal() {
        showModal = false
        productoEnModal = null
    }

    // ─── DIRECCIÓN ───
    fun seleccionarDireccion(direccion: DTODireccion) {
        direccionSeleccionada = direccion
    }

    fun usarUbicacionActual() {
        direccionSeleccionada =
            DTODireccion(apartamento = 1, esquina = "", calle = "", numero = 2, latitud = 0.0, longitud = 0.0)
    }

    fun realizarPedido(restauranteAbierto: Boolean) {
        if (!restauranteAbierto) {
            uiState = CarritoUiState.RestauranteCerrado
            return
        }
        viewModelScope.launch {
            try {
                uiState = CarritoUiState.PagoExitoso
                repository.limpiarCarrito()
                _items.clear()
                total = 0.0
            } catch (e: Exception) {
                uiState = CarritoUiState.Error(e.message ?: "Error al realizar pedido")
            }
        }
    }

    fun reiniciar() {
        _items.clear()
        direccionSeleccionada = null
        total = 0.0
        uiState = CarritoUiState.Cargando
        cargarCarrito()
    }

    private fun actualizarEstado() {
        uiState = if (_items.isEmpty()) {
            CarritoUiState.Vacio
        } else {
            CarritoUiState.Cargado(_items.toList())
        }
    }
}