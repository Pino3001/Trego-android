package com.grupo6.trego.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.utilities.AppReadyState
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.data.utilities.RequestLocation
import com.grupo6.trego.ui.carrito.componentes.DireccionSelectorModal
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
    var selectedSub by remember { mutableStateOf<DTOSubCategoria?>(null) }
    val locationState by viewModel.locationState.collectAsState()
    var locationRetryKey by remember { mutableStateOf(0) }
    val currentAddress by viewModel.currentAddress.collectAsState()
    val isFetchingAddress by viewModel.isFetchingAddress.collectAsState()
    var mostrarSelectorDireccion by remember { mutableStateOf(false) }
    val estadoDirecciones by viewModel.direccionesState.collectAsStateWithLifecycle()
    val direccionesList =
        (estadoDirecciones as? HomeDireccionesState.Cargadas)?.items ?: emptyList()

    LaunchedEffect(mostrarSelectorDireccion) {
        if (mostrarSelectorDireccion) {
            viewModel.cargarDirecciones()
        }
    }

    // Componente centralizado de localización
    RequestLocation(retryKey = locationRetryKey) { state ->
        when (state) {
            is LocationState.Available -> {
                viewModel.onLocationAvailable(state.lat, state.lon)
                AppReadyState.setDataReady(true)
            }

            LocationState.LocationUnavailable -> {
                viewModel.onLocationUnavailable()
                AppReadyState.setDataReady(true)
            }

            LocationState.PermissionDenied -> {
                viewModel.onPermissionDenied()
                AppReadyState.setDataReady(true)
            }

            LocationState.RequestingPermission -> {
                viewModel.onRequestingPermission()
                AppReadyState.setDataReady(true)
            }

            LocationState.Idle -> Unit
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TregoHeader(
                    title = "TREGO",
                    modifier = Modifier.padding(bottom = 0.dp),
                    navigationIcon = if (pagerState.currentPage == 1 && selectedSub != null) {
                        {
                            IconButton(
                                onClick = { selectedSub = null }, modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBackIosNew,
                                    tint = Color.White,
                                    contentDescription = "Volver a subcategorías"
                                )
                            }
                        }
                    } else null,
                    bottomContent = {
                        TextButton(
                            onClick = { mostrarSelectorDireccion = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    tint = Color.White,
                                    contentDescription = "ubicacion",
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(Modifier.width(4.dp))

                                Text(
                                    color = Color.White,
                                    text = if (isFetchingAddress) "Buscando..." else "${currentAddress?.calle ?: "Seleccionar dirección"} ${currentAddress?.numero}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                )

                                Spacer(Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    tint = Color.White,
                                    contentDescription = "modal",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (mostrarSelectorDireccion) {
            DireccionSelectorModal(
                direcciones = direccionesList,
                seleccionada = currentAddress,
                onConfirmar = {
                    viewModel.setManualAddress(it)
                    mostrarSelectorDireccion = false
                },
                onDismiss = { mostrarSelectorDireccion = false }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f),
                beyondViewportPageCount = 2
            ) { page ->
                key(page) {
                    when (page) {
                        0 -> RestaurantListScreen(
                            locationState = locationState,
                            currentAddress = currentAddress,
                            onRetryLocation = {
                                viewModel.resetLocationInitialization()
                                locationRetryKey++
                            },
                            onRestaurantClick = { id -> navController.navigate("menu/$id") }
                        )

                        1 -> {
                            AnimatedContent(
                                targetState = selectedSub,
                                transitionSpec = {
                                    if (targetState != null) {
                                        (slideInHorizontally { width -> width } + fadeIn())
                                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                                    } else {
                                        (slideInHorizontally { width -> -width } + fadeIn())
                                            .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                                    }
                                },
                                label = "SubCategoriaAPlatosTransition"
                            ) { sub ->
                                if (sub == null) {
                                    SubCategoriaListScreen(
                                        onSubCategoriaClick = { selectedSub = it }
                                    )
                                } else {
                                    PlatoListScreen(
                                        subCategoria = sub,
                                        direccion = currentAddress ?: DTODireccion(),
                                        navController = navController,
                                        onBack = { selectedSub = null }
                                    )
                                }
                            }
                        }

                        2 -> {
                            val dir =
                                currentAddress ?: if (locationState is LocationState.Available) {
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
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
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
