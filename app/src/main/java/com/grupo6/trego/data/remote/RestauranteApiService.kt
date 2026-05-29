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

    @GET("restaurante/{restauranteId}/verMenu")
    suspend fun verMenuRestaurante(
        @Path("restauranteId") restauranteId: Int,
        @Query("categoria") categoria: String? = null,
        @Query("orden") orden: String? = null
    ): Response<Any>

    @GET("restaurantes/{id}/productos")
    suspend fun verMenuRestaurante(
        @Path("id") restauranteId: Long
    ): Response<DTORestaurante>

    // ── Obtener un restaurante por ID (sin productos) ──
    @GET("restaurantes/{id}")
    suspend fun obtenerRestaurante(
        @Path("id") restauranteId: Long
    ): Response<DTORestaurante>

    // ── Crear un restaurante nuevo ──
    @POST("restaurantes")
    suspend fun crearRestaurante(
        @Body restaurante: DTORestaurante
    ): Response<DTORestaurante>

    // ── Abrir local (indicar que está abierto a una hora) ──
    @POST("restaurantes/{id}/abrir")
    suspend fun abrirLocal(
        @Path("id") idRestaurante: Long,
        @Body horaServicio: String   // usar un DTO con la fecha/hora
    ): Response<Unit>

    // ── Cerrar local ──
    @POST("restaurantes/{id}/cerrar")
    suspend fun cerrarLocal(
        @Path("id") restauranteId: Long
    ): Response<Unit>
}