package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOSubCategoria
import retrofit2.Response
import retrofit2.http.GET

interface SubCategoriaApiService {

    @GET("subcategorias/listar")
    suspend fun listarSubcategorias(): Response<List<DTOSubCategoria>>
}