package com.grupo6.trego.data.model

import java.time.LocalTime

data class DTORestaurante(
    var idRestaurante: Int? = null,
    var nombre: String? = null,
    var email: String? = null,
    var password: String? = null,
    var rut: String? = null,
    var telefono: String? = null,
    var fotoPerfil: String? = null,
    var fotoPortada: String? = null,
    var direccion: DTODireccion? = null,
    var descripcion: String? = null,
    var categoria: EnumCategoriaRestaurante? = null,
    var calificacionProm: Float? = null,
    var radioEntrega: Int? = null,
    var habilitado: Boolean? = null,
    var abierto: Boolean? = null,
    var horaApertura: LocalTime? = null,
    var horaCierre: LocalTime? = null,
    var productos: MutableList<DTOProducto> = mutableListOf(),
    var ofertas: Boolean = false
)