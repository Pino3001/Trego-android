package com.grupo6.trego.ui.carrito

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.CarritoItem
import com.grupo6.trego.data.model.DireccionDTO
import com.grupo6.trego.data.model.ProductoDTO
import kotlinx.coroutines.launch

sealed class CarritoUiState {
    object Vacio : CarritoUiState()
    object RestauranteCerrado : CarritoUiState()
    object PagoExitoso : CarritoUiState()
    object PagoRechazado : CarritoUiState()
    data class Cargado(val items: List<CarritoItem>) : CarritoUiState()
    data class Error(val mensaje: String) : CarritoUiState()
}

class CarritoViewModel : ViewModel() {

    var uiState by mutableStateOf<CarritoUiState>(CarritoUiState.Vacio)
        private set

    // Lista interna del carrito
    private val _items = mutableStateListOf<CarritoItem>()
    val items: List<CarritoItem> get() = _items

    // Dirección seleccionada
    var direccionSeleccionada by mutableStateOf<DireccionDTO?>(null)
        private set

    // Modal de detalle/edición
    var productoEnModal by mutableStateOf<CarritoItem?>(null)
        private set

    var showModal by mutableStateOf(false)
        private set

    // Nombre del restaurante actual
    var nombreRestaurante by mutableStateOf("")
        private set

    // Total calculado
    val total: Double get() = _items.sumOf { it.subtotal }

    // Direcciones mock — reemplazar con llamada al backend
    val direcciones = listOf(
        DireccionDTO(1, "Casa", "Av. Italia 1234"),
        DireccionDTO(2, "Trabajo", "18 de Julio 890")
    )

    // --- Agregar producto desde MenuScreen ---

    fun abrirModalNuevoProducto(producto: ProductoDTO, restaurante: String) {
        nombreRestaurante = restaurante
        productoEnModal = CarritoItem(producto = producto)
        showModal = true
    }

    fun abrirModalEditar(item: CarritoItem) {
        productoEnModal = item
        showModal = true
    }

    fun cerrarModal() {
        showModal = false
        productoEnModal = null
    }

    fun confirmarModal(item: CarritoItem) {
        val index = _items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            _items[index] = item         // editar existente
        } else {
            _items.add(item)             // agregar nuevo
        }
        actualizarEstado()
        cerrarModal()
    }

    // --- Gestión del carrito ---

    fun cambiarCantidad(itemId: Long, delta: Int) {
        val index = _items.indexOfFirst { it.id == itemId }
        if (index < 0) return
        val nueva = _items[index].cantidad + delta
        if (nueva <= 0) {
            _items.removeAt(index)
        } else {
            _items[index] = _items[index].copy(cantidad = nueva)
        }
        actualizarEstado()
    }

    fun eliminarItem(itemId: Long) {
        _items.removeAll { it.id == itemId }
        actualizarEstado()
    }

    fun seleccionarDireccion(direccion: DireccionDTO) {
        direccionSeleccionada = direccion
    }

    fun usarUbicacionActual() {
        // TODO: obtener coordenadas del dispositivo y geocodificar
        direccionSeleccionada = DireccionDTO(0, "Ubicación actual", "Obteniendo ubicación...")
    }

    // --- Realizar pedido ---

    fun realizarPedido(restauranteAbierto: Boolean) {
        if (!restauranteAbierto) {
            uiState = CarritoUiState.RestauranteCerrado
            _items.clear()
            return
        }

        viewModelScope.launch {
            try {
                // TODO: llamar al backend para crear el pedido
                // val response = RetrofitClient.pedidoService.crearPedido(...)

                // TODO: integrar MercadoPago aquí
                // val checkout = MercadoPagoCheckout.Builder(...)
                //     .build()
                // checkout.startPayment(activity, MP_REQUEST_CODE)

                // Simulación de éxito para pruebas:
                uiState = CarritoUiState.PagoExitoso
                _items.clear()

            } catch (e: Exception) {
                uiState = CarritoUiState.Error(e.message ?: "Error al realizar el pedido")
            }
        }
    }

    fun reiniciar() {
        _items.clear()
        direccionSeleccionada = null
        uiState = CarritoUiState.Vacio
    }

    private fun actualizarEstado() {
        uiState = if (_items.isEmpty()) CarritoUiState.Vacio
        else CarritoUiState.Cargado(_items.toList())
    }
}