package com.grupo6.trego.ui.pedidos

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(
    viewModel: PedidoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHistoryModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TregoHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "MIS PEDIDOS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(
                        onClick = { showHistoryModal = true },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.History, "Historial", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (val state = uiState) {
                is PedidoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TregoOrange
                    )
                }

                is PedidoUiState.Error -> {
                    Text(
                        state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
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
                                ActiveOrderCard(pedido)
                            }
                        }
                    }

                    if (showHistoryModal) {
                        HistorialModal(
                            pedidoModel = state.historial,
                            onDismiss = { showHistoryModal = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveOrderCard(pedidoModel: PedidoUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Pedido #${pedidoModel.pedido.idPedido}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        pedidoModel.pedido.estado?.name ?: "Estado desconocido",
                        color = TregoOrange,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        pedidoModel.nombreRestaurante ?: "Estado desconocido",
                        color = TregoOrange,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = TregoOrange,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", color = Color.Gray)
                Text("$${pedidoModel.pedido.total}", fontWeight = FontWeight.Bold)
            }

            if (pedidoModel.pedido.horaEntregaEstimada != null) {
                val time =
                    pedidoModel.pedido.horaEntregaEstimada.format(DateTimeFormatter.ofPattern("HH:mm"))
                Text(
                    "Entrega estimada: $time",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight(0.8f)) {
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
