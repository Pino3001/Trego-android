package com.grupo6.trego

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.grupo6.trego.ui.auth.FormInicio
import com.grupo6.trego.ui.auth.PhoneAuthScreen
import com.grupo6.trego.ui.carrito.CarritoScreen
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.menu.MenuScreen
import com.grupo6.trego.ui.pedidos.HistorialScreen
import com.grupo6.trego.ui.pedidos.PedidoScreen
import com.grupo6.trego.ui.pedidos.PedidoUiState
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.home.HomePage
import com.grupo6.trego.ui.tabs.NavigationTabs
import com.grupo6.trego.ui.usuario.PerfilScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(pendingPaymentStatus: MutableStateFlow<String?> = MutableStateFlow(null)) {
    val activity = LocalContext.current as ComponentActivity
    val navController = rememberNavController()
    val auth = Firebase.auth

    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val rutasConBottomBar =
        listOf("restaurants", "carrito", "pedido", "profile", "menu/{restauranteId}")

    val status = pendingPaymentStatus.collectAsState().value
    LaunchedEffect(user, status) {
        Log.d("DeepLink", "LaunchedEffect App: user=$user, status=$status")
        if (user != null && status != null) {
            Log.d("DeepLink", "Navegando a carrito con status=$status")
            navController.navigate("carrito?status=$status") {
                popUpTo("restaurants") { inclusive = false }
            }
            pendingPaymentStatus.value = null
        }
    }

    val pedidoViewModel: PedidoViewModel = koinViewModel(viewModelStoreOwner = activity)
    val pedidoState by pedidoViewModel.activosState.collectAsState()
    Log.d("Pedidos", "AppNavigation ViewModel: $pedidoViewModel")

    // Calcular el contador de pedidos activos
    val orderCount = if (pedidoState is PedidoUiState.Success) {
        (pedidoState as PedidoUiState.Success).activos.size
    } else 0
    Log.d("Pedidos", "orderCount = $orderCount, state = ${pedidoState::class.simpleName}")
    // Disparar la carga inicial solo cuando haya usuario autenticado y el estado sea Idle
    LaunchedEffect(user, pedidoState) {
        if (user != null && pedidoState is PedidoUiState.Idle) {
            pedidoViewModel.cargarPedidos()
        }
    }

    Scaffold(
        bottomBar = {
            if (user != null && currentRoute.split("?")[0] in rutasConBottomBar) {
                val carritoViewModel: CarritoViewModel = koinViewModel()

                NavigationTabs(
                    navController = navController,
                    currentRoute = currentRoute.split("?")[0],
                    cartItemCount = if (carritoViewModel.items.isNotEmpty()) 1 else 0,
                    orderCount = orderCount
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (user == null) "login" else "restaurants",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                FormInicio(
                    onLoginSuccess = { /* Autogestionado por listener */ },
                    onNavigateToPhone = { navController.navigate("phone_auth") },
                    viewModel = koinViewModel()
                )
            }
            composable("phone_auth") { PhoneAuthScreen(onAuthSuccess = { }) }

            composable("restaurants") {
                if (user == null) {
                    LaunchedEffect(Unit) { navController.navigate("login") { popUpTo(0) } }
                } else {
                    HomePage(
                        navController = navController
                    )
                }
            }

            composable(
                route = "menu/{restauranteId}",
                arguments = listOf(navArgument("restauranteId") { type = NavType.LongType })
            ) { backStackEntry ->
                if (user == null) {
                    LaunchedEffect(Unit) { navController.navigate("login") { popUpTo(0) } }
                } else {
                    val restauranteId = backStackEntry.arguments?.getLong("restauranteId") ?: 0L
                    MenuScreen(restauranteId = restauranteId, navController = navController)
                }
            }

            //  RUTA CARRITO CON DEEP LINK PARA MERCADO PAGO
            composable(
                route = "carrito?status={status}",
                arguments = listOf(navArgument("status") { nullable = true }),
                deepLinks = listOf(navDeepLink { uriPattern = "trego://pago/{status}" })
            ) { backStackEntry ->
                val status = backStackEntry.arguments?.getString("status")
                Log.d("DeepLink", "CarritoScreen composable: status=$status")
                if (user == null) {
                    LaunchedEffect(Unit) { navController.navigate("login") { popUpTo(0) } }
                } else {
                    CarritoScreen(navController = navController, statusDePago = status)
                }
            }

            composable("pedido") {
                if (user == null) {
                    LaunchedEffect(Unit) { navController.navigate("login") { popUpTo(0) } }
                } else {
                    PedidoScreen(navController = navController)
                }
            }

            composable("profile") {
                if (user == null) {
                    LaunchedEffect(Unit) { navController.navigate("login") { popUpTo(0) } }
                } else {
                    PerfilScreen(onLogout = { auth.signOut() })
                }
            }

            composable("historial") {
                HistorialScreen(
                    viewModel = pedidoViewModel, // Pásale tu viewmodel inyectado
                    navController = navController
                )
            }
        }
    }
}
