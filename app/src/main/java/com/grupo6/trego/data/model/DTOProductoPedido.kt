package com.grupo6.trego.data.model

data class DTOProductoPedido(
    val cantidadDisponible: Int?,
    val ingredientes: List<DTOIngrediente>?,
    val observaciones: String?,
    val cantidad: Int?,
    val subtotal: Double?,
    val producto: DTOProductoSimplificado?
) {
}