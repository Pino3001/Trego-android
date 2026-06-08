package com.grupo6.trego.ui.pedidos

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.componentes.ConfirmDialogComponent
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.ui.pedidos.componentes.ActivePedidoCard
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(navController: NavController) {
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("La vista no está alojada en una ComponentActivity")
    val viewModel: PedidoViewModel = koinViewModel(viewModelStoreOwner = activity)
    val historialState by viewModel.historialState.collectAsState()

    var showHistoryModal by remember { mutableStateOf(false) }
    var pedidoACancelar by remember { mutableStateOf<PedidoUiModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val activosState by viewModel.activosState.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Escuchamos el evento de SnackBar
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Carga inicial
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
                    // La lógica del botón a la derecha es mucho más limpia así
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
            onRefresh = {
                viewModel.cargarPedidos(silencioso = true)
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = activosState) {
                is PedidoUiState.Loading -> {
                    // Evitamos mostrar el spinner doble si ya está la animación de pull-to-refresh
                    if (!isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = TregoOrange
                        )
                    }
                }

                is PedidoUiState.Error -> {
                    VistaError(
                        mensaje = state.message,
                        onReintentar = { viewModel.cargarPedidos() }
                    )
                }

                is PedidoUiState.Success -> {
                    if (state.activos.isEmpty()) {
                        VistaEstado(
                            titulo = "No tienes pedidos activos",
                            mensaje = "¡Tus pedidos pagados aparecerán aquí!",
                            icono = Icons.Default.NotificationsNone,
                            colorIcono = Color.LightGray,
                            onAccion = null
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize() // Aseguramos que ocupe todo el espacio para detectar el scroll
                        ) {
                            item {
                                Text(
                                    "PEDIDOS EN CURSO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                            items(state.activos) { pedido ->
                                ActivePedidoCard(
                                    pedido, onCancelClick = {
                                        pedidoACancelar = pedido
                                    },
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

            if (pedidoACancelar != null) {
                ConfirmDialogComponent(
                    title = "Cancelar Pedido",
                    message = "¿Estás seguro que deseas cancelar tu pedido de ${pedidoACancelar?.nombreRestaurante} (Pedido #${pedidoACancelar?.pedido?.idPedido})?",
                    confirmText = "Sí, Cancelar",
                    dismissText = "No, Volver",
                    onConfirm = {
                        pedidoACancelar?.let { viewModel.cancelarPedido(it.pedido) }
                        pedidoACancelar = null
                    },
                    onDismiss = { pedidoACancelar = null }
                )
            }

            // Lógica del modal de historial
            if (showHistoryModal) {
                when (val hState = historialState) {
                    is PedidoUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TregoOrange)
                        }
                    }

                    is PedidoUiState.Error -> {
                        AlertDialog(
                            onDismissRequest = { showHistoryModal = false },
                            confirmButton = {
                                TextButton(onClick = { showHistoryModal = false }) {
                                    Text("Aceptar")
                                }
                            },
                            title = { Text("Error") },
                            text = { Text(hState.message) }
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
