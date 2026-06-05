package com.grupo6.trego.data.repository

import com.grupo6.trego.data.remote.SubCategoriaApiService
import com.grupo6.trego.data.remote.UsuarioApiService

class SubcategoriaRepository(private val apiService: SubCategoriaApiService) {

    suspend fun listarSubcategorias(){}
}