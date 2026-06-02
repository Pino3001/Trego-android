package com.grupo6.trego.data.model

data class DTOProductoSimplificado(
    val idProducto: Int?,
    val idRestaurante: Int?,
    val nombre: String?,
    val precio: Float?,
    val urlImagen: String?,
    val precioOferta: Float?,
    val ingredientes: List<DTOIngrediente>?,
    val descripcion: String? // ver que no rompa el endpoint
)