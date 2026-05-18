package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun PerfilScreen(onLogout: () -> Unit) {
    val auth = Firebase.auth
    val user = auth.currentUser // Obtenemos el usuario logueado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Foto de perfil (usando Coil para cargar la URL de Google)
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "¡Bienvenido!", style = MaterialTheme.typography.headlineSmall)

        // Datos de Google
        Text(text = user?.displayName ?: "Sin nombre", style = MaterialTheme.typography.bodyLarge)
        Text(text = user?.email ?: "Sin correo", color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = {
            auth.signOut()
            onLogout() // Volver al login
        }) {
            Text("Cerrar Sesión")
        }
    }
}