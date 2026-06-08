package com.grupo6.trego.data.model

data class DTOComentario(
    val idComentario: Int? = null,
    val texto: String? = null,
    val idRestaurante: Long? = null,
    val calificacion: Int? = null,
    val fechaCreacion: String? = null,
    val nombreCliente: String? = null
)
