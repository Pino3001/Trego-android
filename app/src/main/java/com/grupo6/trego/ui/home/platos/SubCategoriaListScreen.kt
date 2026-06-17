package com.grupo6.trego.ui.home.platos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.ui.componentes.SearchBar
import com.grupo6.trego.ui.componentes.VistaError
import com.grupo6.trego.ui.componentes.VistaEstado
import com.grupo6.trego.ui.home.componentes.*
import com.grupo6.trego.ui.home.platos.componentes.CardSubcategoria
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoriaListScreen(
    onSubCategoriaClick: (DTOSubCategoria) -> Unit,
) {
    val viewModel: SubCategoriaViewModel = koinViewModel()
    var showFilterSheet by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onClear = { viewModel.onSearchQueryChanged("") },
                        placeholderText = "Buscar por nombre...",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtros",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.fetchSubcategorias() },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is SubCategoriaUIstate.Cargando -> {
                        LoadingPlaceholder("Cargando subcategorías...")
                    }
                    is SubCategoriaUIstate.Vacio -> {
                        VistaEstado(
                            titulo = "Sin resultados",
                            mensaje = "No se encontraron subcategorías que coincidan con la búsqueda.",
                            icono = Icons.Default.SearchOff,
                            colorIcono = Color.Gray,
                            onAccion = null
                        )
                    }
                    is SubCategoriaUIstate.Cargado -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.items) { subCategoria ->
                                CardSubcategoria(
                                    subCategoria = subCategoria,
                                    onClick = { onSubCategoriaClick(subCategoria) }
                                )
                            }
                        }
                    }
                    is SubCategoriaUIstate.Error -> {
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
