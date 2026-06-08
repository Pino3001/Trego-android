package com.grupo6.trego.ui.restaurantes

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.itemKey
import com.grupo6.trego.R
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.data.utilities.RequestLocation
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.restaurantes.componentes.FilterBottomSheet
import com.grupo6.trego.ui.restaurantes.componentes.RestaurantItem
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListScreen(
    onRestaurantClick: (Long) -> Unit,
) {
    val viewModel: RestauranteViewModel = koinViewModel()
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    val locationState by viewModel.locationState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Incrementar este contador fuerza a RequestLocation a re-ejecutarse
    var locationRetryKey by remember { mutableStateOf(0) }

    RequestLocation(retryKey = locationRetryKey) { state ->
        when (state) {
            is LocationState.Available -> {
                viewModel.onLocationAvailable(state.lat, state.lon)
                // Disparamos la búsqueda por dirección automáticamente
                viewModel.searchRestaurantsByAddress(
                    DTODireccion(
                        latitud = state.lat,
                        longitud = state.lon
                    )
                )
            }

            LocationState.LocationUnavailable ->
                viewModel.onLocationUnavailable()

            LocationState.PermissionDenied ->
                viewModel.onPermissionDenied()

            LocationState.RequestingPermission ->
                viewModel.onRequestingPermission()

            LocationState.Idle -> Unit
        }
    }

    // Paginación (Se mantiene comentada por si se requiere volver a ella)
    // val lazyRestaurantItems = viewModel.restaurantsFlow.collectAsLazyPagingItems()
    val addressSearchState = viewModel.addressSearchUiState

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = viewModel.filterState,
            onApply = {
                viewModel.onApplyFilter(it)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. La cabecera genérica con el logo y nombre
                TregoHeader(
                    title = "TREGO",
                    modifier = Modifier.padding(bottom = 0.dp), // Ajuste fino
                    bottomContent = {
                        Image(
                            painter = painterResource(id = R.drawable.tregologo),
                            contentDescription = "Logo Trego",
                            modifier = Modifier.size(80.dp).padding(top = 4.dp)
                        )
                    }
                )

                // 2. La barra de búsqueda (que vive en la zona blanca)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClear = viewModel::onClearSearch,
                        placeholderText = "Buscar Restaurante",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtros",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (locationState) {

                    LocationState.Idle,
                    LocationState.RequestingPermission -> {
                        LoadingPlaceholder("Obteniendo tu ubicación...")
                    }

                    LocationState.PermissionDenied -> {
                        VistaEstado(
                            titulo = "Ubicación denegada",
                            mensaje = "Permiso de ubicación denegado. Habilitalo desde la configuración para ver restaurantes cercanos.",
                            icono = Icons.Default.LocationOff,
                            colorIcono = Color.Gray,
                            botonTexto = "Ir a Configuración",
                            onAccion = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = android.net.Uri.fromParts(
                                            "package",
                                            context.packageName,
                                            null
                                        )
                                    }
                                )
                            }
                        )
                    }

                    LocationState.LocationUnavailable -> {
                        VistaEstado(
                            titulo = "Ubicación no disponible",
                            mensaje = "No pudimos obtener tu ubicación. Asegurate de tener el GPS activado.",
                            icono = Icons.Default.LocationOff,
                            colorIcono = Color.Gray,
                            botonTexto = "Reintentar",
                            onAccion = { locationRetryKey++ }
                        )
                    }

                    is LocationState.Available -> {
                        // Agregamos la condición para saber si estamos buscando por nombre
                        if (viewModel.isSearchMode) {
                            when (val searchState = viewModel.searchUiState) {
                                SearchUiState.Loading -> {
                                    LoadingPlaceholder("Buscando restaurantes...")
                                }

                                is SearchUiState.Success -> {
                                    RestaurantSimpleList(
                                        restaurants = searchState.restaurants,
                                        onRestaurantClick = onRestaurantClick
                                    )
                                }

                                is SearchUiState.Error -> {
                                    VistaError(
                                        mensaje = searchState.message,
                                        onReintentar = { viewModel.onSearchSubmit() }
                                    )
                                }

                                SearchUiState.Empty -> {
                                    VistaEstado(
                                        titulo = "Sin Resultados",
                                        mensaje = "No se encontraron restaurantes con ese nombre.",
                                        icono = Icons.Default.SearchOff,
                                        colorIcono = Color.Gray,
                                        onAccion = null
                                    )
                                }
                                SearchUiState.Idle -> Unit
                            }
                        } else if (viewModel.isAddressSearchMode) {
                            // Resultados de la búsqueda por dirección (searchRestaurantsByAddress)
                            when (addressSearchState) {
                                AddressSearchUiState.Loading -> {
                                    LoadingPlaceholder("Cargando restaurantes cercanos...")
                                }

                                is AddressSearchUiState.Success -> {
                                    RestaurantSimpleList(
                                        restaurants = addressSearchState.restaurants,
                                        onRestaurantClick = onRestaurantClick
                                    )
                                }

                                is AddressSearchUiState.Error -> {
                                    VistaError(
                                        mensaje = addressSearchState.message,
                                        onReintentar = { viewModel.refresh() }
                                    )
                                }

                                AddressSearchUiState.Empty -> {
                                    VistaEstado(
                                        titulo = "Zona sin cobertura",
                                        mensaje = "Lo sentimos, no hay restaurantes disponibles en tu zona actualmente.",
                                        icono = Icons.Default.Map,
                                        colorIcono = Color.Gray,
                                        onAccion = null
                                    )
                                }
                                AddressSearchUiState.Idle -> Unit
                            }
                        } else {
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composable: placeholder de carga con mensaje
// ---------------------------------------------------------------------------

@Composable
private fun BoxScope.LoadingPlaceholder(message: String) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = TregoOrange)
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---------------------------------------------------------------------------
// Sub-composable: lista simple (no paginada)
// ---------------------------------------------------------------------------

@Composable
private fun RestaurantSimpleList(
    restaurants: List<DTORestaurante>,
    onRestaurantClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantItem(
                restaurant = restaurant,
                onClick = { onRestaurantClick(restaurant.idRestaurante?.toLong() ?: 0L) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composable: lista paginada
// ---------------------------------------------------------------------------

@Composable
private fun RestaurantPagedList(
    lazyItems: androidx.paging.compose.LazyPagingItems<com.grupo6.trego.data.model.DTORestaurante>,
    onRestaurantClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            count = lazyItems.itemCount,
            key = lazyItems.itemKey { it.idRestaurante!! }
        ) { index ->
            lazyItems[index]?.let { restaurant ->
                RestaurantItem(
                    restaurant = restaurant,
                    onClick = {
                        onRestaurantClick(
                            restaurant.idRestaurante?.toLong() ?: 0L
                        )
                    }
                )
            }
        }

        if (lazyItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TregoOrange
                    )
                }
            }
        }

        if (lazyItems.loadState.append is LoadState.Error) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { lazyItems.retry() }) {
                        Text("Error al cargar más. Reintentar", color = TregoOrange)
                    }
                }
            }
        }
    }
}
