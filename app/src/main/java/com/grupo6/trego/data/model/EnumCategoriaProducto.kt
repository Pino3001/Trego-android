package com.grupo6.trego.data.model

enum class EnumCategoriaProducto {
    Bebida,
    Ensalada,
    Principal,
    Entrada,
    Guarnicion,
    Postre,
    Otros
}

fun listarCategorias(): List<EnumCategoriaProducto> {
    return EnumCategoriaProducto.entries
}