package com.grupo6.trego.data.model

data class DTODireccion(
    var id: Int? = 0,
    var calle: String? = null,
    var numero: Int = 0,
    var apartamento: Int = 0,
    var esquina: String? = null,
    var latitud: Double = 0.0,
    var longitud: Double = 0.0
)