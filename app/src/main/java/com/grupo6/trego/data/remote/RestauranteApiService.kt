package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DireccionDTO
import com.grupo6.trego.data.model.RestaurantDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApiService {

    // ── Listar restaurantes por zona ──
    @POST("restaurantes/zona")
    suspend fun listarRestaurantesPorZona(
        @Body direccion: DireccionDTO   // Usás un DTO con latitud/longitud y dirección
    ): Response<List<RestaurantDTO>>

    // ── Buscar restaurantes por nombre ──
    @GET("restaurantes/buscar")
    suspend fun buscarRestaurantesPorNombre(
        @Query("nombre") nombre: String
    ): Response<List<RestaurantDTO>>

    // ── Ver menú de un restaurante (con sus productos) ──
    @GET("restaurantes/{id}/productos")
    suspend fun verMenuRestaurante(
        @Path("id") restauranteId: Long
    ): Response<RestaurantDTO>   // Este DTO ahora debe contener la lista de productos/categorías

    // ── Obtener un restaurante por ID (sin productos) ──
    @GET("restaurantes/{id}")
    suspend fun obtenerRestaurante(
        @Path("id") restauranteId: Long
    ): Response<RestaurantDTO>

    // ── Crear un restaurante nuevo ──
    @POST("restaurantes")
    suspend fun crearRestaurante(
        @Body restaurante: RestaurantDTO
    ): Response<RestaurantDTO>

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

/*    // ── Actualizar hora de cierre ──
    @PUT("restaurantes/{id}/hora-cierre")
    suspend fun actualizarHoraCierre(
        @Path("id") restauranteId: Long,
        @Body nuevaHora: String
    ): Response<Date>

    // ── Listar restaurantes pendientes de aprobación (admin) ──
    @GET("restaurantes/en-espera")
    suspend fun listarRestaurantesEnEspera(): Response<List<RestaurantDTO>>

    // ── Firmar imagen (obtiene URL firmada para subir logo, etc.) ──
    @POST("restaurantes/firmar-imagen")
    suspend fun firmarImagen(
        @Body datos: FirmaRequest   // { nombreArchivo, tipoArchivo }
    ): Response<FirmaDTO>*/
}