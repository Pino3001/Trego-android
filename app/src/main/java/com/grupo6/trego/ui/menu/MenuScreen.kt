package com.grupo6.trego.ui.menu

import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.grupo6.trego.ui.menu.componentes.*
import com.grupo6.trego.ui.tabs.NavigationTabs
import com.grupo6.trego.ui.theme.TregoOrange
import androidx.navigation.NavController
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.carrito.componentes.ProductoDetalleModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    restauranteId: Long,
    navController: NavController,
    viewModel: MenuViewModel = viewModel(),
) {
/*    val activity = LocalContext.current as ComponentActivity
    val carritoViewModel: CarritoViewModel = viewModel(viewModelStoreOwner = activity)

    LaunchedEffect(restauranteId) {
        viewModel.cargarMenu(restauranteId)
    }

    // Dialog ordenar por precio
    if (viewModel.showOrdenDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissOrdenDialog,
            title = { Text("Ordenar por precio") },
            text = {
                Column {
                    TextButton(onClick = { viewModel.onOrdenSeleccionado(OrdenPrecio.MENOR) }) {
                        Text("Menor precio")
                    }
                    TextButton(onClick = { viewModel.onOrdenSeleccionado(OrdenPrecio.MAYOR) }) {
                        Text("Mayor precio")
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                NavigationTabs(navController = navController, currentRoute = "restaurants")
            }
        }
    ) { innerPadding ->
        if (carritoViewModel.showModal && carritoViewModel.productoEnModal != null) {
            ProductoDetalleModal(
                item = carritoViewModel.productoEnModal!!,
                onConfirmar = { carritoViewModel.confirmarModal(it) },
                onDismiss = { carritoViewModel.cerrarModal() }
            )
        }

        when (val state = viewModel.uiState) {

            is MenuUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TregoOrange)
                }
            }

            is MenuUiState.SinProductos -> {
                MenuEmpty(onVolver = { navController.popBackStack() })
            }

            is MenuUiState.Error -> {
                MenuEmpty(
                    mensaje = state.message,
                    onVolver = { navController.popBackStack() }
                )
            }

            is MenuUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Header naranja
                    item {
                        MenuHeader(
                            restaurante = state.restaurante,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Filtros de categoría + botón ordenar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            viewModel.categorias.forEach { categoria ->
                                FilterChip(
                                    selected = viewModel.categoriaSeleccionada == categoria,
                                    onClick = { viewModel.onCategoriaSeleccionada(categoria) },
                                    label = { Text(categoria, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1E2A3A),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            // Botón ordenar
                            Button(
                                onClick = viewModel::onShowOrdenDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Ordenar", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    // Sección ofertas
                    if (viewModel.ofertas.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏷️", fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Ofertas del dia",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(viewModel.ofertas) { oferta ->
                                        OfertaItem(producto = oferta)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    // Título lista general
                    item {
                        Text(
                            text = if (viewModel.categoriaSeleccionada == "Todos")
                                "Todos los Productos"
                            else
                                viewModel.categoriaSeleccionada,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Lista de productos filtrados
                    if (viewModel.productosFiltrados.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay productos disponibles en esta categoría",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(viewModel.productosFiltrados) { producto ->
                            ProductoItem(
                                producto = producto,
                                onAgregar = {
                                    carritoViewModel.abrirModalNuevoProducto(
                                        producto = producto,
                                        restaurante = state.restaurante.nombre
                                    )
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }*/
}