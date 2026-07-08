package com.grupo6.trego.ui.pedidos

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Importación recomendada
import androidx.navigation.NavController
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.ui.componentes.ConfirmDialogComponent
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.pedidos.componentes.ActivePedidoCard
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

/**
 * Esta es la pantalla donde el usuario puede seguir el estado de sus pedidos actuales. 
 * Permite ver qué está pasando con su comida en tiempo real, cancelar si hace falta 
 * o iniciar un reclamo si hubo algún problema, además de dar acceso rápido al historial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(navController: NavController) {
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("La vista no está alojada en una ComponentActivity")
    val viewModel: PedidoViewModel = koinViewModel(viewModelStoreOwner = activity)
    val activosState by viewModel.activosState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val state = rememberPullToRefreshState()
    var pedidoACancelar by remember { mutableStateOf<PedidoUiModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    /* Mostramos avisos en pantalla cuando un pedido se cancela o se envía un reclamo con éxito. */
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarPedidos(silencioso = true)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = TregoOrange,
                    contentColor = Color.White,
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        topBar = {
            TregoHeader(
                title = "MIS PEDIDOS",
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { navController.navigate("historial") }) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial pedido",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.cargarPedidos(silencioso = true) },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    containerColor = BlancoCard,
                    color = TregoOrange,
                    state = state
                )
            }
        ) {
            when (val state = activosState) {
                is PedidoUiState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = TregoOrange
                        )
                    }
                }

                is PedidoUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        VistaError(
                            mensaje = state.message,
                            onReintentar = { viewModel.cargarPedidos() }
                        )
                    }
                }

                is PedidoUiState.Success -> {
                    /* Si no hay pedidos en marcha, mostramos un mensaje amigable invitando al usuario a pedir algo. */
                    if (state.activos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            VistaEstado(
                                titulo = "No tienes pedidos activos",
                                mensaje = "¡Tus pedidos pagados aparecerán aquí!",
                                icono = Icons.Default.NotificationsNone,
                                colorIcono = Color.LightGray,
                                onAccion = null
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    "PEDIDOS EN CURSO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }

                            /* Listamos cada pedido activo usando una tarjeta que muestra todos sus detalles y acciones. */
                            items(
                                items = state.activos,
                                key = { it.pedido.idPedido ?: 0 }
                            ) { pedido ->
                                ActivePedidoCard(
                                    order = pedido,
                                    onCancelClick = { pedidoACancelar = pedido },
                                    onClickReclamo = { peticionReclamo, onSuccess ->
                                        viewModel.crearReclamo(peticionReclamo, onSuccess)
                                    }
                                )
                            }
                        }
                    }
                }

                else -> {}
            }

            /* Un diálogo de seguridad para confirmar que el usuario realmente quiere cancelar su pedido. */
            pedidoACancelar?.let { seguroPedido ->
                ConfirmDialogComponent(
                    title = "Cancelar Pedido",
                    message = "¿Estás seguro que deseas cancelar tu pedido de ${seguroPedido.nombreRestaurante} (Pedido #${seguroPedido.pedido.idPedido})?",
                    confirmText = "Sí, Cancelar",
                    dismissText = "No, Volver",
                    onConfirm = {
                        viewModel.cancelarPedido(seguroPedido.pedido)
                        pedidoACancelar = null
                    },
                    onDismiss = { pedidoACancelar = null }
                )
            }
        }
    }
}