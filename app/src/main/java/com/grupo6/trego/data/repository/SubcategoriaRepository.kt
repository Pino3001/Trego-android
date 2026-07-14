package com.grupo6.trego.data.repository

import android.util.Log
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.remote.SubCategoriaApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Con este repositorio traemos del servidor la lista de subcategorías disponibles para 
 * que el usuario pueda filtrar los productos que busca.
 */
class SubcategoriaRepository(private val apiService: SubCategoriaApiService) {

    private fun handleException(e: Exception): Exception {
        return when (e) {
            is UnknownHostException, is IOException ->
                Exception("Sin conexión a internet. Verificá tu red e intentá de nuevo.")
            is SocketTimeoutException ->
                Exception("El servidor está tardando mucho en responder. Intentá de nuevo.")
            else -> e
        }
    }

    suspend fun listarSubcategorias(): List<DTOSubCategoria> {
        return try {
            val response = apiService.listarSubcategorias()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                // Lanzamos excepción con el código de error para que el ViewModel lo capture
                throw Exception("Error del servidor: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            throw handleException(e)
        }
    }
}
