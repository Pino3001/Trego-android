package com.grupo6.trego

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.grupo6.trego.ui.auth.PhoneAuthScreen
import com.grupo6.trego.ui.auth.FormInicio
import com.grupo6.trego.ui.auth.PhoneAuthViewModel
import com.grupo6.trego.ui.carrito.CarritoScreen
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.menu.MenuScreen
import com.grupo6.trego.ui.pedidos.PedidoScreen
import com.grupo6.trego.ui.tabs.PerfilScreen
import com.grupo6.trego.ui.restaurantes.RestaurantListScreen
import com.grupo6.trego.ui.tabs.NavigationTabs
import androidx.compose.runtime.collectAsState
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.pedidos.PedidoUiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = Firebase.auth

    // 1. Instancia ÚNICA de los ViewModels para toda la navegación
    val carritoViewModel: CarritoViewModel = koinViewModel()
    val pedidoViewModel: PedidoViewModel = koinViewModel()

    val pedidoState by pedidoViewModel.uiState.collectAsState()
    val activeOrdersCount = if (pedidoState is PedidoUiState.Success) {
        (pedidoState as PedidoUiState.Success).activos.size
    } else 0

    // Observamos la ruta actual para saber qué pestaña marcar y si debemos mostrar la barra
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "restaurants"

    // Definimos qué rutas SÍ deben mostrar la barra inferior
    val rutasConBottomBar = listOf("restaurants", "carrito", "pedido", "profile", "menu/{restauranteId}" )
    val mostrarBottomBar = currentRoute in rutasConBottomBar

    val startDestination = if (auth.currentUser != null) "restaurants" else "login"

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationTabs(
                    navController = navController,
                    currentRoute = currentRoute,
                    // Conexión directa y reactiva al ViewModel único
                    cartItemCount = if (carritoViewModel.items.isNotEmpty()) 1 else 0,
                    orderCount = activeOrdersCount
                )
            }
        }
    ) { innerPadding ->
        // El NavHost se encarga de intercambiar el contenido central
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding) // Aplica el padding de la BottomBar automáticamente
        ) {
            // RUTA LOGIN
            composable("login") {
                val authViewModel: PhoneAuthViewModel = koinViewModel()
                FormInicio(
                    onLoginSuccess = {
                        navController.navigate("restaurants") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToPhone = { navController.navigate("phone_auth") },
                    viewModel = authViewModel
                )
            }

            // RUTA PHONE AUTH
            composable("phone_auth") {
                PhoneAuthScreen(
                    onAuthSuccess = {
                        navController.navigate("restaurants") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // RUTA RESTAURANTES (Pasamos el carritoViewModel compartido si lo necesita)
            composable("restaurants") {
                RestaurantListScreen(
                    navController = navController,
                    //carritoViewModel = carritoViewModel, // <--- Compartido
                    onRestaurantClick = { id -> navController.navigate("menu/$id") }
                )
            }

            // RUTA MENU DETALLE
            composable(
                route = "menu/{restauranteId}",
                arguments = listOf(navArgument("restauranteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val restauranteId = backStackEntry.arguments?.getLong("restauranteId") ?: 0L
                MenuScreen(
                    restauranteId = restauranteId,
                    navController = navController
                    // Si MenuScreen necesita agregar cosas al carrito, le pasas también 'carritoViewModel'
                )
            }

            // RUTA CARRITO
            composable("carrito") {
                CarritoScreen(
                    navController = navController,
                    //carritoViewModel = carritoViewModel
                )
            }

            // RUTA PEDIDO
            composable("pedido") {
                PedidoScreen()
            }

            // RUTA PERFIL
            composable("profile") {
                PerfilScreen(
                    navController = navController,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                )
            }

        }
    }
}