package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTOCliente
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOUsuario
import com.grupo6.trego.data.remote.UsuarioApiService

class UsuarioRepository(private val api: UsuarioApiService) {

    suspend fun verPerfil(): Result<DTOUsuario> {
        return try {
            val response = api.verPerfil()

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

    suspend fun actualizarPerfil(cliente: DTOCliente): Result<Unit> {
        return try {
            val response = api.actualizarPerfil(cliente)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDirecciones(): Result<List<DTODireccion>> {
        return try {
            val response = api.obtenerDirecciones()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las direcciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun agregarDireccion(direccion: DTODireccion): Result<Unit> {
        return try {
            val response = api.agregarDireccine(direccion)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarDireccion(tag: String, direccion: DTODireccion): Result<Unit> {
        return try {
            val response = api.actualizarDireccion(tag, direccion, true)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}