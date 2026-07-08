package com.grupo6.trego.ui.home.platos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.home.componentes.CategoryFilterBottomSheet
import com.grupo6.trego.ui.home.componentes.LoadingPlaceholder
import com.grupo6.trego.ui.home.platos.componentes.CardSubcategoria
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel

/**
 * Esta pantalla muestra la lista completa de subcategorías (como Hamburguesas, Pizzas, 
 * Postres, etc.) para que el usuario pueda explorar por tipo de comida. Permite 
 * filtrar por categoría principal y buscar subcategorías específicas por nombre.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoriaListScreen(
    onSubCategoriaClick: (DTOSubCategoria) -> Unit,
) {
    val viewModel: SubCategoriaViewModel = koinViewModel()
    var showFilterSheet by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val uiState = viewModel.uiState

    if (showFilterSheet) {
        CategoryFilterBottomSheet(
            selectedCategory = viewModel.selectedCategory,
            onApply = {
                viewModel.onCategorySelected(it)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.fetchSubcategorias() },
            modifier = Modifier.fillMaxSize(),
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = viewModel.isRefreshing,
                    containerColor = BlancoCard,
                    color = TregoOrange,
                    state = state
                )
            }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                /* Sección superior con la barra de búsqueda y el botón para filtrar por categorías (Comida, Bebida, etc.). */
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = TregoOrange,
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 12.dp,
                                        topEnd = 0.dp,
                                        bottomEnd = 50.dp
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SearchBar(
                                query = viewModel.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChanged,
                                onClear = { viewModel.onSearchQueryChanged("") },
                                placeholderText = "Buscar por nombre...",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color.White,
                                showBorder = false
                            )

                            Spacer(Modifier.width(8.dp))

                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filtros",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                when (uiState) {
                    is SubCategoriaUIstate.Cargando -> {
                        if (!viewModel.isRefreshing) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingPlaceholder("Cargando subcategorías...")
                                }
                            }
                        }
                    }

                    is SubCategoriaUIstate.Vacio -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                VistaEstado(
                                    titulo = "Sin resultados",
                                    mensaje = "No se encontraron subcategorías que coincidan con la búsqueda.",
                                    icono = Icons.Default.SearchOff,
                                    colorIcono = Color.Gray,
                                    onAccion = null
                                )
                            }
                        }
                    }

                    /* Mostramos cada subcategoría en una cuadrícula de tres columnas para que sea fácil de navegar. */
                    is SubCategoriaUIstate.Cargado -> {
                        itemsIndexed(uiState.items) { index, subCategoria ->
                            val col = index % 3
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = when (col) {
                                            0 -> 16.dp; 1 -> 10.dp; else -> 4.dp
                                        },
                                        end = when (col) {
                                            0 -> 4.dp; 1 -> 10.dp; else -> 16.dp
                                        }
                                    )
                            ) {
                                CardSubcategoria(
                                    subCategoria = subCategoria,
                                    onClick = { onSubCategoriaClick(subCategoria) }
                                )
                            }
                        }
                    }

                    is SubCategoriaUIstate.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                VistaError(
                                    mensaje = uiState.mensaje,
                                    onReintentar = { viewModel.fetchSubcategorias() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
