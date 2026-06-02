package com.grupo6.trego.data.model

import java.time.LocalDateTime

data class DTOOferta(
    val idOferta: Int? = null,
    val descripcion: String? = null,
    val descuento: Float? = null,      // porcentaje, ej. 20.0 para 20 %
    val urlImagen: String? = null,
    val fechaInicio: LocalDateTime? = null,
    val fechaFin: LocalDateTime? = null
)