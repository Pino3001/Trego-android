package com.grupo6.trego

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.grupo6.trego.ui.componentes.FormInicio
import com.grupo6.trego.ui.componentes.PerfilScreen

@Composable
fun AppNavigation() {
    // Crea y recuerda el controlador que gestiona el historial de pantallas
    val navController = rememberNavController()
    // Accede a Firebase para verificar el estado de la sesión actual
    val auth = Firebase.auth

    // Determina la pantalla de inicio: si Firebase ya tiene un usuario, va directo a "profile".
    // Si no hay nadie logueado (null), lo envía a "login".
    val startDestination = if (auth.currentUser != null) "profile" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // Ruta del Login
        composable("login") {
            // Le pasamos al FormInicio una función para que sepa navegar al tener éxito
            FormInicio(
                onLoginSuccess = {
                    // Acción al loguearse con éxito:
                    navController.navigate("profile") {
                        // "Limpieza de historial": Borra la pantalla de login del stack
                        // 'inclusive = true' hace que si el usuario pulsa "atrás", la app se cierre
                        // en lugar de volver al formulario de inicio.
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Ruta del Perfil --- cambiar por la pagina de inicio o modificar perfil
        composable("profile") {
            PerfilScreen(onLogout = {
                // Acción al cerrar sesión:
                navController.navigate("login") {
                    // "Limpieza de historial": Borra la pantalla de perfil del stack
                    // para que no se pueda volver a ver los datos después de salir.
                    popUpTo("profile") { inclusive = true }
                }
            })
        }
    }
}