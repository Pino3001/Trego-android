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
import com.grupo6.trego.data.model.DTOIngrediente
import com.grupo6.trego.data.model.DTOPreferenciaMP
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.data.model.DTOProductoSimplificado
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.notificaciones.PushNotificationManager
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.UsuarioRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

sealed class CarritoUiState {
    object Cargando : CarritoUiState()               // nueva
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
    }

    fun recargarCarrito() {
        cargarCarrito()
    }

    private fun emitirError(mensaje: String) {
        viewModelScope.launch { _errorEvent.send(mensaje) }
    }

    // Carga el carrito que tiene el backend en el viewModel
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

    // Confirma los datos que se ingresaron en el modal de detalle donde se ingresa la cantidad, observaciones etc..
    fun confirmarModal(item: DTOProductoPedido) {
        viewModelScope.launch {
            uiState = CarritoUiState.Cargando
            // ¿El producto ya existe en el carrito?
            val existe = _items.any { it.producto?.idProducto == item.producto?.idProducto }

            if (existe) {
                // EDITAR → modificar, no agregar
                repository.modificarProducto(item)
                    .onSuccess { lineaActualizada ->
                        if (lineaActualizada != null) {
                            val index = _items.indexOfFirst {
                                it.producto?.idProducto == lineaActualizada.producto?.idProducto
                            }
                            if (index >= 0) _items[index] = lineaActualizada
                        } else {
                            _items.removeAll { it.producto?.idProducto == item.producto?.idProducto }
                        }
                        actualizarEstado()
                    }
                    .onFailure { e ->
                        emitirError("Error al modificar producto")
                        cargarCarrito() // refrescar por si hay inconsistencia
                    }
            } else {
                // NUEVO → agregar
                repository.agregarProducto(item)
                    .onSuccess { carrito ->
                        _items.clear()
                        _items.addAll(carrito.productos ?: emptyList())
                        total = carrito.total ?: 0.0
                        actualizarEstado()
                    }
                    .onFailure { e ->
                        emitirError("Error al agregar producto")
                        cargarCarrito()
                    }
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
            val precioUnitario: Float = (item.producto?.precio) ?: 0f
            val nuevoSubtotal = precioUnitario * nuevaCantidad
            // Para modificar, enviamos idProducto en la raíz
            val request = DTOProductoPedido(
                producto = item.producto,
                cantidad = nuevaCantidad,
                ingredientes = item.ingredientes,
                subtotal = nuevoSubtotal,
                observaciones = item.observaciones,
                cantidadDisponible = item.cantidadDisponible
            )
            Log.e("Request", request.toString())
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
                    emitirError("Error al actualizar cantidad")
                }
        }
    }

    fun eliminarItem(item: DTOProductoPedido) {
        viewModelScope.launch {
            val request = DTOProductoPedido(
                producto = item.producto,
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
        productoSimplificado: DTOProductoSimplificado,
        restaurante: DTORestaurante
    ) {
        esEdicion = false
        nombreRestaurante = restaurante.nombre.toString()
        productoEnModal = DTOProductoPedido(
            producto = productoSimplificado,
            cantidad = 1,
            observaciones = null,
            ingredientes = emptyList(),  // o null según tu modelo
            cantidadDisponible = 1,// aca debe de ir la cantidad disponible
            subtotal = productoSimplificado.precio as Float?
        )
        showModal = true
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

    fun confirmarPedido() {
        // Validaciones previas
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
                    //  disparamos la orden de abrir la web
                    uiState = CarritoUiState.AbrirMercadoPago(
                        url = preferencia.initPoint ?: "", // o initPoint en producción
                    )
                }
                .onFailure { e ->
                    emitirError("Error al confirmar pedido")
                }
        }
    }

    // ─── NUEVA FUNCIÓN EN EL VIEWMODEL ───
    fun onPreferenciaLanzada() {
        val preferencia = preferenciaActual
        if (preferencia != null) {
            // Pasamos al estado pendiente usando la que teníamos guardada
            uiState = CarritoUiState.PagoPendiente(preferencia)
        } else {
            // Respaldo por si algo raro pasa, recargamos el carrito
            cargarCarrito()
        }
    }

    // ═══ FUNCIONES PARA EL DEEP LINK (ACTUALIZACIÓN OPTIMISTA) ═══

    fun marcarPagoExitoso() {
        // 1. Vaciamos el carrito localmente de forma optimista
        _items.clear()
        total = 0.0
        carritoActual = null

        // 2. Cambiamos el estado para que la app sepa que fue un éxito
        uiState = CarritoUiState.PagoExitoso
    }

    fun marcarPagoRechazado() {
        // Solo cambiamos el estado, los items siguen en el carrito
        // para que el usuario pueda intentar pagar de nuevo.
        uiState = CarritoUiState.PagoRechazado
    }

    fun marcarPagoPendiente() {
        // Usamos la preferencia que ya habías guardado en confirmarPedido()
        val preferencia = preferenciaActual
        if (preferencia != null) {
            uiState = CarritoUiState.PagoPendiente(preferencia)
        } else {
            // Si por alguna razón no está en memoria, volvemos a mostrar el carrito normal
            actualizarEstado()
        }
    }

}