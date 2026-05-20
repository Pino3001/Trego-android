package com.grupo6.trego.data.model

data class DireccionDTO(
    val id: Long?,
    val etiqueta: String?,   // "Casa", "Trabajo", etc.
    val direccion: String?,
    val latitud: Double? = null,
    val longitud: Double? = null
)