package com.grupo6.trego.ui.home.ofertas

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.utilities.TokenManager
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.home.componentes.LoadingPlaceholder
import com.grupo6.trego.ui.home.ofertas.componentes.OfertaListItem
import com.grupo6.trego.ui.home.platos.componentes.PlatoFilterBottomSheet
import com.grupo6.trego.ui.menu.MenuUiEvent
import com.grupo6.trego.ui.menu.MenuViewModel
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfertaScreen(
    direccion: DTODireccion,
    navController: NavController,
) {
    val viewModel: OfertaViewModel = koinViewModel()
    val activity = (LocalContext.current as? ComponentActivity)
        ?: error("ComponentActivity no disponible")
    val menuViewModel: MenuViewModel = koinViewModel(viewModelStoreOwner = activity)
    val state = rememberPullToRefreshState()
    var showFilterSheet by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState

    val tokenManager: TokenManager = koinInject()
    val tokenReady by tokenManager.isTokenAvailable.collectAsState()

    // Cargar ofertas sólo si el token y las coordenadas son válidas
    LaunchedEffect(direccion, tokenReady) {
        if (tokenReady && (direccion.latitud != 0.0 || direccion.longitud != 0.0) &&
            direccion.latitud in -90.0..90.0 && direccion.longitud in -180.0..180.0
        ) {
            viewModel.loadOfertas(direccion)
        }
    }


    LaunchedEffect(Unit) {
        menuViewModel.uiEvent.collect { event ->
            if (event is MenuUiEvent.NavigateToMenu) {
                navController.navigate("menu/${event.restauranteId}")
            }
        }
    }

    if (showFilterSheet) {
        PlatoFilterBottomSheet(
            currentRating = viewModel.minRating,
            currentSortOrder = viewModel.sortOrder,
            onApply = { rating, sort ->
                viewModel.onRatingFilterChange(rating)
                viewModel.onSortOrderChange(sort)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.loadOfertas(direccion) },
            modifier = Modifier.fillMaxSize(),
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = viewModel.isRefreshing,
                    containerColor = BlancoCard,
                    color = TregoOrange,
                    state = state
                )
            }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Buscador y Filtros
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = TregoOrange,
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 12.dp,
                                        topEnd = 0.dp,
                                        bottomEnd = 50.dp
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SearchBar(
                                query = viewModel.restaurantQuery,
                                onQueryChange = viewModel::onRestaurantQueryChange,
                                onClear = { viewModel.onRestaurantQueryChange("") },
                                placeholderText = "Buscar restaurante...",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color.White,
                                showBorder = false
                            )

                            Spacer(Modifier.width(8.dp))

                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filtros",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                if (uiState is OfertaUIState.Success) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "🏷 Ofertas del dia",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                when (uiState) {
                    is OfertaUIState.Loading -> {
                        if (!viewModel.isRefreshing) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingPlaceholder("Cargando ofertas...")
                                }
                            }
                        }
                    }

                    is OfertaUIState.Empty -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                VistaEstado(
                                    titulo = "Sin resultados",
                                    mensaje = "No se encontraron ofertas que coincidan con los filtros aplicados.",
                                    icono = Icons.Default.SearchOff,
                                    colorIcono = Color.Gray,
                                    onAccion = null
                                )
                            }
                        }
                    }

                    is OfertaUIState.NoCoverage -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                VistaEstado(
                                    titulo = "Zona sin cobertura",
                                    mensaje = "Lo sentimos, no hay ofertas disponibles en tu zona actualmente.",
                                    icono = Icons.Default.Map,
                                    colorIcono = Color.Gray,
                                    onAccion = null
                                )
                            }
                        }
                    }

                    is OfertaUIState.Success -> {
                        items(uiState.platos) { productoZona ->
                            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                OfertaListItem(
                                    productoZona = productoZona,
                                    onClick = { menuViewModel.abrirMenuConProducto(productoZona.producto) }
                                )
                            }
                        }
                    }

                    is OfertaUIState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                VistaError(
                                    mensaje = uiState.message,
                                    onReintentar = { viewModel.loadOfertas(direccion) }
                                )
                            }
                        }
                    }

                    OfertaUIState.Idle -> Unit
                }
            }
        }
    }
}
