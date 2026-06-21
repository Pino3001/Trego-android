package com.grupo6.trego.ui.carrito

import CarritoRepository
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOCarrito
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOPreferenciaMP
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.repository.LocationRepository
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.UsuarioRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class CarritoUiState {
    object Cargando : CarritoUiState()
    object Vacio : CarritoUiState()
    object RestauranteCerrado : CarritoUiState()
    object PagoExitoso : CarritoUiState()
    object PagoRechazado : CarritoUiState()
    data class Cargado(val items: List<DTOProductoPedido>) : CarritoUiState()
    data class PagoPendiente(val preferencia: DTOPreferenciaMP) : CarritoUiState()
    data class AbrirMercadoPago(val url: String) : CarritoUiState()
}

sealed class DireccionesState {
    object Cargando : DireccionesState()
    data class Cargadas(val items: List<DTODireccion>) : DireccionesState()
    data class Error(val message: String) : DireccionesState()
}

class CarritoViewModel(
    private val repository: CarritoRepository,
    private val pedidoRepository: PedidoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val locationRepository: LocationRepository,
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
    val currentRestauranteId: Long? get() = restauranteId

    var esEdicion by mutableStateOf(false)
        private set

    private val _errorEvent = Channel<String>(Channel.BUFFERED)
    val errorEvent = _errorEvent.receiveAsFlow()
    private var carritoActual: DTOCarrito? = null
    private var preferenciaActual: DTOPreferenciaMP? = null

    private val _direccionesState = MutableStateFlow<DireccionesState>(DireccionesState.Cargando)
    val direccionesState: StateFlow<DireccionesState> = _direccionesState.asStateFlow()

    init {
        cargarCarrito()
        observarUbicacion()
    }

    private fun observarUbicacion() {
        viewModelScope.launch {
            locationRepository.currentAddress.collect { direccion ->
                if (direccionSeleccionada == null && direccion != null) {
                    direccionSeleccionada = direccion
                }
            }
        }
    }

    fun recargarCarrito() {
        cargarCarrito()
    }

    private fun emitirError(mensaje: String) {
        viewModelScope.launch { _errorEvent.send(mensaje) }
    }

    private fun cargarCarrito() {
        viewModelScope.launch {
            uiState = CarritoUiState.Cargando
            repository.obtenerCarrito()
                .onSuccess { carrito ->
                    carritoActual = carrito
                    _items.clear()
                    if (carrito != null) {
                        _items.addAll(carrito.productos ?: emptyList())
                        total = carrito.total ?: 0.0
                        restauranteId = carrito.idRestaurante
                    } else {
                        total = 0.0
                    }
                    actualizarEstado()
                }
                .onFailure { e ->
                    emitirError("Error al cargar carrito")
                }
        }
    }

    fun cargarDirecciones() {
        viewModelScope.launch {
            _direccionesState.value = DireccionesState.Cargando
            val result = usuarioRepository.obtenerDirecciones()
            if (result.isSuccess) {
                val direcciones = result.getOrNull() ?: emptyList()
                _direccionesState.value = DireccionesState.Cargadas(direcciones)
            } else {
                val error = result.exceptionOrNull()
                _direccionesState.value = DireccionesState.Error(error?.message ?: "Error desconocido")
            }
        }
    }

    // Compara si dos items son la misma combinación exacta, sin importar su idLinea
    private fun sonMismaCombinacion(item1: DTOProductoPedido, item2: DTOProductoPedido): Boolean {
        val mismoId = item1.producto?.idProducto == item2.producto?.idProducto
        val mismosIngredientes = item1.ingredientesAQuitar?.toSet() == item2.ingredientesAQuitar?.toSet()
        return mismoId && mismosIngredientes
    }

    fun confirmarModal(itemModificado: DTOProductoPedido) {
        viewModelScope.launch {
            uiState = CarritoUiState.Cargando

            if (esEdicion && itemModificado.idLinea != null) {
                repository.modificarProducto(itemModificado)
                    .onSuccess { lineaActualizada ->
                        if (lineaActualizada != null) {
                            // 1. Si el backend fusionó esta línea con la que ya existía,
                            // el idLinea que nos devuelve será diferente al que enviamos.
                            if (lineaActualizada.idLinea != itemModificado.idLinea) {
                                _items.removeAll { it.idLinea == itemModificado.idLinea }
                            }

                            // 2. LIMPIEZA EXTRA: Por si acaso, borramos cualquier OTRA línea en pantalla
                            // que comparta la misma combinación exacta pero con distinto ID.
                            _items.removeAll {
                                it.idLinea != lineaActualizada.idLinea && sonMismaCombinacion(it, lineaActualizada)
                            }

                            // 3. Actualizamos la línea unificada con los datos correctos
                            val index = _items.indexOfFirst { it.idLinea == lineaActualizada.idLinea }
                            if (index >= 0) {
                                _items[index] = lineaActualizada
                            } else {
                                _items.add(lineaActualizada) // Por si es totalmente nueva
                            }

                            actualizarTotal()
                            actualizarEstado()
                        } else {
                            cargarCarrito() // Respaldo seguro
                        }
                    }
                    .onFailure {
                        emitirError("Error al modificar producto")
                        cargarCarrito()
                    }
            } else {
                agregarOModificarItem(itemModificado)
            }
            cerrarModal()
        }
    }

    private suspend fun agregarOModificarItem(itemNuevo: DTOProductoPedido) {
        // Buscamos si ya existe una línea con exactamente la misma configuración
        val itemExistente = _items.firstOrNull { sonMismaCombinacion(it, itemNuevo) }

        if (itemExistente != null) {
            // Ya existe esta combinación. Le sumamos cantidad a esa línea usando SU idLinea.
            val nuevaCantidad = (itemExistente.cantidad ?: 0) + (itemNuevo.cantidad ?: 1)
            val precioUnitario = (itemExistente.producto?.calcularPrecioConDescuento()?.toFloat()) ?: 0f

            // Gracias al método .copy() de las data classes, esto es súper limpio
            val requestModificacion = itemExistente.copy(
                cantidad = nuevaCantidad,
                subtotal = precioUnitario * nuevaCantidad,
                observaciones = itemNuevo.observaciones ?: itemExistente.observaciones // Mantiene observaciones
            )

            repository.modificarProducto(requestModificacion)
                .onSuccess { lineaActualizada ->
                    if (lineaActualizada != null) {
                        val index = _items.indexOfFirst { it.idLinea == lineaActualizada.idLinea }
                        if (index >= 0) _items[index] = lineaActualizada
                        actualizarTotal()
                    } else {
                        cargarCarrito()
                    }
                    actualizarEstado()
                }
                .onFailure {
                    emitirError("Error al modificar producto")
                    cargarCarrito()
                }
        } else {
            // No existe la combinación en el carrito, agregamos línea nueva
            repository.agregarProducto(itemNuevo)
                .onSuccess { carrito ->
                    _items.clear()
                    _items.addAll(carrito.productos ?: emptyList())
                    total = carrito.total ?: 0.0
                    actualizarEstado()
                }
                .onFailure {
                    emitirError("Error al agregar producto")
                    cargarCarrito()
                }
        }
    }

    fun cambiarCantidad(item: DTOProductoPedido, delta: Int) {
        val nuevaCantidad = (item.cantidad ?: 0) + delta
        if (nuevaCantidad <= 0) {
            eliminarItem(item)
            return
        }
        viewModelScope.launch {
            val precioUnitario = (item.producto?.calcularPrecioConDescuento()?.toFloat()) ?: 0f

            // Usamos .copy() para crear la request asegurando que el idLinea viaje intacto
            val request = item.copy(
                cantidad = nuevaCantidad,
                subtotal = precioUnitario * nuevaCantidad
            )

            repository.modificarProducto(request)
                .onSuccess { lineaActualizada ->
                    if (lineaActualizada != null) {
                        val index = _items.indexOfFirst { it.idLinea == lineaActualizada.idLinea }
                        if (index >= 0) {
                            _items[index] = lineaActualizada
                            actualizarTotal()
                        } else {
                            cargarCarrito()
                        }
                    } else {
                        // Si nos devuelven null, lo borramos de la lista guiándonos por el idLinea
                        _items.removeAll { it.idLinea == request.idLinea }
                    }
                    actualizarEstado()
                }
                .onFailure {
                    emitirError("Error al actualizar cantidad")
                }
        }
    }

    fun eliminarItem(item: DTOProductoPedido) {
        viewModelScope.launch {
            // Ya no hace falta armar el objeto a mano, le mandamos el item entero que trae su idLinea
            repository.eliminarProducto(item)
                .onSuccess { carrito ->
                    if (carrito != null) {
                        _items.clear()
                        _items.addAll(carrito.productos ?: emptyList())
                        total = carrito.total ?: 0.0
                    } else {
                        // Borramos basándonos en el ID exacto de la línea
                        _items.removeAll { it.idLinea == item.idLinea }
                    }
                    actualizarEstado()
                }
                .onFailure {
                    emitirError("Error al eliminar producto")
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
                    emitirError("Error al limpiar carrito")
                }
        }
    }

    // ─── MODAL ───
    fun abrirModalNuevoProducto(
        producto: DTOProducto,
        restaurante: DTORestaurante,
        abierto: Boolean
    ) {
        if (abierto) {
            esEdicion = false
            nombreRestaurante = restaurante.nombre.toString()
            productoEnModal = DTOProductoPedido(
                idLinea = null, // Explícitamente Nulo para que el sistema sepa que es nuevo
                producto = producto,
                cantidad = 1,
                observaciones = null,
                ingredientesAQuitar = emptyList(),
                cantidadDisponible = producto.cantidadDisponible ?: 0,
                subtotal = producto.calcularPrecioConDescuento().toFloat()
            )
            showModal = true
        } else {
            Log.e("Boton", "se Preciono")
            emitirError("El restaurante se encuentra cerrado")
        }
    }

    fun abrirModalEditar(item: DTOProductoPedido) {
        productoEnModal = item
        showModal = true
        esEdicion = true
    }

    fun cerrarModal() {
        showModal = false
        productoEnModal = null
    }

    // ─── DIRECCIÓN ───
    fun seleccionarDireccion(direccion: DTODireccion) {
        direccionSeleccionada = direccion
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

    private fun actualizarTotal() {
        total = _items.sumOf { (it.subtotal ?: 0f).toDouble() }
    }

    fun confirmarPedido() {
        if (_items.isEmpty()) {
            emitirError("El carrito está vacío")
            return
        }
        val direccion = direccionSeleccionada
        if (direccion == null) {
            emitirError("Debe seleccionar una dirección de entrega")
            return
        }
        val idRest = restauranteId
        if (idRest == null) {
            emitirError("Restaurante no identificado")
            return
        }

        viewModelScope.launch {
            uiState = CarritoUiState.Cargando
            pedidoRepository.confirmarPedido(direccion)
                .onSuccess { preferencia ->
                    preferenciaActual = preferencia
                    uiState = CarritoUiState.AbrirMercadoPago(
                        url = preferencia.initPoint ?: "",
                    )
                }
                .onFailure { e ->
                    emitirError("Error al confirmar pedido")
                }
        }
    }

    fun onPreferenciaLanzada() {
        val preferencia = preferenciaActual
        if (preferencia != null) {
            uiState = CarritoUiState.PagoPendiente(preferencia)
        } else {
            cargarCarrito()
        }
    }

    fun marcarPagoExitoso() {
        _items.clear()
        total = 0.0
        carritoActual = null
        uiState = CarritoUiState.PagoExitoso
    }

    fun marcarPagoRechazado() {
        uiState = CarritoUiState.PagoRechazado
    }

    fun marcarPagoPendiente() {
        val preferencia = preferenciaActual
        if (preferencia != null) {
            uiState = CarritoUiState.PagoPendiente(preferencia)
        } else {
            actualizarEstado()
        }
    }
}