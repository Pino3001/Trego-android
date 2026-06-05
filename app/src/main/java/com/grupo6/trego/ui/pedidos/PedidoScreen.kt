package com.grupo6.trego.ui.pedidos

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.ui.componentes.ActivePedidoCard
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen() {
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("La vista no está alojada en una ComponentActivity")
    val viewModel: PedidoViewModel = koinViewModel(viewModelStoreOwner = activity)
    val activosState by viewModel.activosState.collectAsState()
    val historialState by viewModel.historialState.collectAsState()

    var showHistoryModal by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Log.d("Pedidos", "CarritoScreen ViewModel: $viewModel")
// Escuchamos el evento
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarPedidos()
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TregoHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    // Título anclado arriba al centro
                    Text(
                        text = "MIS PEDIDOS",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )

                    // Píldora anclada abajo al centro
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showHistoryModal = true
                                viewModel.cargarHistorial()
                            },
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Ver historial",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(13.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.cargarPedidos() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = activosState) {
                is PedidoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TregoOrange
                    )
                }

                is PedidoUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.dismissActivosError()  // vuelve al Success anterior
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissActivosError() }) {
                                Text("Aceptar")
                            }
                        },
                        title = { Text("Error") },
                        text = { Text(state.message) }
                    )
                }

                is PedidoUiState.Success -> {
                    if (state.activos.isEmpty()) {
                        EmptyActiveOrders()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                ActivePedidoCard(pedido, onCancelClick = {
                                    viewModel.cancelarPedido(
                                        pedido.pedido
                                    )
                                })
                            }
                        }
                    }

                }

                else -> {}

            }

            if (showHistoryModal) {
                when (val hState = historialState) {
                    is PedidoUiState.Loading -> {
                        // Muestra un loader centrado sobre la pantalla mientras baja el historial
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TregoOrange)
                        }
                    }

                    is PedidoUiState.Historial -> {
                        HistorialModal(
                            pedidoModel = hState.historial,
                            onDismiss = { showHistoryModal = false }
                        )
                    }

                    is PedidoUiState.Error -> {
                        // Si falla el historial, mostramos un cartelito y cerramos el modal
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

@Composable
fun EmptyActiveOrders() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text("No tienes pedidos activos", color = Color.Gray)
        Text("¡Tus pedidos pagados aparecerán aquí!", fontSize = 12.sp, color = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialModal(pedidoModel: List<PedidoUiModel>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(0.8f)
        ) {
            Text(
                "HISTORIAL DE PEDIDOS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (pedidoModel.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay pedidos anteriores", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pedidoModel) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Pedido #${item.pedido.idPedido}",
                                    fontWeight = FontWeight.Bold
                                )
                                val fecha =
                                    item.pedido.fechaCreacion?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                        ?: "---"
                                Text(fecha, fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$${item.pedido.total}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TregoOrange
                                )
                                Text(
                                    item.pedido.estado?.name ?: "",
                                    fontSize = 10.sp,
                                    color = if (item.pedido.estado == EnumEstadoPedido.Entregado) Color(
                                        0xFF4CAF50
                                    ) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
