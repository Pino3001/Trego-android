package com.grupo6.trego.data.model

data class RestaurantDTO(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val zona: String,
    val calificacion: Double,
    val horarioApertura: String,
    val horarioCierre: String,
    val abierto: Boolean,
    val tieneOfertas: Boolean,
    val imagenUrl: String? = null,
    val productos: List<ProductoDTO>? = null
)