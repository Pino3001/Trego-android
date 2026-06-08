package com.grupo6.trego.ui.usuario

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.grupo6.trego.R
import com.grupo6.trego.data.model.DTOUsuario
import com.grupo6.trego.ui.componentes.DialogComponent
import com.grupo6.trego.ui.componentes.TregoHeader
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.usuario.componentes.DireccionGestion
import org.koin.androidx.compose.koinViewModel

@Composable
fun PerfilScreen(
    onLogout: () -> Unit
) {
    val perfilView: PerfilViewModel = koinViewModel()
    val state by perfilView.state.collectAsStateWithLifecycle()

    val auth = Firebase.auth

    var isEditing by remember { mutableStateOf(false) }
    var nombreInput by remember { mutableStateOf("") }
    var telefonoInput by remember { mutableStateOf("") }
    var emailUsuario by remember { mutableStateOf("Sin correo") }
    var urlImagenUsuario by remember { mutableStateOf<String?>(null) }
    val direcciones = (state as? PerfilUiState.Success)?.direcciones ?: emptyList()
    val tags = (state as? PerfilUiState.Success)?.tags ?: emptyList()

    // Estado para el modal de direcciones
    var showDireccionesModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        perfilView.eventFlow.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    // Cuando se cargan los datos correctamente, inicializamos los campos
    LaunchedEffect(state) {
        if (state is PerfilUiState.Success) {
            val usuario = (state as PerfilUiState.Success).user
            nombreInput = usuario.nombre ?: ""
            telefonoInput = usuario.telefono ?: ""
            emailUsuario = usuario.email ?: ""
            urlImagenUsuario = usuario.urlImagen ?: ""
        }
    }

    // Modal de direcciones
    if (showDireccionesModal) {
        DireccionGestion(
            viewModel = perfilView,
            direcciones = direcciones,  // ✅ Usa la lista del state
            onAgregar = { nueva ->
                perfilView.agregarDireccion(nueva)
            },
            onEditar = { tag, editada ->
                perfilView.actualizarDireccion(tag, editada)
            },
            onDismiss = { showDireccionesModal = false },
            tagsExistentes = tags,
        )
    }

    // Overlay de carga de datos
    AnimatedVisibility(
        visible = state is PerfilUiState.Loading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BackHandler(enabled = true) { /* Bloqueamos el botón atrás */ }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .pointerInput(Unit) { detectTapGestures { } }, // Bloquea toques
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = TregoOrange,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Cargando perfil",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Por favor, espera un momento.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenUri = uri
        }
    }

    if (state is PerfilUiState.Error) {
        DialogComponent(
            message = (state as PerfilUiState.Error).message,
            onDismiss = {
                perfilView.descartarError()
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
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
        topBar = {
            TregoHeader(
                title = "MI PERFIL",
                actions = {
                    // La lógica del botón a la derecha es mucho más limpia así
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = {
                            if (!isEditing) {
                                isEditing = true
                                imagenUri = null
                            } else {
                                isEditing = false
                                imagenUri = null
                                // Restaurar los valores originales
                                if (state is PerfilUiState.Success) {
                                    val u = (state as PerfilUiState.Success).user
                                    nombreInput = u.nombre ?: ""
                                    telefonoInput = u.telefono ?: ""
                                    emailUsuario = u.email ?: ""
                                    urlImagenUsuario = u.urlImagen ?: ""
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (!isEditing) Icons.Default.Edit else Icons.Default.Close,
                                contentDescription = if (!isEditing) "Editar perfil" else "Cancelar edición",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (state) {
            is PerfilUiState.Loading -> {}
            is PerfilUiState.Error -> {
                Box(modifier = Modifier.padding(innerPadding)) {
                    VistaError(
                        mensaje = (state as PerfilUiState.Error).message,
                        onReintentar = { perfilView.cargarPerfil() }
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val imageModel = imagenUri ?: urlImagenUsuario

                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (imageModel == null || imageModel.toString().isBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray, CircleShape)
                                    .border(2.dp, TregoOrange.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                            }
                        } else {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.tregologo),
                                error = painterResource(id = R.drawable.tregologo),
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, TregoOrange.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                        if (isEditing) {
                            Surface(
                                shape = CircleShape,
                                color = TregoOrange,
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(x = (-4).dp, y = (-4).dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clickable {
                                            imagePickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Formulario
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nombreInput,
                            onValueChange = { nombreInput = it },
                            label = { Text("Nombre y Apellido") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color.LightGray,
                                disabledTextColor = Color.DarkGray,
                                disabledLabelColor = Color.Gray
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                        )

                        OutlinedTextField(
                            value = telefonoInput,
                            onValueChange = { telefonoInput = it },
                            label = { Text("Número de Teléfono") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = emailUsuario,
                            onValueChange = { },
                            label = { Text("Correo Electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                        )
                    }

                    if (isEditing) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val usuario = DTOUsuario(
                                    nombre = nombreInput,
                                    email = emailUsuario,
                                    urlImagen = urlImagenUsuario,
                                    telefono = telefonoInput
                                )
                                perfilView.actualizarPerfil(usuario, imagenUri)
                                isEditing = false
                                imagenUri = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                "Guardar Cambios",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Sección de direcciones (sin cambios)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDireccionesModal = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9F9F9),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TregoOrange.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TregoOrange,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Mis Direcciones",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Gestionar dónde recibirás tus pedidos",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Ir",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(32.dp))

                    // Cerrar sesión
                    OutlinedButton(
                        onClick = {
                            auth.signOut()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}