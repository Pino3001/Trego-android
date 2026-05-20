package com.grupo6.trego

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.grupo6.trego.ui.auth.PhoneAuthScreen
import com.grupo6.trego.ui.auth.FormInicio
import com.grupo6.trego.ui.auth.PhoneAuthViewModel
import com.grupo6.trego.ui.carrito.CarritoScreen
import com.grupo6.trego.ui.menu.MenuScreen
import com.grupo6.trego.ui.tabs.PerfilScreen
import com.grupo6.trego.ui.restaurantes.RestaurantListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = Firebase.auth

    // 🔄 1. CAMBIADO: Si el usuario ya está logueado, ahora va a "restaurants" en vez de "profile"
    val startDestination = if (auth.currentUser != null) "restaurants" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // Ruta del Login
        composable("login") {
            // 1. Obtenemos la instancia del ViewModel para la pantalla de inicio
            val authViewModel: PhoneAuthViewModel = viewModel()

            FormInicio(
                onLoginSuccess = {
                    // 🔄 Al loguearse con éxito, navega a la lista de restaurantes
                    navController.navigate("restaurants") {
                        // Borra la pantalla de login del stack para que no se pueda volver atrás
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToPhone = {
                    navController.navigate("phone_auth")
                },
                viewModel = authViewModel
            )
        }

        // Ruta del Perfil (Sigue existiendo por si navegas a ella desde un menú o botón)
        composable("profile") {
            PerfilScreen(
                navController = navController,
                onLogout = {
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            })
        }

        composable("phone_auth") {
            PhoneAuthScreen(
                onAuthSuccess = {
                    // 💡 Si el login por teléfono también debe ir a restaurantes, cámbialo aquí:
                    navController.navigate("restaurants") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Ruta de Restaurantes (Ahora es el Home de la app)
        composable("restaurants") {
            RestaurantListScreen(
                navController = navController,
                onRestaurantClick = { id ->
                    navController.navigate("menu/$id")
                }
            )
        }

        composable(
            route = "menu/{restauranteId}",
            arguments = listOf(navArgument("restauranteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val restauranteId = backStackEntry.arguments?.getLong("restauranteId") ?: 0L
            MenuScreen(
                restauranteId = restauranteId,
                navController = navController
            )
        }

        composable("carrito") {
            CarritoScreen(navController = navController)
        }
    }
}