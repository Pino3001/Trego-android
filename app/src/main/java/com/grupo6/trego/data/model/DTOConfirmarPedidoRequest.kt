package com.grupo6.trego.data.model

data class DTOConfirmarPedidoRequest(
    val carrito: DTOCarrito?,
    val direccion: DTODireccion,
    val restauranteId: Int
)