package com.grupo6.trego.data.model

data class DTOProductoPedido(
    val cantidadDisponible: Int?,
    val ingredientesAQuitar: List<DTOIngrediente>?,
    val observaciones: String?,
    val cantidad: Int?,
    val subtotal: Float?,
    val producto: DTOProducto?
)