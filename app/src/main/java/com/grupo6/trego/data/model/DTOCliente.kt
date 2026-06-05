package com.grupo6.trego.data.model

data class DTOCliente(
    val nombre: String? = null,
    val email: String? = null,
    val urlImagen: String? = null,
    val telefono: String? = null,
    val uidCliente: String? = null,
    val direcciones: List<DTODireccion> = emptyList()
)

fun DTOUsuario.toDTOCliente(): DTOCliente = DTOCliente(
    nombre = this.nombre,
    email = this.email,
    urlImagen = this.urlImagen,
    telefono = this.telefono
)