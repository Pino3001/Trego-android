package com.grupo6.trego.ui.pedidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOCrearReclamoRequest
import com.grupo6.trego.data.model.DTOPedido
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.data.notificaciones.PushNotificationManager
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

sealed class PedidoUiState {
    object Idle : PedidoUiState() // Estado inicial para cuando el modal está cerrado
    object Loading : PedidoUiState()

    data class Success(val activos: List<PedidoUiModel>) : PedidoUiState()
    data class Historial(val historial: List<PedidoUiModel>) : PedidoUiState()
    data class Error(val message: String) : PedidoUiState()
}

/**
 * Este ViewModel es el encargado de gestionar todo el ciclo de vida de los pedidos. 
 * Controla tanto los pedidos que están en curso como el historial de compras pasadas, 
 * permitiendo filtrar por fecha, estado o nombre del restaurante, y también 
 * gestionar cancelaciones o reclamos.
 */
class PedidoViewModel(
    private val repository: PedidoRepository,
    private val repositoryRestaurante: RestauranteRepository,
    private val pushManager: PushNotificationManager
) : ViewModel() {

    private val _activosState = MutableStateFlow<PedidoUiState>(PedidoUiState.Idle)
    val activosState: StateFlow<PedidoUiState> = _activosState.asStateFlow()

    private val _historialState = MutableStateFlow<PedidoUiState>(PedidoUiState.Idle)
    val historialState: StateFlow<PedidoUiState> = _historialState.asStateFlow()

    private val _eventChannel = Channel<String>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private var previousActivosState: PedidoUiState = PedidoUiState.Idle

    // Lock para evitar múltiples cargas simultáneas
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ---  ESTADOS PARA FILTROS Y BÚSQUEDA ---

    // Lista original en memoria para no volver a llamar a la API al filtrar
    private var historialOriginal: List<PedidoUiModel> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedEstado = MutableStateFlow<EnumEstadoPedido?>(null)
    val selectedEstado = _selectedEstado.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _ordenMasRecientes = MutableStateFlow(true)
    val ordenMasRecientes = _ordenMasRecientes.asStateFlow()

    fun dismissActivosError() {
        _activosState.value = previousActivosState
    }

    /* Trae los pedidos activos del usuario y busca la información de cada restaurante para mostrarla completa. */
    fun cargarPedidos(silencioso: Boolean = false) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            actualizarPedidosInternal(silencioso)
        }
    }

    private suspend fun actualizarPedidosInternal(silencioso: Boolean) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true

        try {
            if (!silencioso) {
                _activosState.value = PedidoUiState.Loading
            }

            repository.obtenerPedidosCliente()
                .onSuccess { todosLosPedidos ->
                    val idsRestaurantes =
                        todosLosPedidos.mapNotNull { it.idRestaurante?.toLong() }.distinct()

                    val restaurantesMap = coroutineScope {
                        idsRestaurantes.map { id ->
                            async {
                                val resultado = repositoryRestaurante.getRestauranteDatos(id)
                                id to resultado.getOrNull()
                            }
                        }.awaitAll().toMap()
                    }

                    // 🚨 LOGS DE DEBUGGING:
                    Log.d("TREGO_DEBUG", "--- NUEVA CARGA DE PEDIDOS ---")
                    todosLosPedidos.forEach { pedido ->
                        Log.d(
                            "TREGO_DEBUG",
                            "Pedido ID: ${pedido.idPedido} | ID Restaurante que viene del Backend: ${pedido.idRestaurante}"
                        )
                    }
                    restaurantesMap.forEach { (id, restaurante) ->
                        Log.d(
                            "TREGO_DEBUG",
                            "Mapa de la app -> ID Consultado: $id asignado a Nombre: ${restaurante?.nombre}"
                        )
                    }

                    val pedidosConRestaurante = todosLosPedidos.map { pedido ->
                        val restaurante = restaurantesMap[pedido.idRestaurante?.toLong()]
                        PedidoUiModel(
                            pedido = pedido,
                            nombreRestaurante = restaurante?.nombre ?: "Restaurante Desconocido",
                            telefonoRestaurante = restaurante?.telefono ?: "Sin teléfono"
                        )
                    }.sortedByDescending { it.pedido.fechaCreacion }

                    _activosState.value = PedidoUiState.Success(pedidosConRestaurante)

                }
                .onFailure { error ->
                    if (!silencioso) {
                        _activosState.value =
                            PedidoUiState.Error(error.message ?: "Error desconocido")
                    }
                }
        } finally {
            _isRefreshing.value = false
        }
    }

    /* Función para refrescar la lista de pedidos apenas recibimos el aviso de que un pago se procesó bien. */
    suspend fun esperarNuevoPedido(timeOutMillis: Long = 15000L): Boolean {

        return withTimeoutOrNull(timeOutMillis.milliseconds) {
            //  Esperamos el evento de push (si llegó mientras estábamos en la web, el 'replay' lo entrega al instante)
            pushManager.pushEvents.first { it["estado"] == "PAGO_PROCESADO" }

            // Limpiamos la memoria para que no interfiera en futuras compras
            pushManager.limpiarEventos()

            actualizarPedidosInternal(silencioso = true)

            true
        } ?: run {
            // Si a los 15 segundos no llegó nada (se cayó el internet, etc.), fallamos
            false
        }
    }

    /* Recupera todos los pedidos finalizados y los guarda en memoria para que el usuario pueda filtrarlos rápido. */
    fun cargarHistorial() {
        viewModelScope.launch {
            _historialState.value = PedidoUiState.Loading

            repository.obtenerPedidosHistorial()
                .onSuccess { todosLosPedidos ->
                    val idsRestaurantes =
                        todosLosPedidos.mapNotNull { it.idRestaurante?.toLong() }.distinct()

                    val restaurantesMap = coroutineScope {
                        idsRestaurantes.map { id ->
                            async {
                                val resultado = repositoryRestaurante.getRestauranteDatos(id)
                                resultado.onFailure { error ->
                                    Log.e(
                                        "PedidoViewModel",
                                        "Error al cargar restaurante $id: ${error.message}"
                                    )
                                }
                                id to resultado.getOrNull()
                            }
                        }.awaitAll().toMap()
                    }

                    val pedidosConRestaurante = todosLosPedidos.map { pedido ->
                        val restaurante = restaurantesMap[pedido.idRestaurante?.toLong()]
                        PedidoUiModel(
                            pedido = pedido,
                            nombreRestaurante = restaurante?.nombre ?: "Restaurante Desconocido",
                            telefonoRestaurante = restaurante?.telefono ?: "Sin teléfono"
                        )
                    }.sortedByDescending { it.pedido.fechaCreacion }

                    // Filtramos solo los estados base del historial y los guardamos en memoria
                    historialOriginal = pedidosConRestaurante.filter {
                        it.pedido.estado in listOf(
                            EnumEstadoPedido.Cancelado,
                            EnumEstadoPedido.Entregado,
                            EnumEstadoPedido.Reembolsado
                        )
                    }

                    // En lugar de emitir directamente, pasamos por la función de filtrado
                    aplicarFiltros()
                }
                .onFailure {
                    _historialState.value = PedidoUiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    /* Le pide al servidor cancelar un pedido activo, siempre y cuando todavía esté en un estado permitido. */
    fun cancelarPedido(pedido: DTOPedido) {
        viewModelScope.launch {
            previousActivosState = _activosState.value
            _activosState.value = PedidoUiState.Loading

            val copia = DTOPedido(
                idPedido = pedido.idPedido
            )
            repository.cancelarPedido(copia)
                .onSuccess {
                    _eventChannel.send("Pedido cancelado con éxito")
                    cargarPedidos()
                }
                .onFailure { error ->
                    _activosState.value =
                        PedidoUiState.Error("No se pudo cancelar: ${error.message}")
                }
        }
    }

    // --- FUNCIONES DE ACTUALIZACIÓN DE FILTROS ---

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        aplicarFiltros()
    }

    fun toggleEstadoFiltro(estado: EnumEstadoPedido) {
        // Si vuelve a tocar el mismo que ya está seleccionado, lo limpia (pone null), si no, asigna el nuevo
        _selectedEstado.value = if (_selectedEstado.value == estado) null else estado
        aplicarFiltros()
    }

    fun onDateChange(date: LocalDate?) {
        _selectedDate.value = date
        aplicarFiltros()
    }

    fun clearFiltros() {
        _searchQuery.value = ""
        _selectedEstado.value = null
        _selectedDate.value = null
        aplicarFiltros()
    }

    fun toggleOrden() {
        _ordenMasRecientes.value = !_ordenMasRecientes.value
        aplicarFiltros()
    }

    // Lógica central donde convergen la búsqueda y todos los filtros
    /* Procesa los filtros de búsqueda, estado y fecha sobre la lista de historial que tenemos cargada. */
    private fun aplicarFiltros() {
        val query = _searchQuery.value.trim()
        val estadoFiltro = _selectedEstado.value
        val fechaFiltro = _selectedDate.value
        val masRecientesPrimero = _ordenMasRecientes.value

        // Primero filtramos la lista original con los criterios existentes
        val listaFiltrada = historialOriginal.filter { item ->
            val coincideBusqueda = if (query.isEmpty()) true else {
                item.nombreRestaurante.contains(query, ignoreCase = true) ||
                        item.pedido.idPedido?.toString()?.contains(query) == true
            }

            val coincideEstado = estadoFiltro == null || item.pedido.estado == estadoFiltro

            val coincideFecha = if (fechaFiltro != null) {
                item.pedido.fechaCreacion?.toLocalDate() == fechaFiltro
            } else {
                true
            }

            coincideBusqueda && coincideEstado && coincideFecha
        }

        // 🚀 NUEVO: Aplicamos el ordenamiento según la preferencia del usuario
        val listaFinal = if (masRecientesPrimero) {
            listaFiltrada.sortedByDescending { it.pedido.fechaCreacion } // Últimos realizados primero
        } else {
            listaFiltrada.sortedBy { it.pedido.fechaCreacion } // Primeros realizados primero
        }

        _historialState.value = PedidoUiState.Historial(listaFinal)
    }

    /* Envía un reclamo formal al restaurante por un pedido específico y actualiza la UI para mostrar que ya se reclamó. */
    fun crearReclamo(reclamo: DTOCrearReclamoRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.realizarReclamo(reclamo)
                .onSuccess {
                    _eventChannel.send("Reclamo enviado con éxito")
                    val estadoActual = _activosState.value
                    if (estadoActual is PedidoUiState.Success) {
                        val listaActualizada = estadoActual.activos.map { uiModel ->
                            if (uiModel.pedido.idPedido == reclamo.idPedido) {
                                // Modificamos el tieneReclamo en true usando .copy()
                                uiModel.copy(pedido = uiModel.pedido.copy(tieneReclamo = true))
                            } else {
                                uiModel
                            }
                        }
                        _activosState.value = PedidoUiState.Success(listaActualizada)
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    val mensaje = error.message ?: "Error desconocido"
                    _eventChannel.send(mensaje)
                }
        }
    }
}

