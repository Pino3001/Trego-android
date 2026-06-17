package com.grupo6.trego.ui.home.componentes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.ui.home.restaurantes.componentes.RestaurantItem

@Composable
fun RestaurantSimpleList(
    restaurants: List<DTORestaurante>,
    onRestaurantClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantItem(
                restaurant = restaurant,
                onClick = { onRestaurantClick(restaurant.idRestaurante?.toLong() ?: 0L) }
            )
        }
    }
}