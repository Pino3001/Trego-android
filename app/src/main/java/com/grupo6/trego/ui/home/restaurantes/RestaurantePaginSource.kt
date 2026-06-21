package com.grupo6.trego.ui.home.restaurantes

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grupo6.trego.data.model.PageResponse
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.repository.RestauranteRepository
import com.grupo6.trego.data.utilities.AppReadyState

class RestaurantPagingSource(
    private val repository: RestauranteRepository,
    private val lat: Double,
    private val lon: Double
) : PagingSource<Int, DTORestaurante>() {

    override fun getRefreshKey(state: PagingState<Int, DTORestaurante>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DTORestaurante> {
        // La API de Spring Boot suele empezar en la página 0
        val currentPage = params.key ?: 0

        return try {
            val result = repository.getRestaurantsByZone(
                lat = lat,
                lon = lon,
                page = currentPage,
                size = params.loadSize
            )

            if (result.isSuccess) {
                val pageResponse = result.getOrNull() ?: PageResponse()

                // Si cargamos la primera página con éxito, avisamos que la data está lista
                if (currentPage == 0) {
                    AppReadyState.setDataReady(true)
                }

                LoadResult.Page(
                    data = pageResponse.content,
                    prevKey = if (currentPage == 0) null else currentPage - 1,
                    // Si 'last' es true, devolvemos null para indicarle a Paging que no busque más
                    nextKey = if (pageResponse.last || pageResponse.content.isEmpty()) null else currentPage + 1
                )
            } else {
                if (currentPage == 0) AppReadyState.setDataReady(true)
                LoadResult.Error(result.exceptionOrNull() ?: Exception("Error en la respuesta de la API"))
            }
        } catch (e: Exception) {
            if (params.key == null || params.key == 0) AppReadyState.setDataReady(true)
            LoadResult.Error(e)
        }
    }
}