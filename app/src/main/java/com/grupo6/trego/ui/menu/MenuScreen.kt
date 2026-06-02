package com.grupo6.trego.ui.menu

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.grupo6.trego.ui.menu.componentes.MenuEmpty
import com.grupo6.trego.ui.menu.componentes.MenuHeader
import com.grupo6.trego.ui.menu.componentes.OfertaItem
import com.grupo6.trego.ui.menu.componentes.ProductoItem
import com.grupo6.trego.ui.tabs.NavigationTabs
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MenuScreen(
    restauranteId: Long,
    navController: NavController,
) {
    val menuViewModel: MenuViewModel = koinViewModel()
    val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()

    val activity = LocalContext.current as? ComponentActivity
        ?: error("ComponentActivity no disponible")
    val carritoViewModel: CarritoViewModel = koinViewModel(viewModelStoreOwner = activity)

    LaunchedEffect(restauranteId) {
        menuViewModel.loadMenu(restauranteId)
    }

    // Scaffold SIN topBar para permitir inmersión total de la foto
    Scaffold(
        containerColor = Color.White,
    ) { innerPadding ->

        // Modal carrito (Permanece igual)
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
            is MenuUiState.SinProductos -> MenuEmpty(onVolver = { navController.popBackStack() })
            is MenuUiState.Error -> MenuEmpty(mensaje = state.message, onVolver = { navController.popBackStack() })

            is MenuUiState.Success -> {
                // (Mantén aquí tu lógica del AlertDialog ordenPrecio tal cual la tienes)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        // 🌟 MAGIA EDGE-TO-EDGE: Solo aplicamos el padding inferior para evitar la navbar
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    // 1. Imagen inmersiva
                    item {
                        MenuHeader(
                            restaurante = state.restaurante,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 2. Ofertas (Debajo de la foto, antes de los filtros)
                    if (state.ofertas.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏷️", fontSize = 18.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ofertas destacadas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.height(130.dp)
                                ) {
                                    items(items = state.ofertas, key = { it.idProducto!! }) { oferta ->
                                        OfertaItem(producto = oferta, onClick = {
                                            carritoViewModel.abrirModalNuevoProducto(
                                                productoSimplificado = oferta.toSimplificado(),
                                                restaurante = state.restaurante
                                            )
                                        })
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    // 3. BARRA DE FILTROS PEGADIZA (Sticky Header)
                    stickyHeader {
                        Surface( // Surface para darle fondo sólido cuando pase por encima de los productos
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = menuViewModel::showOrdenDialog,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.ordenPrecio != OrdenPrecio.NINGUNO) Color(0xFF1E2A3A) else TregoOrange
                                    ),
                                    shape = RoundedCornerShape(8.dp), // Forma más cuadrada/moderna
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (state.ordenPrecio == OrdenPrecio.NINGUNO) "Ordenar" else "Filtro activo",
                                        fontSize = 13.sp
                                    )
                                }

                                // Línea separadora
                                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.LightGray))

                                state.categorias.forEach { categoria ->
                                    FilterChip(
                                        selected = state.categoriaSeleccionada == categoria,
                                        onClick = { menuViewModel.selectCategoria(categoria) },
                                        label = { Text(categoria, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF1E2A3A),
                                            selectedLabelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Lista de Productos
                    item {
                        Text(
                            text = if (state.categoriaSeleccionada == "Todos") "Todos los productos" else state.categoriaSeleccionada,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                        )
                    }

                    if (state.productosFiltrados.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No hay productos en esta categoría", color = Color.Gray, fontSize = 15.sp)
                            }
                        }
                    } else {
                        items(items = state.productosFiltrados, key = { it.idProducto!! }) { producto ->
                            ProductoItem(
                                producto = producto,
                                onAgregar = {
                                    carritoViewModel.abrirModalNuevoProducto(
                                        productoSimplificado = producto.toSimplificado(),
                                        restaurante = state.restaurante
                                    )
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) } // Margen holgado al final
                }
            }
        }
    }
}