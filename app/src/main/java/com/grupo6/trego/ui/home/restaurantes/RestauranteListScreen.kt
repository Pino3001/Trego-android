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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.grupo6.trego.ui.home.componentes.RestaurantSimpleList
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListScreen(
    locationState: LocationState,
    onRetryLocation: () -> Unit,
    onRestaurantClick: (Long) -> Unit,
) {
    val viewModel: RestauranteViewModel = koinViewModel()
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Sincronizar ubicación del HomePage con el ViewModel de Restaurantes
    LaunchedEffect(locationState) {
        if (locationState is LocationState.Available) {
            viewModel.updateLocation(locationState.lat, locationState.lon)
            // Disparamos la búsqueda por dirección automáticamente si no estamos en búsqueda por nombre
            if (!viewModel.isSearchMode) {
                viewModel.searchRestaurantsByAddress(
                    DTODireccion(
                        latitud = locationState.lat,
                        longitud = locationState.lon
                    )
                )
            }
        }
    }

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
                                        data = Uri.fromParts(
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
                            onAccion = onRetryLocation
                        )
                    }

                    is LocationState.Available -> {
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
                        }
                    }
                }
            }
        }
    }
}
