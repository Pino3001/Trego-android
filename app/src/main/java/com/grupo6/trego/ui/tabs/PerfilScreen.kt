package com.grupo6.trego.ui.tabs

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun PerfilScreen(onLogout: () -> Unit, navController: NavController) {
    val auth = Firebase.auth
    val user = auth.currentUser

    Scaffold(
        bottomBar = {
            // 🚀 CORREGIDO: Le pasamos "profile" como la ruta actual para que la pestaña se pinte bien
            NavigationTabs(navController = navController, currentRoute = "profile")
        }
    ) { innerPadding -> // 👈 Usamos el padding que nos da el Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 🚀 CORREGIDO: El padding evita que el contenido quede tapado por la barra
                .padding(24.dp), // Tu padding personalizado hacia adentro
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = user?.photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "¡Bienvenido!", style = MaterialTheme.typography.headlineSmall)

            Text(text = user?.displayName ?: "Sin nombre", style = MaterialTheme.typography.bodyLarge)
            Text(text = user?.email ?: "Sin correo", color = Color.Gray)

            Spacer(modifier = Modifier.height(40.dp))

            Button(onClick = {
                auth.signOut()
                onLogout()
            }) {
                Text("Cerrar Sesión")
            }
        }
    }
}