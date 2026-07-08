package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOSubCategoria
import retrofit2.Response
import retrofit2.http.GET

/**
 * Este service es bien simple, solo lo usamos para traer la lista de todas las 
 * subcategorías (como hamburguesas, pizzas, etc.) que ofrece la app.
 */
interface SubCategoriaApiService {

    @GET("subcategorias/listar")
    suspend fun listarSubcategorias(): Response<List<DTOSubCategoria>>
}