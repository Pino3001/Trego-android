package com.grupo6.trego.data.model

data class DTOCarrito(
    val idCarrito: Long?,
    val idRestaurante: Long?,
    val uidCliente: String?,
    val productos: List<DTOProductoPedido>?,   // lista de líneas de pedido
    val total: Double?
) {
}