package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOProductoZona
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Con este service pedimos al servidor los platos y las ofertas que están disponibles 
 * según la zona donde esté el usuario.
 */
interface ProductosApiService {
    @POST("productos/listarProductosSubcategorias")
    suspend fun listarPlatos(
        @Query("idSubCategoria") idSubCategoria: Int,
        @Body direccion: DTODireccion
    ): Response<List<DTOProductoZona>>

    @POST("productos/listarProductosOferta")
    suspend fun listarOfertas(
        @Body direccion: DTODireccion
    ): Response<List<DTOProductoZona>>
}