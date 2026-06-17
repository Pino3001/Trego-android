package com.grupo6.trego.ui.home.restaurantes.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun RestaurantPagedList(
    lazyItems: LazyPagingItems<DTORestaurante>,
    onRestaurantClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            count = lazyItems.itemCount,
            key = lazyItems.itemKey { it.idRestaurante!! }
        ) { index ->
            lazyItems[index]?.let { restaurant ->
                RestaurantItem(
                    restaurant = restaurant,
                    onClick = {
                        onRestaurantClick(
                            restaurant.idRestaurante?.toLong() ?: 0L
                        )
                    }
                )
            }
        }

        if (lazyItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TregoOrange
                    )
                }
            }
        }

        if (lazyItems.loadState.append is LoadState.Error) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { lazyItems.retry() }) {
                        Text("Error al cargar más. Reintentar", color = TregoOrange)
                    }
                }
            }
        }
    }
}
