package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.PageResponse
import com.grupo6.trego.data.model.DTORestaurante
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApiService {

    // ── Listar restaurantes por zona ──
    @POST("restaurantes/listarXdirreccion")
    suspend fun listarRestaurantesPorDireccion(
        @Body direccion: DTODireccion
    ): List<DTORestaurante>

    @GET("pedido/Listar")
    suspend fun listarRestaurantesPorZona(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<DTORestaurante>>

    // ── Buscar restaurantes por nombre ──
    @GET("restaurantes/buscar")
    suspend fun buscarRestaurantesPorNombre(
        @Query("nombre") nombre: String
    ): Response<List<DTORestaurante>>

    // ── Ver menú de un restaurante (con sus productos) ──

    @GET("pedido/restaurante/{restauranteId}/verMenu")
    suspend fun verMenuRestaurante(
        @Path("restauranteId") restauranteId: Int,
        @Query("categoria") categoria: String? = null,
        @Query("orden") orden: String? = null
    ): Response<DTORestaurante>

    @GET("restaurantes/obtenerRestaurante/{id}")
    suspend fun verRestauranteData(
        @Path("id") id: String
    ): Response<DTORestaurante>

}