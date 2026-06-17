package com.grupo6.trego.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo6.trego.R
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.data.utilities.RequestLocation
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.home.ofertas.OfertaScreen
import com.grupo6.trego.ui.home.platos.PlatoListScreen
import com.grupo6.trego.ui.home.platos.SubCategoriaListScreen
import com.grupo6.trego.ui.home.restaurantes.RestaurantListScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(navController: NavController) {
    val viewModel: HomeViewModel = koinViewModel()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val locationState by viewModel.locationState.collectAsState()
    var locationRetryKey by remember { mutableStateOf(0) }

    // Componente centralizado de localización
    RequestLocation(retryKey = locationRetryKey) { state ->
        when (state) {
            is LocationState.Available -> viewModel.onLocationAvailable(state.lat, state.lon)
            LocationState.LocationUnavailable -> viewModel.onLocationUnavailable()
            LocationState.PermissionDenied -> viewModel.onPermissionDenied()
            LocationState.RequestingPermission -> viewModel.onRequestingPermission()
            LocationState.Idle -> Unit
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TregoHeader(
                    title = "TREGO",
                    modifier = Modifier.padding(bottom = 0.dp),
                    bottomContent = {
                        Image(
                            painter = painterResource(id = R.drawable.tregologo),
                            contentDescription = "Logo Trego",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(top = 4.dp)
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> RestaurantListScreen(
                        locationState = locationState,
                        onRetryLocation = { locationRetryKey++ },
                        onRestaurantClick = { id -> navController.navigate("menu/$id") }
                    )

                    1 -> {
                        var selectedSub by remember { mutableStateOf<DTOSubCategoria?>(null) }

                        if (selectedSub == null) {
                            SubCategoriaListScreen(
                                onSubCategoriaClick = { selectedSub = it }
                            )
                        } else {
                            // Construimos la dirección desde el estado global de ubicación
                            val dir = if (locationState is LocationState.Available) {
                                val available = locationState as LocationState.Available
                                DTODireccion(
                                    latitud = available.lat,
                                    longitud = available.lon
                                )
                            } else {
                                DTODireccion()
                            }

                            PlatoListScreen(
                                subCategoria = selectedSub!!,
                                direccion = dir,
                                navController = navController,
                                onBack = { selectedSub = null }
                            )
                        }
                    }

                    2 -> {
                        val dir = if (locationState is LocationState.Available) {
                            val available = locationState as LocationState.Available
                            DTODireccion(
                                latitud = available.lat,
                                longitud = available.lon
                            )
                        } else {
                            DTODireccion()
                        }
                        OfertaScreen(
                            direccion = dir,
                            navController = navController,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}
