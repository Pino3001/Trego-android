package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOComentario
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.model.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Acá centralizamos todo lo que necesitamos de los restaurantes: buscarlos por ubicación, 
 * ver sus menús, leer comentarios o dejar una reseña propia.
 */
interface RestaurantApiService {

    @POST("restaurantes/listarXdirreccion")
    suspend fun listarRestaurantesPorDireccion(
        @Body direccion: DTODireccion
    ): Response<List<DTORestaurante>>

    @GET("pedido/Listar")
    suspend fun listarRestaurantesPorZona(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<DTORestaurante>>

    @GET("restaurantes/listar")
    suspend fun buscarRestaurantesPorNombre(
        @Query("nombre") nombre: String
    ): Response<List<DTORestaurante>>

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

    @GET("restaurantes/comentarios/listar")
    suspend fun listarComentarios(
        @Query("idRestaurante") idRestaurante: Int
    ): Response<List<DTOComentario>>

    @GET("restaurantes/comentarios/yaComente")
    suspend fun yaComentoUsuario(
        @Query("idRestaurante") idRestaurante: Int
    ): Response<Map<String, Boolean>>

    @POST("restaurantes/comentarios/agregar")
    suspend fun crearComentario(
        @Body request: DTOComentario
    ): Response<DTOComentario>

    @GET("restaurantes/calificacion/obtener/{id}")
    suspend fun obtenerCalificacion(
        @Path("id") id: Int,
        @Query("esRestaurante") esRestaurante: Boolean = false
    ): Response<Int>


}