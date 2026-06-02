package com.grupo6.trego.ui.tabs

import android.R.attr.scaleX
import android.R.attr.scaleY
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo6.trego.ui.theme.TregoOrange

private val NavBarColor = Color(0xFFFFFEFE)   // ligeramente más oscuro que blanco puro

@Composable
fun NavigationTabs(
    navController: NavController,
    currentRoute: String,
    cartItemCount: Int = 0,     // 0 = vacío  |  1 = tiene item (máximo 1)
    orderCount: Int = 0,        // cantidad de pedidos activos
) {
    val tabs = remember {
        listOf(
            TabItem("Restaurantes", "restaurants", Icons.Default.Home),
            TabItem("Carrito",      "carrito",     Icons.Default.ShoppingCart),
            TabItem("Pedido",       "pedido",      Icons.Default.Badge),
            TabItem("Perfil",       "profile",     Icons.Default.Person),
        )
    }

    val selectedIndex = remember(currentRoute) {
        tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    Surface(
        shadowElevation = 8.dp,
        color = NavBarColor,
    ) {
        NavigationBar(
            containerColor = NavBarColor,
            tonalElevation = 0.dp,
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "scale_${tab.route}",
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                when {
                                    // Carrito: solo punto naranja (siempre es 1 item)
                                    tab.route == "carrito" && cartItemCount > 0 -> {
                                        Badge(containerColor = TregoOrange)
                                    }
                                    // Pedido: muestra el número de pedidos activos
                                    tab.route == "pedido" && orderCount > 0 -> {
                                        Badge(containerColor = TregoOrange) {
                                            Text(
                                                text = if (orderCount > 99) "99+" else "$orderCount",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = TregoOrange,
                        selectedTextColor   = TregoOrange,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor      = TregoOrange.copy(alpha = 0.12f),
                    )
                )
            }
        }
    }
}

private data class TabItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
)