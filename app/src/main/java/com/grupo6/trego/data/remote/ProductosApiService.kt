package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProductoZona
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ProductosApiService {
    @POST("productos/listarProductosSubcategorias")
    suspend fun listarPlatos(
        @Query("idSubCategoria") idSubCategoria: Int,
        @Body direccion: DTODireccion
    ): Response<List<DTOProductoZona>>

    @POST("productos/listarProductosOferta")
    suspend fun listarOfertas(
        @Body direccion: DTODireccion
    ): Response<List<DTOProductoZona>>
}