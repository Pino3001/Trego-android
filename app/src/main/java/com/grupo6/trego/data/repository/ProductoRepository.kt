package com.grupo6.trego.data.repository

import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProductoZona
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.remote.ProductosApiService

/**
 * Este repositorio se encarga de pedir al servidor los platos de cada categoría y 
 * las ofertas disponibles según la ubicación del usuario.
 */
class ProductoRepository(private val apiService: ProductosApiService) {

    /**
     * Obtiene la lista de productos de una subcategoría elegida en una dirección específica.
     * @param subCa Subcategoría seleccionada por el usuario.
     * @param dir Dirección obtenida por el dispositivo con la lista de direcciones.
     * @return Lista de productos en la zona.
     */
    suspend fun listarPlatos(subCa: DTOSubCategoria, dir: DTODireccion): List<DTOProductoZona>? {
        return try {
            val response = apiService.listarPlatos(subCa.idSubCategoria, dir)
            if (response.isSuccessful) {
                response.body()
            } else {
                println("Error de servidor: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("Error de conexión: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene la lista de productos con ofertas de restaurantes en una dirección específica.
     * @param dir Dirección obtenida por el dispositivo con la lista de direcciones.
     * @return Lista de productos en la zona.
     */
    suspend fun listarOfertas(dir: DTODireccion): List<DTOProductoZona>? {
        return try {
            val response = apiService.listarOfertas(dir)
            if (response.isSuccessful) {
                response.body()
            } else {
                println("Error de servidor: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("Error de conexión: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}