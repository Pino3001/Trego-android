package com.grupo6.trego.ui.home.restaurantes

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.home.componentes.FilterBottomSheet
import com.grupo6.trego.ui.home.componentes.LoadingPlaceholder
import com.grupo6.trego.ui.home.restaurantes.componentes.RestaurantItem
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListScreen(
    locationState: LocationState,
    currentAddress: DTODireccion?,
    onRetryLocation: () -> Unit,
    onRestaurantClick: (Long) -> Unit,
) {
    val viewModel: RestauranteViewModel = koinViewModel()
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val state = rememberPullToRefreshState()
    // Controla si ya se realizó la carga inicial con éxito para no repetir jamás
    var isInitialLoadDone by remember { mutableStateOf(false) }
    val addressSearchState = viewModel.addressSearchUiState

    // Sincronizar ubicación con el ViewModel de Restaurantes (Solo reacciona a cambios manuales de dirección)
    LaunchedEffect(currentAddress, locationState) {
        // Si ya hicimos la carga inicial y no hay un cambio de dirección manual, ignoramos cualquier actualización del GPS
        if (isInitialLoadDone && currentAddress == null) return@LaunchedEffect

        val targetLat: Double
        val targetLon: Double
        val targetAddress: DTODireccion

        if (currentAddress != null) {
            targetLat = currentAddress.latitud
            targetLon = currentAddress.longitud
            targetAddress = currentAddress
        } else if (locationState is LocationState.Available) {
            targetLat = locationState.lat
            targetLon = locationState.lon
            targetAddress = DTODireccion(latitud = targetLat, longitud = targetLon)
        } else {
            return@LaunchedEffect // Espera hasta que el GPS entregue el primer valor válido
        }

        // Se ejecuta la carga inicial o el cambio manual
        isInitialLoadDone = true
        viewModel.updateLocation(targetLat, targetLon)
        if (!viewModel.isSearchMode) {
            viewModel.searchRestaurantsByAddress(targetAddress)
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    containerColor = BlancoCard,
                    color = TregoOrange,
                    state = state
                )
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Buscador y Filtros
                item {
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
                                query = viewModel.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChange,
                                onClear = viewModel::onClearSearch,
                                onSearch = viewModel::onSearchSubmit,
                                placeholderText = "Buscar Restaurante",
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

                // AHORA USAMOS !isInitialLoadDone en lugar de !hasLoadedData
                if ((locationState is LocationState.Idle || locationState is LocationState.RequestingPermission) && !isInitialLoadDone) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingPlaceholder("Obteniendo tu ubicación...")
                        }
                    }
                } else if (locationState is LocationState.PermissionDenied && !isInitialLoadDone) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            VistaEstado(
                                titulo = "Ubicación denegada",
                                mensaje = "Permiso de ubicación denegado. Habilitalo desde la configuración para ver restaurantes cercanos.",
                                icono = Icons.Default.LocationOff,
                                colorIcono = Color.Gray,
                                botonTexto = "Ir a Configuración",
                                onAccion = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                    )
                                }
                            )
                        }
                    }
                } else if (locationState is LocationState.LocationUnavailable && !isInitialLoadDone) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            VistaEstado(
                                titulo = "Ubicación no disponible",
                                mensaje = "No pudimos obtener tu ubicación. Asegurate de tener el GPS activado.",
                                icono = Icons.Default.LocationOff,
                                colorIcono = Color.Gray,
                                botonTexto = "Reintentar",
                                onAccion = {
                                    // Si el usuario pide reintentar explícitamente, reseteamos el control para permitir una nueva carga
                                    isInitialLoadDone = false
                                    onRetryLocation()
                                }
                            )
                        }
                    }
                } else {
                    if (viewModel.isSearchMode) {
                        when (val searchState = viewModel.searchUiState) {
                            SearchUiState.Loading -> {
                                if (!isRefreshing) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillParentMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            LoadingPlaceholder("Buscando restaurantes...")
                                        }
                                    }
                                }
                            }

                            is SearchUiState.Success -> {
                                items(searchState.restaurants) { restaurant ->
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

                            is SearchUiState.Error -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        VistaError(
                                            mensaje = searchState.message,
                                            onReintentar = { viewModel.onSearchSubmit() }
                                        )
                                    }
                                }
                            }

                            SearchUiState.Empty -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        VistaEstado(
                                            titulo = "Sin Resultados",
                                            mensaje = "No se encontraron restaurantes con ese nombre.",
                                            icono = Icons.Default.SearchOff,
                                            colorIcono = Color.Gray,
                                            onAccion = null
                                        )
                                    }
                                }
                            }

                            SearchUiState.Idle -> Unit
                        }
                    } else if (viewModel.isAddressSearchMode) {
                        when (val addressState = addressSearchState) {
                            AddressSearchUiState.Loading -> {
                                if (!isRefreshing) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillParentMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            LoadingPlaceholder("Cargando restaurantes cercanos...")
                                        }
                                    }
                                }
                            }

                            is AddressSearchUiState.Success -> {
                                items(addressState.restaurants) { restaurant ->
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

                            is AddressSearchUiState.Error -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        VistaError(
                                            mensaje = addressState.message,
                                            onReintentar = { viewModel.refresh() }
                                        )
                                    }
                                }
                            }

                            AddressSearchUiState.Empty -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        VistaEstado(
                                            titulo = "Zona sin cobertura",
                                            mensaje = "Lo sentimos, no hay restaurantes disponibles en tu zona actualmente.",
                                            icono = Icons.Default.Map,
                                            colorIcono = Color.Gray,
                                            onAccion = null
                                        )
                                    }
                                }
                            }

                            AddressSearchUiState.Idle -> Unit
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                VistaEstado(
                                    titulo = "Todo listo",
                                    mensaje = "Busca un restaurante por nombre o usa tu ubicación.",
                                    icono = Icons.Default.Search,
                                    colorIcono = Color.Gray,
                                    onAccion = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}