package com.grupo6.trego.ui.pedidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.RestauranteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PedidoUiState {
    object Loading : PedidoUiState()
    data class Success(
        val activos: List<PedidoUiModel>,
        val historial: List<PedidoUiModel>
    ) : PedidoUiState()

    data class Error(val message: String) : PedidoUiState()
}

class PedidoViewModel(
    private val repository: PedidoRepository,
    private val repositoryRestaurante: RestauranteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PedidoUiState>(PedidoUiState.Loading)
    val uiState: StateFlow<PedidoUiState> = _uiState.asStateFlow()

    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = PedidoUiState.Loading

            repository.obtenerPedidosCliente()
                .onSuccess { todosLosPedidos ->

                    // Obtener una lista de IDs de restaurantes únicos (como Long para el repo)
                    val idsRestaurantes = todosLosPedidos.mapNotNull { it.idRestaurante?.toLong() }.distinct()

                    // Buscar los detalles de los restaurantes en paralelo
                    val restaurantesMap = coroutineScope {
                        idsRestaurantes.map { id ->
                            async {
                                // En lugar de usar getOrNull() directo, evaluamos el resultado
                                val resultado = repositoryRestaurante.getRestauranteDatos(id)

                                resultado.onFailure { error ->
                                    // ¡Esto te dirá si la API de restaurantes está fallando!
                                    Log.e("PedidoViewModel", "Error al cargar restaurante $id: ${error.message}")
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
                    val activos = pedidosConRestaurante.filter {
                        it.pedido.estado in listOf(
                            EnumEstadoPedido.Pagado,
                            EnumEstadoPedido.Aprobado,
                            EnumEstadoPedido.EnCamino
                        )
                    }

                    val historial = pedidosConRestaurante.filter {
                        it.pedido.estado in listOf(
                            EnumEstadoPedido.Entregado,
                            EnumEstadoPedido.Cancelado,
                            EnumEstadoPedido.Reembolsado,
                            EnumEstadoPedido.PagoRechazado
                        )
                    }

                    _uiState.value = PedidoUiState.Success(activos, historial)
                }
                .onFailure {
                    _uiState.value = PedidoUiState.Error(it.message ?: "Error desconocido")
                }
        }
    }
}
