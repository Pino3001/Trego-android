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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.home.componentes.LoadingPlaceholder
import com.grupo6.trego.ui.home.ofertas.componentes.OfertaListItem
import com.grupo6.trego.ui.home.platos.componentes.CardPlato
import com.grupo6.trego.ui.home.platos.componentes.PlatoFilterBottomSheet
import com.grupo6.trego.ui.menu.MenuUiEvent
import com.grupo6.trego.ui.menu.MenuViewModel
import org.koin.androidx.compose.koinViewModel

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

    var showFilterSheet by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState

    // Cargar platos al iniciar o cuando cambie la subcategoría/dirección
    LaunchedEffect(direccion) {
        viewModel.loadOfertas(direccion)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    text = "🏷 Ofertas del dia",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,

                    )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.loadOfertas(direccion) },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is OfertaUIState.Loading -> {
                        LoadingPlaceholder("Cargando ofertas...")
                    }

                    is OfertaUIState.Empty -> {
                        VistaEstado(
                            titulo = "Sin resultados",
                            mensaje = "No se encontraron ofertas que coincidan con los filtros aplicados.",
                            icono = Icons.Default.SearchOff,
                            colorIcono = Color.Gray,
                            onAccion = null
                        )
                    }

                    is OfertaUIState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.platos) { productoZona ->
                                OfertaListItem(
                                    productoZona = productoZona,
                                    onClick = { menuViewModel.abrirMenuConProducto(productoZona.producto) }
                                )
                            }
                        }
                    }

                    is OfertaUIState.Error -> {
                        VistaError(
                            mensaje = uiState.message,
                            onReintentar = { viewModel.loadOfertas(direccion) }
                        )
                    }

                    OfertaUIState.Idle -> Unit
                }
            }
        }
    }
}
