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
    val ingredientes: List<DTOIngrediente>?,
    val tipo: EnumTipoProducto?,
    val plato: PlatoDTO?,
    val articulo: ArticuloDTO?,
    val combo: ComboDTO?,
    val oferta: DTOOferta? = null,
    val subcategoria: DTOSubCategoria? = null
){
    fun toSimplificado(): DTOProductoSimplificado {

        return DTOProductoSimplificado(
            idProducto = idProducto,
            idRestaurante = idRestaurante,
            nombre = nombre,
            precio = precio,
            urlImagen = urlImagen,
            precioOferta = precio, // aca hay que sacar el precio desde ofertas
            ingredientes = ingredientes,
            descripcion = descripcion
        )
    }

    fun calcularPrecioConDescuento(): Float {
        // Si no hay oferta, el precio es el original
        if (this.oferta == null || this.oferta.descuento!! <= 0) {
            return this.precio!!
        }


        // Aplicamos el descuento
        val factorDescuento = this.oferta.descuento / 100.0f
        val montoDescuento = this.precio!! * factorDescuento

        return this.precio - montoDescuento
    }
}

enum class EnumTipoProducto {
    Plato, Articulo, Combo
}


