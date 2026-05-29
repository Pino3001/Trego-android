package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOCarrito
import com.grupo6.trego.data.model.DTOAgregarAlCarritoRequest
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTOProductoPedido
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

interface CarritoApiService {

    @GET("carrito")
    suspend fun obtenerCarrito(): Response<DTOCarrito>

    @POST("carrito/productos")
    suspend fun agregarProducto(@Body request: DTOProductoPedido): Response<DTOCarrito>

    @PUT("carrito/productos")   // o como lo tengas mapeado
    suspend fun modificarProductoCarrito(@Body request: DTOProductoPedido): Response<DTOProductoPedido>

    @HTTP(method = "DELETE", path = "carrito/productos", hasBody = true)
    suspend fun eliminarProducto(@Body request: DTOProductoPedido): Response<DTOCarrito>

    @DELETE("carrito")
    suspend fun limpiarCarrito(): Response<Unit>

    @DELETE("carrito/items")
    suspend fun limpiarItemsCarrito(): Response<DTOCarrito>

}