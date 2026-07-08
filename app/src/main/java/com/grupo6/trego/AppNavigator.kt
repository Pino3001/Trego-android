package com.grupo6.trego

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.grupo6.trego.data.model.NavigationTarget
import com.grupo6.trego.ui.auth.FormInicio
import com.grupo6.trego.ui.auth.PhoneAuthScreen
import com.grupo6.trego.ui.carrito.CarritoScreen
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.home.HomePage
import com.grupo6.trego.ui.menu.MenuScreen
import com.grupo6.trego.ui.pedidos.HistorialScreen
import com.grupo6.trego.ui.pedidos.PedidoScreen
import com.grupo6.trego.ui.pedidos.PedidoUiState
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.tabs.NavigationTabs
import com.grupo6.trego.ui.theme.BackgroundColor
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.usuario.PerfilScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel

/**
 * Este archivo es el mapa de navegación de toda la app. Acá definimos todas las rutas, 
 * manejamos cómo se pasa de una pantalla a otra y controlamos qué partes de la interfaz 
 * se muestran según dónde esté el usuario, como la barra de navegación de abajo.
 */
@Composable
fun AppNavigation(
    pendingPaymentStatus: MutableStateFlow<String?> = MutableStateFlow(null),
    navigationTarget: MutableStateFlow<NavigationTarget?> = MutableStateFlow(null)
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val navController = rememberNavController()
    val auth = Firebase.auth
    val destinoPendiente by navigationTarget.collectAsState()
    val carritoViewModel: CarritoViewModel =
        koinViewModel(viewModelStoreOwner = activity ?: context as ComponentActivity)

    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    /* Este bloque se encarga de redirigir al usuario automáticamente cuando volvemos de un pago o cuando tocamos una notificación. */
    LaunchedEffect(destinoPendiente) {
        destinoPendiente?.let { target ->
            if (target == NavigationTarget.PAGO_REALIZADO) {
                carritoViewModel.marcarPagoExitoso()
            } else {
                carritoViewModel.recargarCarrito()
            }
            val rutaDestino = when (target) {
                NavigationTarget.PEDIDO -> "pedido"
                NavigationTarget.HISTORIAL -> "historial"
                NavigationTarget.PAGO_REALIZADO -> "pedido"
            }
            navController.navigate(rutaDestino) {
                launchSingleTop = true
                popUpTo("restaurants") { inclusive = false }
            }
            navigationTarget.value = null
        }
    }

    /* Cambiamos el color de la barra de estado del celular para que combine con la pantalla actual: clarito para login y naranja para el resto de la app. */
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Si es pantalla de login o verificación, barra de estado clara con iconos oscuros
            if (currentRoute == "login" || currentRoute == "phone_auth" || (currentRoute.isEmpty() && user == null)) {
                window.statusBarColor = BackgroundColor.toArgb()
                insetsController.isAppearanceLightStatusBars = true
            } else {
                // En el resto de la app, fondo naranja con iconos claros
                window.statusBarColor = TregoOrange.toArgb()
                insetsController.isAppearanceLightStatusBars = false
            }
        }
    }
    val rutasConBottomBar =
        listOf("restaurants", "carrito", "pedido", "profile", "menu/{restauranteId}", "historial")

    val status = pendingPaymentStatus.collectAsState().value
    LaunchedEffect(user, status) {
        if (user != null && status != null) {
            navController.navigate("carrito?status=$status") {
                popUpTo("restaurants") { inclusive = false }
            }
            pendingPaymentStatus.value = null
        }
    }

    val pedidoViewModel: PedidoViewModel =
        koinViewModel(viewModelStoreOwner = activity ?: context as ComponentActivity)
    val pedidoState by pedidoViewModel.activosState.collectAsState()

    // Calcular el contador de pedidos activos
    val orderCount = if (pedidoState is PedidoUiState.Success) {
        (pedidoState as PedidoUiState.Success).activos.size
    } else 0
    // Disparar la carga inicial solo cuando haya usuario autenticado y el estado sea Idle
    LaunchedEffect(user, pedidoState) {
        if (user != null && pedidoState is PedidoUiState.Idle) {
            pedidoViewModel.cargarPedidos()
        }
    }

    /* Si el usuario está logueado y la ruta lo permite, mostramos la barra de pestañas de abajo con los contadores de carrito y pedidos activos. */
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
        /* Definimos cada una de las pantallas de la app y qué información necesitan para cargarse, como el ID de un restaurante o el estado de un pago. */
        NavHost(
            navController = navController,
            startDestination = if (user == null) "login" else "restaurants",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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
                arguments = listOf(navArgument("status") { nullable = true })
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


fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}