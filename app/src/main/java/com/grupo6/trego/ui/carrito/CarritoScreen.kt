package com.grupo6.trego.ui.carrito

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.grupo6.trego.ui.carrito.componentes.*
import com.grupo6.trego.ui.tabs.NavigationTabs
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
        navController: NavController,
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: CarritoViewModel = viewModel(viewModelStoreOwner = activity)

    // Modal de detalle/edición
    if (viewModel.showModal && viewModel.productoEnModal != null) {
        ProductoDetalleModal(
            item = viewModel.productoEnModal!!,
            onConfirmar = { viewModel.confirmarModal(it) },
            onDismiss = { viewModel.cerrarModal() }
        )
    }

    // Diálogo restaurante cerrado
    if (viewModel.uiState == CarritoUiState.RestauranteCerrado) {
        AlertDialog(
            onDismissRequest = { viewModel.reiniciar() },
            title = { Text("Restaurante cerrado") },
            text = { Text("El restaurante está cerrado y no se encuentra disponible para recibir pedidos.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reiniciar()
                    navController.popBackStack()
                }) {
                    Text("Volver", color = TregoOrange)
                }
            }
        )
    }

    // Diálogo pago exitoso
    if (viewModel.uiState == CarritoUiState.PagoExitoso) {
        AlertDialog(
            onDismissRequest = { viewModel.reiniciar() },
            title = { Text("¡Pedido realizado!") },
            text = { Text("Tu pedido fue enviado con éxito al restaurante.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reiniciar()
                    navController.navigate("restaurants") {
                        popUpTo("restaurants") { inclusive = true }
                    }
                }) {
                    Text("Aceptar", color = TregoOrange)
                }
            }
        )
    }

    // Diálogo pago rechazado
    if (viewModel.uiState == CarritoUiState.PagoRechazado) {
        AlertDialog(
            onDismissRequest = { viewModel.reiniciar() },
            title = { Text("Pago rechazado") },
            text = { Text("Hubo un problema con tu pago. Podés intentarlo de nuevo.") },
            confirmButton = {
                TextButton(onClick = { viewModel.reiniciar() }) {
                    Text("Intentar de nuevo", color = TregoOrange)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
                // Header naranja
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = TregoOrange,
                            modifier = Modifier.fillMaxSize()
                        ) {}
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Carrito",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (viewModel.nombreRestaurante.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    viewModel.nombreRestaurante,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                NavigationTabs(navController = navController, currentRoute = "carrito")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = viewModel.uiState) {

                is CarritoUiState.Vacio -> CarritoEmpty()

                is CarritoUiState.Cargado -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            // Título y selector de dirección
                            item {
                                DireccionSelector(
                                    direcciones = viewModel.direcciones,
                                    seleccionada = viewModel.direccionSeleccionada,
                                    onSeleccionar = { viewModel.seleccionarDireccion(it) },
                                    onUbicacionActual = { viewModel.usarUbicacionActual() }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }

                            // Items del carrito
                            items(state.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item.id) },
                                    onCambiarCantidad = { delta ->
                                        viewModel.cambiarCantidad(item.id, delta)
                                    }
                                )
                            }
                        }

                        // Total + botón realizar pedido
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total :",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${viewModel.total.toInt()}$",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = TregoOrange
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    // TODO: pasar restauranteAbierto real desde el ViewModel de menú
                                    viewModel.realizarPedido(restauranteAbierto = true)
                                },
                                enabled = state.items.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "REALIZAR PEDIDO",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}