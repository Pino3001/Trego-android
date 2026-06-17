package com.grupo6.trego.ui.home.platos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.home.componentes.LoadingPlaceholder
import com.grupo6.trego.ui.home.platos.componentes.CardPlato
import com.grupo6.trego.ui.home.platos.componentes.PlatoFilterBottomSheet
import com.grupo6.trego.ui.menu.MenuUiEvent
import com.grupo6.trego.ui.menu.MenuViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatoListScreen(
    subCategoria: DTOSubCategoria,
    direccion: DTODireccion,
    navController: NavController,
    onBack: () -> Unit
) {
    val viewModel: PlatoViewModel = koinViewModel()
    val activity = LocalContext.current as? ComponentActivity
        ?: error("ComponentActivity no disponible")
    val menuViewModel: MenuViewModel = koinViewModel(viewModelStoreOwner = activity)

    var showFilterSheet by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState

    // Cargar platos al iniciar o cuando cambie la subcategoría/dirección
    LaunchedEffect(subCategoria, direccion) {
        viewModel.loadPlatos(subCategoria, direccion)
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

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }

                    SearchBar(
                        query = viewModel.restaurantQuery,
                        onQueryChange = viewModel::onRestaurantQueryChange,
                        onClear = { viewModel.onRestaurantQueryChange("") },
                        placeholderText = "Buscar restaurante...",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(4.dp))

                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtros",
                            tint = Color.Black
                        )
                    }
                }
                
                Text(
                    text = subCategoria.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp) .fillMaxWidth(),
                    textAlign = TextAlign.Center,

                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.loadPlatos(subCategoria, direccion) },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is PlatoUIState.Loading -> {
                        LoadingPlaceholder("Cargando platos...")
                    }
                    is PlatoUIState.Empty -> {
                        VistaEstado(
                            titulo = "Sin resultados",
                            mensaje = "No se encontraron platos que coincidan con los filtros aplicados.",
                            icono = Icons.Default.SearchOff,
                            colorIcono = Color.Gray,
                            onAccion = null
                        )
                    }
                    is PlatoUIState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.platos) { productoZona ->
                                CardPlato(
                                    productoZona = productoZona,
                                    onClick = { menuViewModel.abrirMenuConProducto(productoZona.producto) }
                                )
                            }
                        }
                    }
                    is PlatoUIState.Error -> {
                        VistaError(
                            mensaje = uiState.message,
                            onReintentar = { viewModel.loadPlatos(subCategoria, direccion) }
                        )
                    }
                    is PlatoUIState.Idle -> Unit
                }
            }
        }
    }
}
