package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTOComentario
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.model.PageResponse
import com.grupo6.trego.data.remote.RestaurantApiService

/**
 * Con este repositorio manejamos toda la información de los restaurantes, permitiéndonos
 * buscarlos, ver sus menús y gestionar los comentarios de los usuarios.
 */
class RestauranteRepository(
    private val api: RestaurantApiService
) {
    suspend fun getRestaurantsByZone(
        lat: Double, lon: Double, page: Int = 0, size: Int = 10
    ): Result<PageResponse<DTORestaurante>> {
        return try {
            val response = api.listarRestaurantesPorZona(lat, lon, page, size)

            if (response.isSuccessful) {
                Result.success(response.body() ?: PageResponse())
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestaurantsByAddress(direccion: DTODireccion): Result<List<DTORestaurante>> {
        return try {
            val response = api.listarRestaurantesPorDireccion(direccion)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRestaurantsByName(name: String): Result<List<DTORestaurante>> {
        return try {
            val response = api.buscarRestaurantesPorNombre(name)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestauranteDatos(restaurantId: Long): Result<DTORestaurante> {
        return try {
            val response = api.verRestauranteData(restaurantId.toString())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListarComentarios(idRestaurante: Int): Result<List<DTOComentario>> {
        return try {
            val response = api.listarComentarios(idRestaurante)
            if (response.isSuccessful()) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setComentario(comentario: DTOComentario): Result<DTOComentario> {
        return try {
            val response = api.crearComentario(comentario)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                val errorMsg =
                    response.errorBody()?.string() ?: "Error desconocido HTTP ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun yaComentoUsuario(idRestaurante: Long): Result<Boolean> {
        return try {
            val response = api.yaComentoUsuario(idRestaurante.toInt())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(requireNotNull(body["yaComento"]))
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCalificacionPromedio(idRestaurante: Int): Result<Float> {
        return try {
            val response = api.obtenerCalificacion(idRestaurante)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.toFloat())
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
