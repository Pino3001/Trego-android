package com.grupo6.trego.ui.carrito.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.data.utilities.RequestLocation
import com.grupo6.trego.data.utilities.reverseGeocode
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionSelectorModal(
    direcciones: List<DTODireccion>,
    seleccionada: DTODireccion?,
    titulo: String = "Dirección de entrega",
    onConfirmar: (DTODireccion) -> Unit,
    onDismiss: () -> Unit
) {
    // Estado del BottomSheet para que se abra completo y no se bugee con el scroll
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tabSeleccionada by remember { mutableStateOf(0) }
    var direccionElegida by remember { mutableStateOf(seleccionada) }

    // Estado ubicación actual
    var locationState by remember { mutableStateOf<LocationState>(LocationState.Idle) }
    var retryKey by remember { mutableStateOf(0) }
    var direccionActual by remember { mutableStateOf<DTODireccion?>(null) }
    var cargandoGeocode by remember { mutableStateOf(false) }

    // Campos editables para ubicación actual
    var calleInput by remember { mutableStateOf("") }
    var numeroInput by remember { mutableStateOf("") }
    var esquinaInput by remember { mutableStateOf("") }
    var apartamentoInput by remember { mutableStateOf("") }

    LaunchedEffect(locationState) {
        if (locationState is LocationState.Available) {
            val loc = locationState as LocationState.Available
            cargandoGeocode = true
            val dto = reverseGeocode(loc.lat, loc.lon)
            dto?.let {
                direccionActual = it
                calleInput = it.calle ?: ""
                numeroInput = it.numero?.takeIf { num -> num.isNotBlank() && num != "0" } ?: ""
                esquinaInput = it.esquina ?: ""
                apartamentoInput = it.apartamento ?: ""
            }
            cargandoGeocode = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = BlancoCard,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
        ) {
            // --- HEADER FIJO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEEEEEE)),
                ) {
                    listOf("Guardadas", "Ubicación actual").forEachIndexed { index, label ->
                        val seleccionado = tabSeleccionada == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (seleccionado) TregoOrange else Color.Transparent)
                                .clickable { tabSeleccionada = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (seleccionado) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // --- BODY SCROLLEABLE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (tabSeleccionada == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        if (direcciones.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOff,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "No tenés direcciones guardadas",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(direcciones) { dir ->
                                val esSeleccionada = direccionElegida?.tag == dir.tag
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                        .clickable { direccionElegida = dir },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esSeleccionada) TregoOrange.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                                    border = if (esSeleccionada) BorderStroke(1.5.dp, TregoOrange) else BorderStroke(1.dp, Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (esSeleccionada) TregoOrange else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "${dir.tag ?: "Tag"} - ${dir.calle}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )

                                        Spacer(Modifier.weight(1f))
                                        if (esSeleccionada) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = TregoOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Tab Ubicación actual ──
                if (tabSeleccionada == 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .padding(horizontal = 20.dp)
                    ) {
                        if (locationState is LocationState.Idle || locationState is LocationState.RequestingPermission) {
                            RequestLocation(
                                retryKey = retryKey,
                                onStateChange = { locationState = it }
                            )
                        }

                        when (locationState) {
                            is LocationState.Idle, is LocationState.RequestingPermission -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = TregoOrange)
                                        Spacer(Modifier.height(12.dp))
                                        Text("Obteniendo ubicación...", color = Color.Gray, fontSize = 13.sp)
                                    }
                                }
                            }
                            is LocationState.PermissionDenied -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.LocationOff, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Permiso de ubicación denegado", color = Color.Gray, fontSize = 13.sp)
                                        Spacer(Modifier.height(8.dp))
                                        TextButton(onClick = { retryKey++ }) { Text("Reintentar", color = TregoOrange) }
                                    }
                                }
                            }
                            is LocationState.LocationUnavailable -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("No se pudo obtener la ubicación", color = Color.Gray)
                                        TextButton(onClick = { retryKey++ }) { Text("Reintentar", color = TregoOrange) }
                                    }
                                }
                            }
                            is LocationState.Available -> {
                                if (cargandoGeocode) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = TregoOrange)
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Confirmá tu dirección", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        OutlinedTextField(
                                            value = calleInput,
                                            onValueChange = { calleInput = it },
                                            label = { Text("Calle") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = numeroInput,
                                                onValueChange = { numeroInput = it },
                                                label = { Text("Número") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                            )
                                            OutlinedTextField(
                                                value = apartamentoInput,
                                                onValueChange = { apartamentoInput = it },
                                                label = { Text("Apto.") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                                            )
                                        }
                                        OutlinedTextField(
                                            value = esquinaInput,
                                            onValueChange = { esquinaInput = it },
                                            label = { Text("Esquina (opcional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                                        )
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- FOOTER FIJO ---
            val puedeConfirmar = when (tabSeleccionada) {
                0 -> direccionElegida != null
                1 -> locationState is LocationState.Available && !cargandoGeocode && calleInput.isNotBlank()
                else -> false
            }

            Box(modifier = Modifier.padding(20.dp)) {
                Button(
                    onClick = {
                        when (tabSeleccionada) {
                            0 -> direccionElegida?.let { onConfirmar(it) }
                            1 -> {
                                val loc = locationState as? LocationState.Available ?: return@Button
                                onConfirmar(
                                    DTODireccion(
                                        calle = calleInput.ifBlank { null },
                                        numero = numeroInput.ifBlank { null },
                                        apartamento = apartamentoInput.ifBlank { null },
                                        esquina = esquinaInput.ifBlank { null },
                                        latitud = loc.lat,
                                        longitud = loc.lon
                                    )
                                )
                            }
                        }
                        onDismiss()
                    },
                    enabled = puedeConfirmar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "Confirmar dirección",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}