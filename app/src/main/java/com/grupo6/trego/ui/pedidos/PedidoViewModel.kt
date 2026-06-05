package com.grupo6.trego.ui.pedidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOPedido
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class PedidoUiState {
    object Idle : PedidoUiState() // Estado inicial para cuando el modal está cerrado
    object Loading : PedidoUiState()

    data class Success(val activos: List<PedidoUiModel>) : PedidoUiState()
    data class Historial(val historial: List<PedidoUiModel>) : PedidoUiState()
    data class Error(val message: String) : PedidoUiState()
}

class PedidoViewModel(
    private val repository: PedidoRepository,
    private val repositoryRestaurante: RestauranteRepository
) : ViewModel() {

    private val _activosState = MutableStateFlow<PedidoUiState>(PedidoUiState.Idle)
    val activosState: StateFlow<PedidoUiState> = _activosState.asStateFlow()

    private val _historialState = MutableStateFlow<PedidoUiState>(PedidoUiState.Idle)
    val historialState: StateFlow<PedidoUiState> = _historialState.asStateFlow()

    private val _eventChannel = Channel<String>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private var previousActivosState: PedidoUiState = PedidoUiState.Idle

    fun dismissActivosError() {
        _activosState.value = previousActivosState
    }

    fun cargarPedidos(silencioso: Boolean = false   ) {
        if (_activosState.value is PedidoUiState.Loading) return
        viewModelScope.launch {
            if (!silencioso) {
                _activosState.value = PedidoUiState.Loading
            }

            repository.obtenerPedidosCliente()
                .onSuccess { todosLosPedidos ->
                    Log.d("Pedidos", "Pedidos obtenidos: ${todosLosPedidos.size}")
                    // Obtener una lista de IDs de restaurantes únicos (como Long para el repo)
                    val idsRestaurantes =
                        todosLosPedidos.mapNotNull { it.idRestaurante?.toLong() }.distinct()

                    // Buscar los detalles de los restaurantes en paralelo
                    val restaurantesMap = coroutineScope {
                        idsRestaurantes.map { id ->
                            async {
                                // En lugar de usar getOrNull() directo, evaluamos el resultado
                                val resultado = repositoryRestaurante.getRestauranteDatos(id)

                                resultado.onFailure { error ->
                                    // ¡Esto te dirá si la API de restaurantes está fallando!
                                    Log.e(
                                        "PedidoViewModel",
                                        "Error al cargar restaurante $id: ${error.message}"
                                    )
                                }

                                id to resultado.getOrNull()
                            }
                        }.awaitAll().toMap()
                    }

                    // Mapear tus DTOPedido a PedidoUiModel combinando los datos
                    val pedidosConRestaurante = todosLosPedidos.map { pedido ->
                        val restaurante = restaurantesMap[pedido.idRestaurante?.toLong()]
                        PedidoUiModel(
                            pedido = pedido,
                            nombreRestaurante = restaurante?.nombre ?: "Restaurante Desconocido",
                            telefonoRestaurante = restaurante?.telefono ?: "Sin teléfono"
                        )
                    }

                    _activosState.value = PedidoUiState.Success(pedidosConRestaurante)
                    Log.d("Pedidos", "Nuevo estado activo emitido con ${pedidosConRestaurante.size} elementos")
                }
                .onFailure {
                    if (!silencioso) {
                        _activosState.value = PedidoUiState.Error(it.message ?: "Error desconocido")
                    }
                }
        }
    }

    suspend fun esperarNuevoPedido(conteoAnterior: Int, timeOutMillis: Long = 20000L): Boolean {
        return withTimeoutOrNull(timeOutMillis) {
            var conseguido = false
            while (!conseguido) {
                // Consultamos solo la lista ligera al backend directamente
                val resultado = repository.obtenerPedidosCliente().getOrNull()

                if (resultado != null && resultado.size > conteoAnterior) {
                    conseguido = true
                    // Cuando confirmamos que ya llegó, hacemos UNA sola carga con datos completos (silenciosa)
                    cargarPedidos(silencioso = true)
                } else {
                    // Esperamos 2.5 segundos antes de volver a preguntar para no saturar el backend
                    delay(2500)
                }
            }
            true
        } ?: false // Retorna false si se agotó el tiempo
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _historialState.value = PedidoUiState.Loading

            repository.obtenerPedidosHistorial()
                .onSuccess { todosLosPedidos ->

                    // Obtener una lista de IDs de restaurantes únicos (como Long para el repo)
                    val idsRestaurantes =
                        todosLosPedidos.mapNotNull { it.idRestaurante?.toLong() }.distinct()

                    // Buscar los detalles de los restaurantes en paralelo
                    val restaurantesMap = coroutineScope {
                        idsRestaurantes.map { id ->
                            async {
                                // En lugar de usar getOrNull() directo, evaluamos el resultado
                                val resultado = repositoryRestaurante.getRestauranteDatos(id)

                                resultado.onFailure { error ->
                                    // ¡Esto te dirá si la API de restaurantes está fallando!
                                    Log.e(
                                        "PedidoViewModel",
                                        "Error al cargar restaurante $id: ${error.message}"
                                    )
                                }

                                id to resultado.getOrNull()
                            }
                        }.awaitAll().toMap()
                    }

                    // Mapear tus DTOPedido a PedidoUiModel combinando los datos
                    val pedidosConRestaurante = todosLosPedidos.map { pedido ->
                        val restaurante = restaurantesMap[pedido.idRestaurante?.toLong()]
                        PedidoUiModel(
                            pedido = pedido,
                            nombreRestaurante = restaurante?.nombre ?: "Restaurante Desconocido",
                            telefonoRestaurante = restaurante?.telefono ?: "Sin teléfono"
                        )
                    }

                    //  Filtrar -- Deveria de venir todo ya filtrado
                    val historial = pedidosConRestaurante.filter {
                        it.pedido.estado in listOf(
                            EnumEstadoPedido.Cancelado,
                            EnumEstadoPedido.Entregado,
                            EnumEstadoPedido.Reembolsado
                        )
                    }

                    _historialState.value = PedidoUiState.Historial(historial)
                }
                .onFailure {
                    _historialState.value = PedidoUiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

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
}
