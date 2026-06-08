package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOCrearReclamoRequest
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOPedido
import com.grupo6.trego.data.model.DTOPreferenciaMP
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PedidoApiService {

    @POST("pedido/confirmar")
    suspend fun confirmarPedido(
        @Body request: DTODireccion
    ): Response<DTOPreferenciaMP>

    @GET("pedido/misPedidosActuales")
    suspend fun obtenerPedidosCliente(): Response<List<DTOPedido>>

    @GET("pedido/misPedidos")
    suspend fun obtenerPedidosHistorial(): Response<List<DTOPedido>>

    @POST("pedido/reembolsar")
    suspend fun cancelarPedido(
        @Body request: DTOPedido
    ): Response<DTOPedido>

    @POST("reclamos")
    suspend fun crearReclamo(@Body request: DTOCrearReclamoRequest): Response<Unit>

}