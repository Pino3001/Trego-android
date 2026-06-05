package com.grupo6.trego.data.model

data class DTODireccion(
    var id: Int? = 0,
    var tag: String? = "",
    var calle: String? = null,
    var numero: String? = null,
    var apartamento: String? = null,
    var esquina: String? = null,
    var latitud: Double = 0.0,
    var longitud: Double = 0.0
)