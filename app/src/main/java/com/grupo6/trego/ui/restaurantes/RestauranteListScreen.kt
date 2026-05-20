package com.grupo6.trego.ui.restaurantes

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.restaurantes.componentes.EmptyState
import com.grupo6.trego.ui.restaurantes.componentes.FilterBottomSheet
import com.grupo6.trego.ui.restaurantes.componentes.RestaurantItem
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.R
import com.grupo6.trego.ui.tabs.NavigationTabs

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListScreen(
    viewModel: RestaurantListViewModel = viewModel(),
    onRestaurantClick: (Long) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var showFilterSheet by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    // Pedir ubicación al iniciar
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.onLocationAvailable(location.latitude, location.longitude)
                } else {
                    // lastLocation puede ser null en emulador o GPS frío
                    // Cargamos el mock directo como fallback
                    viewModel.loadRestaurants(0.0, 0.0)
                }
            }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    // Diálogo de ubicación desactivada
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = {
                showLocationDialog = false
                viewModel.onLocationDisabled()
            },
            title = { Text("Activar ubicación") },
            text = { Text("Para ver restaurantes en tu zona necesitamos acceder a tu ubicación.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationDialog = false
                    locationPermission.launchPermissionRequest()
                }) {
                    Text("Activar", color = TregoOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLocationDialog = false
                    viewModel.onLocationDisabled()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // BottomSheet de filtros
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = viewModel.filterState,
            onApply = { viewModel.onApplyFilter(it) },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        // Top bar, hay que sacarlo para un componente independiente
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Header naranja con logo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp)
                        .background(TregoOrange),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center

                ) {
                    Text(
                        "TREGO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Image(
                        painter = painterResource(id = R.drawable.tregologo),
                        contentDescription = "Logo de la aplicación Trego", // Importante para accesibilidad (alt text)
                        modifier = Modifier
                            .size(110.dp)
                    )
                }

                // Barra de búsqueda + acciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClear = viewModel::onClearSearch,
                        placeholderText = "Buscar Restaurante",
                        // Le pasamos el weight(1f) aquí para que ocupe el espacio disponible si está al lado de un botón
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))
                    // Filtros
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filtros", tint = TregoOrange)
                    }
                    // Recargar
                    IconButton(onClick = {
                        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                viewModel.loadRestaurants(it.latitude, it.longitude)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = TregoOrange)
                    }
                }
            }
        },
        bottomBar = {
                // 💡 Agregamos las pestañas abajo en la barra del Scaffold
            NavigationTabs(navController = navController, currentRoute = "restaurants")
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            when (val state = viewModel.uiState) {
                is RestaurantUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TregoOrange
                    )
                }

                is RestaurantUiState.LocationDisabled -> {
                    EmptyState("Activá tu ubicación para ver restaurantes")
                }

                is RestaurantUiState.Empty -> {
                    EmptyState("No hay nada para mostrar")
                }

                is RestaurantUiState.Error -> {
                    EmptyState(state.message)
                }

                is RestaurantUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.restaurants) { restaurant ->
                            RestaurantItem(
                                restaurant = restaurant,
                                onClick = { onRestaurantClick(restaurant.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}