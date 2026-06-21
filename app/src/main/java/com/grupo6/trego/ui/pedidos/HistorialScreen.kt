package com.grupo6.trego.ui.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.navigation.NavController
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.pedidos.componentes.FiltrosHistorial
import com.grupo6.trego.ui.pedidos.componentes.HistorialCard
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: PedidoViewModel,
    navController: NavController
) {
    val historialState by viewModel.historialState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedEstado by viewModel.selectedEstado.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val ordenMasRecientes by viewModel.ordenMasRecientes.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarHistorial() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surface = BlancoCard,
                surfaceContainerHigh = BlancoCard,
            )
        ) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convertimos los millis del DatePicker a LocalDate usando UTC
                            // (El DatePicker de Material 3 siempre trabaja en UTC internamente)
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()

                            // Enviamos el cambio al ViewModel
                            viewModel.onDateChange(localDate)
                        }
                        showDatePicker = false
                    }) { Text("Confirmar", color = TregoOrange) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = BlancoCard,
                        selectedDayContainerColor = TregoOrange,
                        todayDateBorderColor = TregoOrange,

                        )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TregoHeader(
                title = "Historial",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = TregoOrange,
                    contentColor = Color.White,
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Barra de filtros — 100% sincronizada con el ViewModel
            FiltrosHistorial(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                selectedEstado = selectedEstado,
                onEstadoClick = { estado -> viewModel.toggleEstadoFiltro(estado) },
                selectedDate = selectedDate,
                onDateClick = { showDatePicker = true },
                onDateClear = { viewModel.onDateChange(null) },
                ordenMasRecientes = ordenMasRecientes,
                onOrdenClick = { viewModel.toggleOrden() }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = historialState) {
                    is PedidoUiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TregoOrange
                    )

                    is PedidoUiState.Historial -> {
                        val pedidos = state.historial
                        if (pedidos.isEmpty()) {
                            val esBusqueda = searchQuery.isNotEmpty() || selectedEstado != null || selectedDate != null
                            VistaEstado(
                                titulo = if (esBusqueda) "Sin resultados" else "Historial vacío",
                                mensaje = if (esBusqueda) "No encontramos pedidos que coincidan con tus filtros." 
                                          else "Aún no tienes pedidos finalizados en tu historial.",
                                icono = if (esBusqueda) Icons.Default.SearchOff else Icons.Default.History,
                                colorIcono = Color.Gray,
                                onAccion = if (esBusqueda) { { viewModel.clearFiltros() } } else null,
                                botonTexto = "Limpiar filtros"
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(pedidos) { item ->
                                    HistorialCard(item, onClick = { peticionReclamo, onSuccess ->
                                        viewModel.crearReclamo(peticionReclamo, onSuccess)
                                    })
                                }
                            }
                        }
                    }

                    is PedidoUiState.Error -> {
                        VistaError(
                            mensaje = state.message,
                            onReintentar = { viewModel.cargarHistorial() }
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
