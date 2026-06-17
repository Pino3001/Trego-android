package com.grupo6.trego.ui.home.platos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.model.EnumCategoriaProducto
import com.grupo6.trego.data.repository.SubcategoriaRepository
import kotlinx.coroutines.launch

sealed class SubCategoriaUIstate {
    object Cargando : SubCategoriaUIstate()
    object Vacio : SubCategoriaUIstate()
    data class Cargado(val items: List<DTOSubCategoria>) : SubCategoriaUIstate()
    data class Error(val mensaje: String) : SubCategoriaUIstate()
}

class SubCategoriaViewModel(private val repository: SubcategoriaRepository) : ViewModel() {

    private var allSubcategorias = emptyList<DTOSubCategoria>()

    var uiState by mutableStateOf<SubCategoriaUIstate>(SubCategoriaUIstate.Cargando)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf<EnumCategoriaProducto?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        fetchSubcategorias()
    }

    fun fetchSubcategorias() {
        viewModelScope.launch {
            // Si ya tenemos datos, usamos el indicador de refresco (isRefreshing)
            // Si es la primera carga (allSubcategorias está vacío), usamos el estado Cargando
            if (allSubcategorias.isEmpty()) {
                uiState = SubCategoriaUIstate.Cargando
            } else {
                isRefreshing = true
            }

            try {
                val response = repository.listarSubcategorias()
                allSubcategorias = response ?: emptyList()
                aplicarFiltros()
            } catch (e: Exception) {
                uiState = SubCategoriaUIstate.Error("Error al cargar subcategorías: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery
        aplicarFiltros()
    }

    fun onCategorySelected(categoria: EnumCategoriaProducto?) {
        selectedCategory = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val filtradas = allSubcategorias.filter { sub ->
            val coincideNombre = sub.nombre.contains(searchQuery, ignoreCase = true)
            val coincideCategoria = selectedCategory == null || sub.categoria == selectedCategory
            coincideNombre && coincideCategoria
        }

        uiState = when {
            // Si no hay datos y estamos iniciando, mostramos Vacio (o Cargando si prefieres)
            allSubcategorias.isEmpty() && uiState is SubCategoriaUIstate.Cargando -> SubCategoriaUIstate.Vacio
            filtradas.isEmpty() -> SubCategoriaUIstate.Vacio
            else -> SubCategoriaUIstate.Cargado(filtradas)
        }
    }
}
