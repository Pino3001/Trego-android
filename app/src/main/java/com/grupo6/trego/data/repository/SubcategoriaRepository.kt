package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.remote.SubCategoriaApiService

/**
 * Con este repositorio traemos del servidor la lista de subcategorías disponibles para 
 * que el usuario pueda filtrar los productos que busca.
 */
class SubcategoriaRepository(private val apiService: SubCategoriaApiService) {

    suspend fun listarSubcategorias(): List<DTOSubCategoria>? {
        val response = apiService.listarSubcategorias()
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}
