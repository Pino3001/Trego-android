package com.grupo6.trego.data.model

data class CarritoItem(
    val id: Long = System.currentTimeMillis(), // id único por item en carrito
    val producto: ProductoDTO,
    val cantidad: Int = 1,
    val ingredientesQuitados: List<String> = emptyList(),
    val comentario: String = ""
) {
    val subtotal: Double
        get() = (producto.precioOferta ?: producto.precio) * cantidad
}