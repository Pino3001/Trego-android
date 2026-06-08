package com.grupo6.trego.ui.usuario.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.ResultadoGeoapify
import com.grupo6.trego.data.utilities.LocationState
import com.grupo6.trego.data.utilities.RequestLocation
import com.grupo6.trego.data.utilities.autocompletarDireccion
import com.grupo6.trego.data.utilities.reverseGeocode
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.theme.TregoOrange
import kotlinx.coroutines.delay

@Composable
fun DireccionForm(
    direccion: DTODireccion?,
    tagsExistentes: List<String>,
    onSave: (String?, DTODireccion) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val tagOriginal = remember { direccion?.tag }
    val dir = direccion ?: DTODireccion(
        id = 0,
        tag = "",
        calle = "",
        numero = "",
        apartamento = "",
        esquina = "",
        latitud = 0.0,
        longitud = 0.0
    )

    var etiqueta by remember { mutableStateOf(dir.tag ?: "") }
    var calle by remember { mutableStateOf(dir.calle ?: "") }
    var esquina by remember { mutableStateOf(dir.esquina ?: "") }
    var numero by remember { mutableStateOf(dir.numero ?: "") }
    var apartamento by remember { mutableStateOf(dir.apartamento ?: "") }
    var latitud by remember { mutableStateOf(dir.latitud) }
    var longitud by remember { mutableStateOf(dir.longitud) }

    // ── La clave del fix: LaunchedEffect se suscribe a la VERSIÓN, no al valor ──
    // Solo el usuario incrementa la versión al tipear → los cambios programáticos
    // (aplicarResultado, GPS) actualizan el display sin disparar nueva búsqueda.
    var searchQuery by remember { mutableStateOf("") }
    var searchVersion by remember { mutableIntStateOf(0) }
    var resultados by remember { mutableStateOf<List<ResultadoGeoapify>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    var mostrarResultados by remember { mutableStateOf(false) }

    var esquinaVersion by remember { mutableIntStateOf(0) }
    var resultadosEsquina by remember { mutableStateOf<List<ResultadoGeoapify>>(emptyList()) }
    var buscandoEsquina by remember { mutableStateOf(false) }
    var mostrarResultadosEsquina by remember { mutableStateOf(false) }

    var locationState by remember { mutableStateOf<LocationState>(LocationState.Idle) }
    var retryKey by remember { mutableIntStateOf(0) }
    var solicitandoUbicacion by remember { mutableStateOf(false) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }

    if (solicitandoUbicacion) {
        RequestLocation(retryKey = retryKey, onStateChange = { locationState = it })
    }

    // Al editar, excluimos el tag propio para que no se marque como duplicado
    val tagError by remember(tagsExistentes) {
        derivedStateOf {
            when {
                etiqueta.isBlank() -> "La etiqueta es obligatoria"
                tagsExistentes
                    .filter { !it.equals(direccion?.tag, ignoreCase = true) }
                    .any {
                        it.equals(
                            etiqueta.trim(),
                            ignoreCase = true
                        )
                    } -> "Ya tenés una dirección con esa etiqueta"

                else -> null
            }
        }
    }

    LaunchedEffect(locationState) {
        when (val state = locationState) {
            is LocationState.Available -> {
                errorUbicacion = null
                val dto = reverseGeocode(state.lat, state.lon)
                if (dto != null) {
                    calle = dto.calle ?: ""
                    numero = dto.numero ?: ""
                    latitud = dto.latitud
                    longitud = dto.longitud
                    // SIN searchVersion++ → no dispara búsqueda, solo actualiza display
                    searchQuery = listOfNotNull(
                        dto.calle,
                        dto.numero?.takeIf { it.isNotBlank() }
                    ).joinToString(" ")
                    mostrarResultados = false
                    resultados = emptyList()
                } else {
                    errorUbicacion = "No se pudo identificar la dirección"
                }
                solicitandoUbicacion = false
            }

            is LocationState.PermissionDenied -> {
                errorUbicacion = "Permiso de ubicación denegado"; solicitandoUbicacion = false
            }

            is LocationState.LocationUnavailable -> {
                errorUbicacion = "No se pudo obtener la ubicación"; solicitandoUbicacion = false
            }

            else -> Unit
        }
    }

    // Keyed en searchVersion, no en searchQuery
    LaunchedEffect(searchVersion) {
        if (searchVersion == 0) return@LaunchedEffect
        if (searchQuery.length >= 3) {
            delay(400L)
            buscando = true
            resultados = autocompletarDireccion(searchQuery)
            mostrarResultados = resultados.isNotEmpty()
            buscando = false
        } else {
            resultados = emptyList()
            mostrarResultados = false
        }
    }

    LaunchedEffect(esquinaVersion) {
        if (esquinaVersion == 0) return@LaunchedEffect
        if (esquina.length >= 3) {
            delay(400L)
            buscandoEsquina = true
            resultadosEsquina = autocompletarDireccion(esquina)
            mostrarResultadosEsquina = resultadosEsquina.isNotEmpty()
            buscandoEsquina = false
        } else {
            resultadosEsquina = emptyList()
            mostrarResultadosEsquina = false
        }
    }

    fun aplicarResultado(r: ResultadoGeoapify) {
        calle = r.calle ?: ""; numero = r.numero ?: ""
        latitud = r.latitud; longitud = r.longitud
        searchQuery = r.descripcion   // SIN searchVersion++ → el LaunchedEffect no se reinicia
        mostrarResultados = false
        resultados = emptyList()
    }

    fun aplicarResultadoEsquina(r: ResultadoGeoapify) {
        esquina = r.calle ?: r.descripcion   // SIN esquinaVersion++ → idem
        mostrarResultadosEsquina = false
        resultadosEsquina = emptyList()
    }

    val cargandoGPS = locationState is LocationState.RequestingPermission
    val hayErrorGPS =
        locationState is LocationState.PermissionDenied || locationState is LocationState.LocationUnavailable
    val gpsColor = if (hayErrorGPS) Color.Red.copy(alpha = 0.7f) else TregoOrange

    Scaffold(
        topBar = {
            TregoHeader(
                title = if (direccion == null) "NUEVA DIRECCIÓN" else "EDITAR DIRECCIÓN",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            )
        },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = etiqueta,
                    onValueChange = { etiqueta = it },
                    label = { Text("Etiqueta (Casa, Trabajo...)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = tagError != null,
                    supportingText = tagError?.let { msg ->
                        { Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(enabled = !cargandoGPS) {
                            errorUbicacion = null; locationState =
                            LocationState.Idle; retryKey++; solicitandoUbicacion = true
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = gpsColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.5.dp, gpsColor.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (cargandoGPS) CircularProgressIndicator(
                            Modifier.size(20.dp),
                            color = TregoOrange,
                            strokeWidth = 2.dp
                        )
                        else Icon(
                            Icons.Default.MyLocation,
                            null,
                            tint = gpsColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (errorUbicacion != null) {
                Text(
                    errorUbicacion!!,
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Buscador principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchVersion++   // ← solo el usuario toca esto
                    },
                    label = { Text("Buscar calle y número") },
                    placeholder = { Text("Ej: 18 de Julio 1234") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        if (buscando) CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = TregoOrange,
                            strokeWidth = 2.dp
                        )
                        else Icon(Icons.Default.Search, null, tint = Color.Gray)
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = {
                                searchQuery = ""; searchVersion++; resultados =
                                emptyList(); mostrarResultados = false
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null
                )
                if (mostrarResultados) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 62.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shadowElevation = 8.dp, color = Color.White
                    ) {
                        Column {
                            resultados.take(5).forEachIndexed { idx, r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { aplicarResultado(r) }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = TregoOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            r.descripcion,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (!r.ciudad.isNullOrBlank()) Text(
                                            r.ciudad,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                if (idx < resultados.take(5).lastIndex) HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Campos manuales — sin el texto extra "Detalle de la dirección"
            OutlinedTextField(
                value = calle, onValueChange = { calle = it },
                label = { Text("Calle") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = numero ?: "", onValueChange = { numero = it },
                    label = { Text("Número") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = apartamento ?: "", onValueChange = { apartamento = it },
                    label = { Text("Apto/Piso") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
            }

            // Buscador esquina
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                OutlinedTextField(
                    value = esquina,
                    onValueChange = {
                        esquina = it
                        esquinaVersion++   // ← solo el usuario toca esto
                    },
                    label = { Text("Esquina (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        if (buscandoEsquina) CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = TregoOrange,
                            strokeWidth = 2.dp
                        )
                        else Icon(Icons.Default.Search, null, tint = Color.Gray)
                    },
                    trailingIcon = if (esquina.isNotBlank()) {
                        {
                            IconButton(onClick = {
                                esquina = ""; esquinaVersion++; resultadosEsquina =
                                emptyList(); mostrarResultadosEsquina = false
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null
                )
                if (mostrarResultadosEsquina) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 62.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shadowElevation = 8.dp, color = Color.White
                    ) {
                        Column {
                            resultadosEsquina.take(3).forEachIndexed { idx, r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { aplicarResultadoEsquina(r) }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = TregoOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        r.calle ?: r.descripcion,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (idx < resultadosEsquina.take(3).lastIndex) HorizontalDivider(
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        tagOriginal,
                        DTODireccion(
                            id = direccion?.id,
                            tag = etiqueta.trim(),
                            calle = calle,
                            numero = numero ?: "",
                            esquina = esquina.ifBlank { null },
                            apartamento = apartamento?.ifBlank { null },
                            latitud = latitud,
                            longitud = longitud
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                shape = RoundedCornerShape(16.dp),
                enabled = calle.isNotBlank() && numero.isNotBlank() && tagError == null
            ) {
                Text("GUARDAR DIRECCIÓN", fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}