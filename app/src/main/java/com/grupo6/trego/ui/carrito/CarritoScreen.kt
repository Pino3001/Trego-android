package com.grupo6.trego.ui.carrito

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.grupo6.trego.ui.carrito.componentes.CarritoItemCard
import com.grupo6.trego.ui.carrito.componentes.DireccionSelector
import com.grupo6.trego.ui.carrito.componentes.ProductoDetalleModal
import com.grupo6.trego.ui.tabs.NavigationTabs
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.R
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
            onConfirm = { viewModel.confirmarModal(it) },
            onDismiss = { viewModel.cerrarModal() },
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

                is CarritoUiState.Vacio -> {
                    // Mensaje de carrito vacío
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.bolsa), // tu ícono
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Tu carrito está vacío",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "¡Agregá productos para empezar!",
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

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
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta ->
                                        viewModel.cambiarCantidad(item, delta)
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

                // ─── ERROR ───
                is CarritoUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.reiniciar() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is CarritoUiState.Cargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TregoOrange)
                    }
                }
                // ─── PAGO EXITOSO ───
                is CarritoUiState.PagoExitoso -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.trego),  // usa un ícono de éxito
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("¡Pedido realizado con éxito!", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }

                // ─── PAGO RECHAZADO ───
                is CarritoUiState.PagoRechazado -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.trego),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Pago rechazado", style = MaterialTheme.typography.headlineSmall)
                            Text("Intentá de nuevo más tarde", color = Color.Gray)
                        }
                    }
                }

                // ─── RESTAURANTE CERRADO ───
                is CarritoUiState.RestauranteCerrado -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("El restaurante está cerrado", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No se pueden realizar pedidos en este momento.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}