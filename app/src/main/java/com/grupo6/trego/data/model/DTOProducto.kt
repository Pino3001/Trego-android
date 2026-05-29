package com.grupo6.trego.data.model

data class DTOProducto(
    val idProducto: Int?,
    val nombre: String?,
    val descripcion: String?,
    val precio: Float?,
    val urlImagen: String?,
    val categoria: EnumCategoriaProducto?,
    val disponible: Boolean?,
    val idRestaurante: Int?,
    val cantidadDisponible: Int?,
    val ingredientes: List<IngredienteDTO>?,
    val tipo: EnumTipoProducto?,
    val plato: PlatoDTO?,
    val articulo: ArticuloDTO?,
    val combo: ComboDTO?
)

enum class EnumTipoProducto {
    Plato, Articulo, Combo
}

enum class EnumCategoriaProducto {
    Plato, Articulo, Combo
}