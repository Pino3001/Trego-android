package com.grupo6.trego.data.model

import java.time.LocalDateTime

data class DTOProducto(
    val idProducto: Int,
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
    val subCategoria: DTOSubCategoria? = null
) {
    fun toSimplificado(): DTOProductoSimplificado {
        val precio = calcularPrecioConDescuento()
        return DTOProductoSimplificado(
            idProducto = idProducto,
            idRestaurante = idRestaurante,
            nombre = nombre,
            precio = precio,
            urlImagen = urlImagen,
            oferta = oferta,
            ingredientes = ingredientes,
            descripcion = descripcion
        )
    }

    fun calcularPrecioConDescuento(): Float {
        // Si no hay oferta o el descuento no es positivo, precio original
        if (this.oferta == null || this.oferta.descuento!! <= 0) {
            return this.precio!!
        }

        // Validación de fechas
        val ahora = LocalDateTime.now()
        val inicio = this.oferta.fechaInicio
        val fin = this.oferta.fechaFin

        // Si hay fecha de inicio y aún no empezó, no aplica
        if (inicio != null && ahora.isBefore(inicio)) {
            return this.precio!!
        }
        // Si hay fecha de fin y ya finalizó, no aplica
        if (fin != null && ahora.isAfter(fin)) {
            return this.precio!!
        }

        // Si llegamos acá, la oferta es válida: aplicamos el descuento
        val factorDescuento = this.oferta.descuento / 100.0f
        val montoDescuento = this.precio!! * factorDescuento
        return this.precio - montoDescuento
    }
}

enum class EnumTipoProducto {
    Plato, Articulo, Combo
}


