package com.grupo6.trego.ui.menu

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.carrito.componentes.ProductoDetalleModal
import com.grupo6.trego.ui.componentes.CalificacionModal
import com.grupo6.trego.ui.componentes.ResenaCard
import com.grupo6.trego.ui.componentes.ResenasHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.menu.componentes.MenuHeader
import com.grupo6.trego.ui.menu.componentes.OfertaItem
import com.grupo6.trego.ui.menu.componentes.ProductoItem
import com.grupo6.trego.ui.theme.ComboGreenDark
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.theme.TregoSecondary
import com.grupo6.trego.ui.theme.onCancelar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MenuScreen(
    restauranteId: Long,
    navController: NavController,
) {
    val activity = LocalContext.current as? ComponentActivity
        ?: error("ComponentActivity no disponible")
    val menuViewModel: MenuViewModel = koinViewModel(viewModelStoreOwner = activity)
    val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()

    val carritoViewModel: CarritoViewModel = koinViewModel(viewModelStoreOwner = activity)
    var abrirStar by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        menuViewModel.uiEvent.collect { event ->
            when (event) {
                is MenuUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is MenuUiEvent.AbrirModalProducto -> {
                    (menuViewModel.uiState.value as? MenuUiState.Success)?.restaurante?.let { rest ->
                        carritoViewModel.abrirModalNuevoProducto(event.producto, rest, rest.abierto == true)
                    }
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        carritoViewModel.errorEvent.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    LaunchedEffect(restauranteId) {
        menuViewModel.loadMenu(restauranteId)
    }

    // Scaffold SIN topBar para permitir inmersión total de la foto
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
        containerColor = Color.White,
    ) { innerPadding ->

        val productoEnModal = carritoViewModel.productoEnModal
        if (carritoViewModel.showModal && productoEnModal != null) {
            ProductoDetalleModal(
                item = productoEnModal,
                onConfirm = { carritoViewModel.confirmarModal(it) },
                onDismiss = { carritoViewModel.cerrarModal() },
                esEdicion = false
            )
        }

        when (val state = uiState) {
            is MenuUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TregoOrange)
                }
            }

            is MenuUiState.SinProductos -> {
                VistaEstado(
                    titulo = "Sin Productos",
                    mensaje = "Este restaurante aún no ha cargado su menú.",
                    icono = Icons.Default.RestaurantMenu,
                    colorIcono = Color.LightGray,
                    botonTexto = "Volver al listado",
                    onAccion = { navController.popBackStack() }
                )
            }

            is MenuUiState.Error -> {
                VistaError(
                    mensaje = state.message,
                    onReintentar = { menuViewModel.loadMenu(restauranteId) }
                )
            }

            is MenuUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    item {
                        MenuHeader(
                            restaurante = state.restaurante,
                            onBack = { navController.popBackStack() },
                            onStarClick = { abrirStar = !abrirStar }
                        )
                    }

                    if (state.ofertas.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏷️", fontSize = 18.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Ofertas destacadas",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.height(130.dp)
                                ) {
                                    items(
                                        items = state.ofertas,
                                        key = { it.idProducto }) { oferta ->
                                        OfertaItem(producto = oferta, onClick = {
                                            carritoViewModel.abrirModalNuevoProducto(
                                                producto = oferta,
                                                restaurante = state.restaurante,
                                                state.restaurante.abierto ?: false
                                            )
                                        })
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    stickyHeader {
                        Surface(
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 12.dp, vertical = 1.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector = Icons.Filled.RestaurantMenu,
                                        contentDescription = "Categorías de comida",
                                        tint = TregoSecondary
                                    )

                                    state.categorias.forEach { categoria ->
                                        FilterChip(
                                            selected = state.categoriaSeleccionada == categoria,
                                            onClick = { menuViewModel.selectCategoria(categoria) },
                                            label = {
                                                Text(
                                                    categoria,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF1E2A3A),
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }


                                if (state.subcategoriasDisponibles.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DinnerDining,
                                            contentDescription = "Platos de comida",
                                            tint = ComboGreenDark
                                        )
                                        state.subcategoriasDisponibles.forEach { subcategoria ->
                                            FilterChip(
                                                selected = state.subcategoriaSeleccionada == subcategoria,
                                                onClick = {
                                                    menuViewModel.selectSubcategoria(
                                                        subcategoria
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = subcategoria,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = onCancelar,
                                                    selectedLabelColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (state.categoriaSeleccionada == "Todos") "Todos los productos" else state.categoriaSeleccionada,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (state.categoriaSeleccionada == "Todos") 14.sp else 18.sp,
                            color = if (state.categoriaSeleccionada == "Todos") Color.Unspecified else TregoOrange,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                            )
                        )
                    }

                    if (state.productosFiltrados.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay productos disponibles",
                                    color = Color.Gray,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    } else {
                        when {
                            state.subcategoriaSeleccionada != "Todos" -> {
                                item {
                                    Text(
                                        text = state.subcategoriaSeleccionada,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(
                                            start = 22.dp,
                                            top = 6.dp,
                                            bottom = 8.dp
                                        )
                                    )
                                }

                                items(
                                    items = state.productosFiltrados,
                                    key = { it.idProducto }
                                ) { producto ->
                                    ProductoItem(
                                        producto = producto,
                                        onAgregar = {
                                            carritoViewModel.abrirModalNuevoProducto(
                                                producto = producto,
                                                restaurante = state.restaurante,
                                                state.restaurante.abierto ?: false
                                            )
                                        })
                                }
                            }

                            state.categoriaSeleccionada != "Todos" -> {
                                val productosAgrupadosPorSub =
                                    state.productosFiltrados.groupBy {
                                        it.subCategoria?.nombre ?: "Otros"
                                    }

                                productosAgrupadosPorSub.forEach { (nombreSubcategoria, productosSub) ->
                                    item {
                                        Text(
                                            text = nombreSubcategoria,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(
                                                start = 24.dp,
                                                top = 2.dp,
                                                bottom = 2.dp
                                            )
                                        )
                                    }

                                    items(
                                        items = productosSub,
                                        key = { it.idProducto!! }) { producto ->
                                        ProductoItem(
                                            producto = producto,
                                            onAgregar = {
                                                carritoViewModel.abrirModalNuevoProducto(
                                                    producto = producto,
                                                    restaurante = state.restaurante,
                                                    state.restaurante.abierto ?: false
                                                )
                                            })
                                    }
                                }
                            }

                            else -> {
                                val productosPorCategoria = state.productosFiltrados.groupBy {
                                    it.categoria?.name ?: "Otros"
                                }
                                productosPorCategoria.forEach { (nombreCategoria, productosDeLaCategoria) ->
                                    item {
                                        Text(
                                            text = nombreCategoria.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = TregoOrange,
                                            modifier = Modifier.padding(
                                                start = 24.dp,
                                                top = 8.dp,
                                                bottom = 2.dp
                                            )
                                        )
                                    }

                                    val productosPorSubcategoria =
                                        productosDeLaCategoria.groupBy {
                                            it.subCategoria?.nombre ?: "Otros"
                                        }

                                    productosPorSubcategoria.forEach { (nombreSubcategoria, productosFinales) ->
                                        item {
                                            Text(
                                                text = nombreSubcategoria,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(
                                                    start = 32.dp,
                                                    bottom = 2.dp
                                                )
                                            )
                                        }

                                        // Lista de productos final
                                        items(
                                            items = productosFinales,
                                            key = { it.idProducto!! }) { producto ->
                                            ProductoItem(
                                                producto = producto,
                                                onAgregar = {
                                                    carritoViewModel.abrirModalNuevoProducto(
                                                        producto = producto,
                                                        restaurante = state.restaurante,
                                                        state.restaurante.abierto ?: false
                                                    )
                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ResenasHeader(
                            resenas = state.resenas,
                            promedio = state.restaurante.calificacionProm ?: 0f,
                            onClick = { abrirStar = !abrirStar }
                        )
                    }

                    itemsIndexed(
                        items = state.resenas,
                        key = { index, r -> "${r.nombreCliente}_${r.fechaCreacion}_$index" }
                    ) { index, resena ->
                        ResenaCard(resena = resena)
                        if (index < state.resenas.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFF5F5F5)
                            )
                        } else {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFF5F5F5)
                            )
                        }
                    }

                    item { Spacer(Modifier.height(48.dp)) }
                }

                if (abrirStar) {
                    CalificacionModal(
                        nombreResto = state.restaurante.nombre,
                        onDismiss = { abrirStar = false },
                        onConfirm = { rating, comentario ->
                            menuViewModel.enviarResena(restauranteId, rating, comentario)
                            abrirStar = false
                        }
                    )
                }
            }
        }
    }
}
