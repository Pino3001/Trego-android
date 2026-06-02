package com.grupo6.trego.data.repository

import android.util.Log
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.model.PageResponse
import com.grupo6.trego.data.remote.RestaurantApiService

//Transforma los datos extraidos desde la api
class RestauranteRepository(
    private val api: RestaurantApiService
) {
    // construye un DireccionDTO y lo envia, ver si solo enviar lat y long
    suspend fun getRestaurantsByZone(
        lat: Double,
        lon: Double,
        page: Int = 0,
        size: Int = 10
    ): Result<PageResponse<DTORestaurante>> {
        return try {
            val response = api.listarRestaurantesPorZona(lat, lon, page, size)

            if (response.isSuccessful) {
                // response.body() ahora es un PageResponse.
                // Si es nulo, devolvemos un PageResponse vacío.
                Result.success(response.body() ?: PageResponse())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestaurantsByAddress(direccion: DTODireccion): Result<List<DTORestaurante>> {
        return try {
            val response = api.listarRestaurantesPorDireccion(direccion)

            Log.d("API_PRUEBA", "¡Éxito! Restaurantes obtenidos: ${response.size}")
            Result.success(response)

        } catch (e: Exception) {
            // Comprobamos si el error es de Retrofit (Error HTTP del servidor)
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("API_PRUEBA", "Error HTTP ${e.code()}: $errorBody")
            } else {
                // Es un error de red (sin internet) o falló al leer el JSON
                Log.e("API_PRUEBA", "Error interno o de conexión: ${e.message}")
                e.printStackTrace()
            }

            Result.failure(e)
        }
    }

    suspend fun searchRestaurantsByName(name: String): Result<List<DTORestaurante>> {
        return try {
            val response = api.buscarRestaurantesPorNombre(name)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestaurantMenu(restaurantId: Long): Result<DTORestaurante> {
        return try {
            val response = api.verMenuRestaurante(restaurantId.toInt())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestauranteDatos(restaurantId: Long): Result<DTORestaurante> {
        return try {
            val response = api.verRestauranteData(restaurantId.toInt())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
