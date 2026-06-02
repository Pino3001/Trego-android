package com.grupo6.trego.ui.carrito

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.grupo6.trego.R
import com.grupo6.trego.ui.carrito.componentes.CarritoItemCard
import com.grupo6.trego.ui.carrito.componentes.DireccionSelectorModal
import com.grupo6.trego.ui.carrito.componentes.ProductoDetalleModal
import com.grupo6.trego.ui.componentes.DialogComponent
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavController,
) {
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("La vista no está alojada en una ComponentActivity")
    val viewModel: CarritoViewModel = koinViewModel(viewModelStoreOwner = activity)

    var mostrarSelectorDireccion by remember { mutableStateOf(false) }

// Estado para mostrar mensajes de error/rechazo
    val snackbarHostState = remember { SnackbarHostState() }

    // ═══ LAUNCHED EFFECT: EL CONTROLADOR DE ESTADOS DE PAGO ═══
    LaunchedEffect(viewModel.uiState) {
        when (viewModel.uiState) {
            is CarritoUiState.PagoExitoso -> {
                // Si por alguna razón el estado pasa a éxito acá, mandamos a la pantalla final
                navController.navigate("pedido") {
                    popUpTo("carrito") { inclusive = true }
                }
                viewModel.reiniciar()
            }
            is CarritoUiState.PagoRechazado -> {
                // Mostramos un mensaje flotante de que el pago falló
                snackbarHostState.showSnackbar("El pago fue rechazado. Por favor, intenta nuevamente.")
            }
            else -> {}
        }
    }

    // ─── Modales mejorados ────────────────────────────────────────────────
    if (mostrarSelectorDireccion) {
        DireccionSelectorModal(
            direcciones = viewModel.direcciones,
            seleccionada = viewModel.direccionSeleccionada,
            onConfirmar = { viewModel.seleccionarDireccion(it); mostrarSelectorDireccion = false },
            onDismiss = { mostrarSelectorDireccion = false },
            geoapifyApiKey = "d8f9a863d62a4f869bb5cac555b14ef3"
        )
    }

    if (viewModel.showModal && viewModel.productoEnModal != null) {
        ProductoDetalleModal(
            item = viewModel.productoEnModal!!,
            onConfirm = { viewModel.confirmarModal(it) },
            onDismiss = { viewModel.cerrarModal() },
            esEdicion = true
        )
    }

    // ─── ALERTAS PROFESIONALES (SUSTITUYEN AlertDialogs simples) ──────────
    
    // Diálogo de error / validación usando DialogComponent reutilizable
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    
    if (errorDialogMessage != null) {
        DialogComponent(
            message = errorDialogMessage!!,
            onDismiss = { errorDialogMessage = null }
        )
    }

    // El viewModel ahora puede disparar este mensaje
    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is CarritoUiState.Error) {
            errorDialogMessage = (viewModel.uiState as CarritoUiState.Error).mensaje
        }
    }

    if (viewModel.uiState == CarritoUiState.RestauranteCerrado) {
        DialogComponent(
            message = "El restaurante ya no acepta pedidos por hoy. ¡Te esperamos mañana!",
            onDismiss = {
                viewModel.reiniciar()
                navController.popBackStack()
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TregoHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val rid = viewModel.currentRestauranteId
                            if (rid != null && rid != 0L) {
                                navController.navigate("menu/$rid") {
                                    popUpTo("restaurants") // mantiene el listado en el fondo
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CARRITO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = viewModel.nombreRestaurante.ifBlank { "Tu Pedido" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (viewModel.items.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 8.dp, top = 4.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            IconButton(onClick = { viewModel.limpiarCarrito() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Limpiar carrito",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = viewModel.uiState) {

                // ─── VACÍO MEJORADO ──────────────────────────────────────────
                is CarritoUiState.Vacio -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(150.dp),
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.bolsa),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Tu carrito está vacío",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Parece que aún no has añadido\nnada delicioso a tu pedido.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                        ) {
                            Text("Explorar Restaurantes", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ─── CARGADO ──────────────────────────────────────────────────
                is CarritoUiState.Cargado -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                SelectorDireccionItem(
                                    viewModel = viewModel,
                                    onAbrirSelector = { mostrarSelectorDireccion = true }
                                )
                            }
                            items(state.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta -> viewModel.cambiarCantidad(item, delta) }
                                )
                            }
                        }
                        TotalYBoton(
                            viewModel = viewModel,
                            enabled = state.items.isNotEmpty(),
                            labelBoton = "REALIZAR PEDIDO"
                        )
                    }
                }

                // ─── CARGANDO ─────────────────────────────────────────────────
                is CarritoUiState.Cargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TregoOrange)
                    }
                }

                // ─── ERROR ────────────────────────────────────────────────────
                is CarritoUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.reiniciar() }) { Text("Reintentar") }
                        }
                    }
                }

                // ─── PAGO EXITOSO → spinner mientras LaunchedEffect navega ────
                is CarritoUiState.PagoExitoso -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TregoOrange)
                    }
                }

                // ─── PAGO RECHAZADO → carrito con banner de error ─────────────
                is CarritoUiState.PagoRechazado -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                BannerEstadoPago(
                                    icono = Icons.Default.Warning,
                                    tintIcono = MaterialTheme.colorScheme.error,
                                    colorFondo = Color(0xFFFFEBEE),
                                    titulo = "Pago rechazado",
                                    mensaje = "Hubo un problema con tu pago. Revisá tus datos e intentá de nuevo."
                                )
                            }
                            item {
                                SelectorDireccionItem(
                                    viewModel = viewModel,
                                    onAbrirSelector = { mostrarSelectorDireccion = true }
                                )
                            }
                            items(viewModel.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta -> viewModel.cambiarCantidad(item, delta) }
                                )
                            }
                        }
                        TotalYBoton(
                            viewModel = viewModel,
                            enabled = viewModel.items.isNotEmpty(),
                            labelBoton = "REINTENTAR PAGO"
                        )
                    }
                }

                // ─── PAGO PENDIENTE → carrito con banner de advertencia ────────
                is CarritoUiState.PagoPendiente -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                BannerEstadoPago(
                                    icono = Icons.Default.Info,
                                    tintIcono = Color(0xFFF57C00),
                                    colorFondo = Color(0xFFFFF3E0),
                                    titulo = "Pago pendiente",
                                    mensaje = "El pedido está en espera hasta que se confirme tu pago."
                                )
                            }
                            item {
                                SelectorDireccionItem(
                                    viewModel = viewModel,
                                    onAbrirSelector = { mostrarSelectorDireccion = true }
                                )
                            }
                            items(viewModel.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta -> viewModel.cambiarCantidad(item, delta) }
                                )
                            }
                        }
                        TotalYBoton(
                            viewModel = viewModel,
                            enabled = viewModel.items.isNotEmpty(),
                            labelBoton = "COMPLETAR PAGO"
                        )
                    }
                }

                // ─── RESTAURANTE CERRADO ──────────────────────────────────────
                is CarritoUiState.RestauranteCerrado -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("El restaurante está cerrado", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No se pueden realizar pedidos en este momento.", color = Color.Gray)
                        }
                    }
                }

                // ─── ABRIENDO MERCADO PAGO ────────────────────────────────────
                is CarritoUiState.AbrirMercadoPago -> {
                    LaunchedEffect(state.url) {
                        val customTabsIntent =
                            androidx.browser.customtabs.CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                        customTabsIntent.launchUrl(activity, android.net.Uri.parse(state.url))
                        viewModel.onPreferenciaLanzada()
                    }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TregoOrange)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Composables privados de apoyo
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SelectorDireccionItem(
    viewModel: CarritoViewModel,
    onAbrirSelector: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text("Entregar en:", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Button(
            onClick = onAbrirSelector,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.direccionSeleccionada != null)
                    Color(0xFFF5F5F5) else colorResource(id = R.color.trego_orange),
                contentColor = if (viewModel.direccionSeleccionada != null)
                    Color.Black else Color(0xFFF5F5F5),
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.direccionSeleccionada?.calle ?: "Seleccionar dirección",
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun TotalYBoton(
    viewModel: CarritoViewModel,
    enabled: Boolean,
    labelBoton: String
) {
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
            Text("Total :", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                "${viewModel.total.toInt()}$",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = TregoOrange
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.confirmarPedido() },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
            shape = RoundedCornerShape(50)
        ) {
            Text(labelBoton, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
private fun BannerEstadoPago(
    icono: ImageVector,
    tintIcono: Color,
    colorFondo: Color,
    titulo: String,
    mensaje: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = tintIcono, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.SemiBold, color = tintIcono, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(mensaje, fontSize = 12.sp, color = tintIcono.copy(alpha = 0.85f), lineHeight = 16.sp)
            }
        }
    }
}