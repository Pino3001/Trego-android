package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProductoZona
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.remote.ProductosApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Este repositorio se encarga de pedir al servidor los platos de cada categoría y 
 * las ofertas disponibles según la ubicación del usuario.
 */
class ProductoRepository(private val apiService: ProductosApiService) {

    private fun handleException(e: Exception): Exception {
        return when (e) {
            is UnknownHostException, is IOException ->
                Exception("Sin conexión a internet. Verificá tu red e intentá de nuevo.")
            is SocketTimeoutException ->
                Exception("El servidor está tardando mucho en responder. Intentá de nuevo.")
            else -> e
        }
    }

    /**
     * Obtiene la lista de productos de una subcategoría elegida en una dirección específica.
     * @param subCa Subcategoría seleccionada por el usuario.
     * @param dir Dirección obtenida por el dispositivo con la lista de direcciones.
     * @return Result con la lista de productos en la zona.
     */
    suspend fun listarPlatos(subCa: DTOSubCategoria, dir: DTODireccion): Result<List<DTOProductoZona>> {
        return try {
            val response = apiService.listarPlatos(subCa.idSubCategoria, dir)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else if (response.code() == 404) {
                Result.success(emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error de servidor: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    /**
     * Obtiene la lista de productos con ofertas de restaurantes en una dirección específica.
     * @param dir Dirección obtenida por el dispositivo con la lista de direcciones.
     * @return Result con la lista de productos en la zona.
     */
    suspend fun listarOfertas(dir: DTODireccion): Result<List<DTOProductoZona>> {
        return try {
            val response = apiService.listarOfertas(dir)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else if (response.code() == 404) {
                Result.success(emptyList())
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error de servidor: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
}