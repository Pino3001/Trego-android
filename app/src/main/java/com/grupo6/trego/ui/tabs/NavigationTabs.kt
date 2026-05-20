package com.grupo6.trego.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun NavigationTabs(navController: NavController, currentRoute: String) {
    // Definimos las pestañas, sus rutas y sus íconos
    val tabs = listOf(
        TabItem("Restaurantes", "restaurants", Icons.Default.Home),
        TabItem("Carrito", "carrito", Icons.Default.ShoppingCart),
        TabItem("Perfil", "profile", Icons.Default.Person)
    )

    // Buscamos cuál es la pestaña activa actualmente
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray
        )

        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.White,        // ← fondo de la barra
            contentColor = TregoOrange,
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        // Si no estamos en esa pantalla, navegamos hacia ella
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                // Evita acumular pantallas repetidas en el historial
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    selectedContentColor = TregoOrange,  // ← color cuando está seleccionado
                    unselectedContentColor = Color.Gray, // ← color cuando no está seleccionado
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) }
                )
            }
        }
    }
}

// Clase auxiliar para definir cada pestaña
private data class TabItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)