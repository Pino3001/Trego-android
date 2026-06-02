package com.grupo6.trego.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.grupo6.trego.R
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val auth = Firebase.auth
    val user = auth.currentUser

    // 1. Estados locales para la edición
    var isEditing by remember { mutableStateOf(false) }
    var nombreInput by remember { mutableStateOf(user?.displayName ?: "") }
    var telefonoInput by remember { mutableStateOf("") } // Asumiendo que puedes tener un teléfono guardado
    val emailFijo = user?.email ?: "Sin correo"

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Usamos tu base de Header Naranja
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TregoOrange)
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MI PERFIL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    // Botón para activar/desactivar la edición (Arriba a la derecha)
                    IconButton(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. FOTO DE PERFIL ---
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    // Agregamos fallbacks por si el usuario no tiene foto
                    placeholder = painterResource(id = R.drawable.tregologo),
                    error = painterResource(id = R.drawable.tregologo),
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, TregoOrange.copy(alpha = 0.5f), CircleShape)
                )

                if (isEditing) {
                    // Ícono de cámara superpuesto si está editando
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
                                .clickable { /* TODO: Lógica para abrir galleria */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. FORMULARIO DE DATOS ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Campo Nombre
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
                    )
                )

                // Campo Teléfono
                OutlinedTextField(
                    value = telefonoInput,
                    onValueChange = { telefonoInput = it },
                    label = { Text("Número de Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Campo Correo (Bloqueado)
                OutlinedTextField(
                    value = emailFijo,
                    onValueChange = { },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    enabled = false, // Firebase requiere validación extra para cambiar email
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Botón Guardar Cambios (Solo visible en modo edición)
            if (isEditing) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        isEditing = false
                        // TODO: Aquí llamas a tu ViewModel para actualizar en Firebase/BD
                        // viewModel.actualizarPerfil(nombreInput, telefonoInput)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. SECCIÓN DE DIRECCIONES ---
            // Una tarjeta clickeable que llama la atención
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // TODO: Navegar a tu administrador de direcciones
                        // navController.navigate("gestor_direcciones")
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9F9F9),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row (
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
                        Text("Mis Direcciones", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Gestionar dónde recibirás tus pedidos", color = Color.Gray, fontSize = 12.sp)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ir",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón de cerrar sesión hacia abajo
            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. BOTÓN CERRAR SESIÓN MEJORADO ---
            // OutlinedButton indica que es una acción secundaria o destructiva
            OutlinedButton(
                onClick = {
                    auth.signOut()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color(0xFFD32F2F)), // Tono rojo suave
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