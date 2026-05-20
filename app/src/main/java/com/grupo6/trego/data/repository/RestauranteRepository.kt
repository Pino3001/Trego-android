package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DireccionDTO
import com.grupo6.trego.data.model.RestaurantDTO
import com.grupo6.trego.data.remote.RestaurantApiService
import com.grupo6.trego.data.remote.RetrofitClient

//Transforma los datos extraidos desde la api
class RestauranteRepository(
    private val api: RestaurantApiService = RetrofitClient.restaurantService
) {
    // ✅ construye un DireccionDTO y lo envia, ver si solo enviar lat y long
    suspend fun getRestaurantsByZone(lat: Double, lon: Double): Result<List<RestaurantDTO>> {
        return try {
            val direccion = DireccionDTO(id = null, etiqueta = null, latitud = lat, longitud = lon, direccion = null)
            val response = api.listarRestaurantesPorZona(direccion)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRestaurantsByName(name: String): Result<List<RestaurantDTO>> {
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

    suspend fun getRestaurantMenu(restaurantId: Long): Result<RestaurantDTO> {
        return try {
            val response = api.verMenuRestaurante(restaurantId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Menú vacío"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}