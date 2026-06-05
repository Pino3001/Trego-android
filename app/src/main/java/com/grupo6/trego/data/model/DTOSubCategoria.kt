package com.grupo6.trego.data.model

data class DTOSubCategoria(
    val idSubCategoria: Int = 0,
    val nombre: String = "",
    val categoria: EnumCategoriaProducto = EnumCategoriaProducto.Otros,
    val urlImagen: String = ""
)