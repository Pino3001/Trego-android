package com.grupo6.trego.data.model

data class ProductoBackendDTO(
    val idProducto: Int?,
    val nombre: String?,
    val descripcion: String?,
    val precio: Float?,
    val urlImagen: String?,
    val categoria: String?,          // o un enum, pero como String para evitar deserialización compleja
    val disponible: Boolean?,
    val idRestaurante: Int?,
    // ProductoPedido
    val cantidadDisponible: Int?,
    val ingredientes: List<IngredienteDTO>?,
    val observaciones: String?,
    // Carrito
    val cantidad: Int?,
    val subtotal: Double?,
    // Tipos
    val tipo: String?,               // "PLATO", "ARTICULO", "COMBO"
    val plato: PlatoDTO?,            // DTO específico para plato, si existe
    val articulo: ArticuloDTO?,      // DTO específico para artículo
    val combo: ComboDTO?             // DTO específico para combo
)

data class IngredienteDTO(val nombre: String?, val extra: Boolean?, /* ... */)
data class PlatoDTO(val descripcion: String?, /* ... */)
data class ArticuloDTO(val marca: String?, /* ... */)
data class ComboDTO(val productos: List<ProductoBackendDTO>?, /* ... */)