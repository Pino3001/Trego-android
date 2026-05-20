package com.grupo6.trego.data.model

data class ProductoDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val imagenUrl: String? = null,
    val tieneOferta: Boolean = false,
    val precioOferta: Double? = null,
    val descuentoPorcentaje: Int? = null
)