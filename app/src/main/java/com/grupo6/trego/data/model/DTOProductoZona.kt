package com.grupo6.trego.data.model

data class DTOProductoZona(
    val producto: DTOProducto,
    val nombreRestaurante: String,
    val calificacionProm: Float,
    val direccion: DTODireccion,
)