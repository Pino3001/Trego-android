package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOCrearReclamoRequest
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOPedido
import com.grupo6.trego.data.model.DTOPreferenciaMP
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Este service se encarga de todo el flujo de los pedidos: desde confirmarlos para pagar 
 * con Mercado Pago, hasta ver el historial de lo que compramos o pedir un reembolso.
 */
interface PedidoApiService {

    @POST("pedido/confirmar")
    suspend fun confirmarPedido(
        @Body request: DTODireccion,
        @Query("canal") canal: String = "MOBILE"
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