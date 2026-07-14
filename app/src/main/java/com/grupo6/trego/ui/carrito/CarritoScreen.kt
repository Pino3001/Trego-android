package com.grupo6.trego.ui.carrito

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

/**
 * Esta es la pantalla principal del carrito. Aquí el usuario puede revisar lo que 
 * eligió, cambiar cantidades, elegir su dirección de entrega y finalmente pagar 
 * su pedido. También manejamos la vuelta desde Mercado Pago para confirmar el éxito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavController,
    statusDePago: String? = null
) {
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("La vista no está alojada en una ComponentActivity")

    val viewModel: CarritoViewModel = koinViewModel(viewModelStoreOwner = activity)
    val pedidoViewModel: PedidoViewModel = koinViewModel(viewModelStoreOwner = activity)

    var mostrarSelectorDireccion by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pagoProcesado by rememberSaveable { mutableStateOf(false) }
    var procesandoPago by remember { mutableStateOf(false) }

    /* Cuando volvemos de la pasarela de pagos, este bloque analiza el resultado para avisarle al usuario. */
    LaunchedEffect(statusDePago) {
        if (statusDePago == null || pagoProcesado) return@LaunchedEffect
        Log.d("DeepLink", "Status recibido: $statusDePago")
        pagoProcesado = true
        when (statusDePago) {
            "success" -> {
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
                    popUpTo("restaurants") { inclusive = false }
                }
            }

            "failure" -> {
                viewModel.marcarPagoRechazado()
                snackbarHostState.showSnackbar("El pago fue rechazado. Revisa tus datos.")
            }

            "pending" -> {
                viewModel.marcarPagoPendiente()
                pedidoViewModel.cargarPedidos()
                navController.navigate("pedido") {
                    popUpTo("restaurants") { inclusive = false }
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
            onDismiss = { mostrarSelectorDireccion = false },
            titulo = "Direccion de entrega"
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
    LaunchedEffect(Unit) {
        viewModel.recargarCarrito()
        viewModel.errorEvent.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        containerColor = Color.White,
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
                        }, modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            tint = Color.White,
                            contentDescription = "Volver"
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
                        text = viewModel.nombreRestaurante.ifBlank { "" },
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
            Column(modifier = Modifier.fillMaxSize()) {

    /* Este botón permite al usuario elegir o cambiar la dirección donde quiere que llegue su comida. */
                SelectorDireccionItem(
                    viewModel = viewModel,
                    onAbrirSelector = { mostrarSelectorDireccion = true }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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

                        is CarritoUiState.Cargando, is CarritoUiState.PagoExitoso, is CarritoUiState.AbrirMercadoPago -> {
                            if (state is CarritoUiState.AbrirMercadoPago) {
                                LaunchedEffect(state.url) {
                                    val customTabsIntent =
                                        androidx.browser.customtabs.CustomTabsIntent.Builder()
                                            .setShowTitle(true).build()
                                    customTabsIntent.launchUrl(activity, Uri.parse(state.url))
                                    viewModel.onPreferenciaLanzada()
                                }
                            }
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TregoOrange)
                            }
                        }

                        is CarritoUiState.Error -> {
                            VistaError(
                                mensaje = state.message,
                                onReintentar = { viewModel.recargarCarrito() }
                            )
                        }

                        is CarritoUiState.RestauranteCerrado -> {
                            VistaEstado(
                                titulo = "Restaurante Cerrado",
                                mensaje = "El restaurante ya no acepta pedidos por hoy. ¡Te esperamos mañana!",
                                iconoResId = R.drawable.bolsa,
                                botonTexto = "Volver al inicio",
                                onAccion = {
                                    viewModel.reiniciar()
                                    navController.navigate("restaurants") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        is CarritoUiState.Cargado, is CarritoUiState.PagoRechazado, is CarritoUiState.PagoPendiente -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    if (state is CarritoUiState.PagoRechazado) {
                                        item {
                                            BannerEstadoPago(
                                                icono = Icons.Default.Warning,
                                                tintIcono = MaterialTheme.colorScheme.error,
                                                colorFondo = Color(0xFFFFEBEE),
                                                titulo = "Pago rechazado",
                                                mensaje = "Hubo un problema con tu pago."
                                            )
                                        }
                                    } else if (state is CarritoUiState.PagoPendiente) {
                                        item {
                                            BannerEstadoPago(
                                                icono = Icons.Default.Info,
                                                tintIcono = Color(0xFFF57C00),
                                                colorFondo = Color(0xFFFFF3E0),
                                                titulo = "Pago pendiente",
                                                mensaje = "El pedido está en espera."
                                            )
                                        }
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
                                            }
                                        )
                                    }
                                }

                                val labelBoton = when (state) {
                                    is CarritoUiState.PagoRechazado -> "REINTENTAR PAGO"
                                    is CarritoUiState.PagoPendiente -> "COMPLETAR PAGO"
                                    else -> "REALIZAR PEDIDO"
                                }
                                /* Mostramos el resumen del total y el botón principal para confirmar la compra. */
                                TotalYBoton(
                                    viewModel = viewModel,
                                    enabled = viewModel.items.isNotEmpty(),
                                    labelBoton = labelBoton
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = procesandoPago,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
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
}

@Composable
private fun SelectorDireccionItem(
    viewModel: CarritoViewModel,
    onAbrirSelector: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = TregoOrange,
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 50.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 50.dp))
            .clickable { onAbrirSelector() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            tint = Color.White,
            contentDescription = "Ubicación",
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            color = Color.White,
            text = "${viewModel.direccionSeleccionada?.calle ?: "Seleccionar dirección"} ${viewModel.direccionSeleccionada?.numero ?: ""}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            tint = Color.White,
            contentDescription = "Desplegar",
            modifier = Modifier.size(24.dp)
        )
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
            .padding(8.dp),
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
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.confirmarPedido() },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth(),
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
