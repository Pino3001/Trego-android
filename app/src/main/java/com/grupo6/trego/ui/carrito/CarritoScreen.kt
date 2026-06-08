package com.grupo6.trego.ui.carrito

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grupo6.trego.R
import com.grupo6.trego.ui.carrito.componentes.CarritoItemCard
import com.grupo6.trego.ui.carrito.componentes.DireccionSelectorModal
import com.grupo6.trego.ui.carrito.componentes.ProductoDetalleModal
import com.grupo6.trego.ui.componentes.DialogComponent
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavController,
    statusDePago: String? = null
) {
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("La vista no está alojada en una ComponentActivity")

    // 🌟 Compartimos ViewModels con la Activity como owner para sincronización total
    val viewModel: CarritoViewModel = koinViewModel(viewModelStoreOwner = activity)
    val pedidoViewModel: PedidoViewModel = koinViewModel(viewModelStoreOwner = activity)

    var mostrarSelectorDireccion by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var procesandoPago by remember { mutableStateOf(false) }

    // ═══ PROCESAR RESULTADO DE DEEP LINK (MERCADO PAGO) ═══
    LaunchedEffect(statusDePago) {
        Log.d("DeepLink", "Status recibido: $statusDePago")
        when (statusDePago) {
            "exito" -> {
                viewModel.marcarPagoExitoso()
                procesandoPago = true

                val nuevoPedidoLlego = pedidoViewModel.esperarNuevoPedido()

                procesandoPago = false

                if (!nuevoPedidoLlego) {
                    // Si pasaron los 15 segundos (timeout) y no llegó el push por problemas de red/servidor:
                    Toast.makeText(
                        activity,
                        "Tu pago fue exitoso, pero el sistema está tardando en procesarlo. Refresca la lista en unos segundos.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                navController.navigate("pedido") {
                    popUpTo("carrito") { inclusive = true }
                }
            }

            "rechazado" -> {
                viewModel.marcarPagoRechazado()
                snackbarHostState.showSnackbar("El pago fue rechazado. Revisa tus datos.")
            }

            "pendiente" -> {
                viewModel.marcarPagoPendiente()
                pedidoViewModel.cargarPedidos()
                navController.navigate("pedido") {
                    popUpTo("carrito") { inclusive = true }
                }
            }
        }
    }
    val estadoDirecciones by viewModel.direccionesState.collectAsStateWithLifecycle()
    val direccionesList = (estadoDirecciones as? DireccionesState.Cargadas)?.items ?: emptyList()

    LaunchedEffect(mostrarSelectorDireccion) {
        if (mostrarSelectorDireccion) {
            viewModel.cargarDirecciones()
        }
    }

    // ─── Modales ─────────────────────────────────────────────────────────
    if (mostrarSelectorDireccion) {
        DireccionSelectorModal(
            direcciones = direccionesList,
            seleccionada = viewModel.direccionSeleccionada,
            onConfirmar = { viewModel.seleccionarDireccion(it); mostrarSelectorDireccion = false },
            onDismiss = { mostrarSelectorDireccion = false }
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

    // ─── ALERTAS ────────────────────────────────────────────────────────
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    if (errorDialogMessage != null) {
        DialogComponent(message = errorDialogMessage!!, onDismiss = { errorDialogMessage = null })
    }

    LaunchedEffect(Unit) {
        viewModel.recargarCarrito()
    }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { mensaje ->
            errorDialogMessage = mensaje
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
            TregoHeader(
                title = "CARRITO",
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val rid = viewModel.currentRestauranteId
                            if (rid != null && rid != 0L) {
                                navController.navigate("menu/$rid") { popUpTo("restaurants") }
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (viewModel.items.isNotEmpty()) {
                        IconButton(onClick = { viewModel.limpiarCarrito() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar",
                                tint = Color.White
                            )
                        }
                    }
                },
                bottomContent = {
                    Text(
                        text = viewModel.nombreRestaurante.ifBlank { "Tu Pedido" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = viewModel.uiState) {
                is CarritoUiState.Vacio -> {
                    VistaEstado(
                        titulo = "Tu carrito está vacío",
                        mensaje = "Explora restaurantes y añade tus platos favoritos para empezar un pedido.",
                        iconoResId = R.drawable.bolsa,
                        colorIcono = Color.Gray,
                        botonTexto = "Explorar Restaurantes",
                        onAccion = { navController.popBackStack() }
                    )
                }

                is CarritoUiState.Cargado -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                SelectorDireccionItem(
                                    viewModel = viewModel,
                                    onAbrirSelector = { mostrarSelectorDireccion = true })
                            }
                            item {
                                Text(
                                    "Lista de Pedidos:",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                            items(state.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta ->
                                        viewModel.cambiarCantidad(
                                            item,
                                            delta
                                        )
                                    })
                            }
                        }
                        TotalYBoton(
                            viewModel = viewModel,
                            enabled = state.items.isNotEmpty(),
                            labelBoton = "REALIZAR PEDIDO"
                        )
                    }
                }

                is CarritoUiState.Cargando -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = TregoOrange) }
                }

                is CarritoUiState.PagoExitoso -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = TregoOrange) }
                }

                is CarritoUiState.PagoRechazado -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                BannerEstadoPago(
                                    Icons.Default.Warning,
                                    MaterialTheme.colorScheme.error,
                                    Color(0xFFFFEBEE),
                                    "Pago rechazado",
                                    "Hubo un problema con tu pago."
                                )
                            }
                            item {
                                SelectorDireccionItem(
                                    viewModel = viewModel,
                                    onAbrirSelector = { mostrarSelectorDireccion = true })
                            }
                            items(viewModel.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta ->
                                        viewModel.cambiarCantidad(
                                            item,
                                            delta
                                        )
                                    })
                            }
                        }
                        TotalYBoton(
                            viewModel = viewModel,
                            enabled = viewModel.items.isNotEmpty(),
                            labelBoton = "REINTENTAR PAGO"
                        )
                    }
                }

                is CarritoUiState.PagoPendiente -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                BannerEstadoPago(
                                    Icons.Default.Info,
                                    Color(0xFFF57C00),
                                    Color(0xFFFFF3E0),
                                    "Pago pendiente",
                                    "El pedido está en espera."
                                )
                            }
                            item {
                                SelectorDireccionItem(
                                    viewModel = viewModel,
                                    onAbrirSelector = { mostrarSelectorDireccion = true })
                            }
                            items(viewModel.items) { item ->
                                CarritoItemCard(
                                    item = item,
                                    onEditar = { viewModel.abrirModalEditar(item) },
                                    onEliminar = { viewModel.eliminarItem(item) },
                                    onCambiarCantidad = { delta ->
                                        viewModel.cambiarCantidad(
                                            item,
                                            delta
                                        )
                                    })
                            }
                        }
                        TotalYBoton(
                            viewModel = viewModel,
                            enabled = viewModel.items.isNotEmpty(),
                            labelBoton = "COMPLETAR PAGO"
                        )
                    }
                }

                is CarritoUiState.RestauranteCerrado -> {
                    VistaEstado(
                        titulo = "Restaurante Cerrado",
                        mensaje = "El restaurante ya no acepta pedidos por hoy. ¡Te esperamos mañana!",
                        iconoResId = R.drawable.tregologo,
                        botonTexto = "Volver al inicio",
                        onAccion = {
                            viewModel.reiniciar()
                            navController.navigate("restaurants") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                is CarritoUiState.AbrirMercadoPago -> {
                    LaunchedEffect(state.url) {
                        val customTabsIntent =
                            androidx.browser.customtabs.CustomTabsIntent.Builder()
                                .setShowTitle(true).build()
                        customTabsIntent.launchUrl(activity, Uri.parse(state.url))
                        viewModel.onPreferenciaLanzada()
                    }
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = TregoOrange) }
                }
            }
            AnimatedVisibility(
                visible = procesandoPago,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                // Bloqueamos el botón "Atrás" físico/gestual del teléfono
                BackHandler(enabled = true) {
                    // No hacemos nada, obligamos al usuario a esperar
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        // 🌟 CLAVE: Consumimos los toques para que no traspasen a los botones de abajo
                        .pointerInput(Unit) { detectTapGestures { } },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = TregoOrange,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Procesando tu pago",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Por favor, no cierres la aplicación.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── COMPOSABLES DE APOYO ───

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
        Text(
            "Entregar en:",
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Button(
            onClick = onAbrirSelector,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = if (viewModel.direccionSeleccionada != null) Color.Black else TregoOrange
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (viewModel.direccionSeleccionada != null) Color.LightGray else TregoOrange.copy(
                    alpha = 0.5f
                )
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TregoOrange
                )
                Text(
                    text = viewModel.direccionSeleccionada?.calle ?: "Seleccionar dirección",
                    fontWeight = if (viewModel.direccionSeleccionada != null) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Cambiar",
                    tint = Color.Gray
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
            Text(
                labelBoton,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = Color.White
            )
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = tintIcono,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.SemiBold, color = tintIcono, fontSize = 14.sp)
                Text(
                    mensaje,
                    fontSize = 12.sp,
                    color = tintIcono.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
