package com.grupo6.trego.data.repository

import android.util.Log
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOPedido
import com.grupo6.trego.data.model.DTOPreferenciaMP
import com.grupo6.trego.data.remote.PedidoApiService

class PedidoRepository(private val api: PedidoApiService) {
    suspend fun confirmarPedido(request: DTODireccion): Result<DTOPreferenciaMP> {
        return try {
            val response = api.confirmarPedido(request)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("PedidoRepo", "Éxito: $body")
                Result.success(body!!)
            } else {
                val errorMsg = response.errorBody()?.string()
                Log.e("PedidoRepo", "Error HTTP ${response.code()}: $errorMsg")
                Result.failure(Exception("Error ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("PedidoRepo", "Excepción de red", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerPedidosCliente(): Result<List<DTOPedido>> {
        return try {
            val response = api.obtenerPedidosCliente()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener pedidos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun obtenerPedidosHistorial(): Result<List<DTOPedido>> {
        return try {
            val response = api.obtenerPedidosCliente()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener pedidos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelarPedido(pedido: DTOPedido): Result<DTOPedido> {
        return try {
            val response = api.cancelarPedido(pedido)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    // Caso donde el servidor responde OK pero el cuerpo está vacío
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                // Manejo de errores HTTP (404, 500, etc)
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red (sin internet, timeout, etc)
            Result.failure(e)
        }
    }
}
