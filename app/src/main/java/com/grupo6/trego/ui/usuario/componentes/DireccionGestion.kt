package com.grupo6.trego.ui.usuario.componentes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.usuario.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionGestion(
    viewModel: PerfilViewModel,
    direcciones: List<DTODireccion>,
    onAgregar: (DTODireccion) -> Unit,
    onEditar: (String, DTODireccion) -> Unit,
    onDismiss: () -> Unit,
    tagsExistentes: List<String>,
) {
    var direccionEditando by remember { mutableStateOf<DTODireccion?>(null) }
    var mostrandoFormulario by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    // El contenedor principal mantiene tu zIndex para sobreponerse a otras vistas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(Color.White)
    ) {
        // AnimatedContent genera una transición suave al cambiar entre pantallas
        AnimatedContent(
            targetState = mostrandoFormulario,
            label = "transicion_vistas"
        ) { mostrando ->
            if (mostrando) {
                // ─── VISTA 2: FORMULARIO ───
                DireccionForm(
                    direccion = direccionEditando,
                    onSave = { tagOrig, it ->
                        if (direccionEditando == null) onAgregar(it) else onEditar(
                            tagOrig ?: "",
                            it
                        )
                        mostrandoFormulario = false
                    },
                    onBack = { mostrandoFormulario = false },
                    tagsExistentes = tagsExistentes,
                )
            } else {
                // ─── VISTA 1: LISTA CON SCAFFOLD ───
                Scaffold(
                    topBar = {
                        TregoHeader(
                            title = "MIS DIRECCIONES",
                            navigationIcon = {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = {
                                direccionEditando = null
                                mostrandoFormulario = true
                            },
                            containerColor = TregoOrange,
                            contentColor = Color.White,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar nueva dirección"
                                )
                            },
                            text = {
                                Text(
                                    text = "AGREGAR DIRECCIÓN",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    },
                ) { innerPadding ->
                    // Contenido principal de la lista
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 88.dp
                        ),
                    ) {
                        items(direcciones) { dir ->
                            DireccionItem(
                                direccion = dir,
                                onEdit = {
                                    direccionEditando = dir
                                    mostrandoFormulario = true
                                }
                            )
                        }

                        if (direcciones.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No tienes direcciones guardadas", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DireccionItem(
    direccion: DTODireccion,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9F9F9),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = TregoOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = direccion.tag ?: "Sin Etiqueta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${direccion.calle ?: "Sin calle"} ${direccion.numero}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!direccion.esquina.isNullOrBlank() || !direccion.apartamento.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(
                            direccion.esquina?.let { "Esq. $it" },
                            direccion.apartamento?.let { "Apto. $it" }
                        ).joinToString(" - "),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Column() {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

            }
        }
    }
}
