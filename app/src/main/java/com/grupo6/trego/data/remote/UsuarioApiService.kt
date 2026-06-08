package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOCliente
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOFirma
import com.grupo6.trego.data.model.DTOUsuario
import com.grupo6.trego.data.model.FcmTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsuarioApiService {
    @GET("clientes/actual")
    suspend fun verPerfil(): Response<DTOUsuario>

    @PUT("clientes/actualizar")
    suspend fun actualizarPerfil(
        @Body cliente: DTOCliente
    ): Response<Unit>

    @GET("usuarios/obtenerDirecciones")
    suspend fun obtenerDirecciones(): Response<List<DTODireccion>>

    @POST("usuarios/agregarDireccion")
    suspend fun agregarDireccine(
        @Body direccion: DTODireccion
    ): Response<Unit>

    @POST("usuarios/actualizarDireccion")
    suspend fun actualizarDireccion(
        @Query("tagModificar") tagModificar: String,
        @Body direccion: DTODireccion,
        @Query("client") client: Boolean
    ): Response<Unit>

    @POST("usuarios/imagen/firma/{nombreArchivo}/{tipo}")
    suspend fun firmarImagen(
        @Query("nombreArchivo") nombreArchivo: String,
        @Query("tipo") tipo: String
    ): Response<DTOFirma>

    @PUT("clientes/fcm-token")
    suspend fun renovarTockenFcm(
        @Body request: FcmTokenRequest
    ): Response<Unit>
}
