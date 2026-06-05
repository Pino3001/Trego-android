package com.grupo6.trego.data.model

data class ResultadoGeoapify(
    val descripcion: String,   // formatted address para mostrar
    val calle: String?,
    val numero: String?,
    val ciudad: String?,
    val latitud: Double,
    val longitud: Double
)