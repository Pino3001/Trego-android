package com.grupo6.trego.data.model

import java.time.LocalDateTime

data class DTOProducto(
    val idProducto: Int,               // sigue siendo obligatorio
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Float? = null,
    val urlImagen: String? = null,
    val categoria: EnumCategoriaProducto? = null,
    val disponible: Boolean? = null,
    val idRestaurante: Int? = null,
    val cantidadDisponible: Int? = null,
    val ingredientes: List<DTOIngrediente>? = null,
    val tipo: EnumTipoProducto? = null,
    val ofertaActiva: Boolean? = null,
    val plato: DTOPlato? = null,
    val articulo: DTOArticulo? = null,
    val combo: DTOCombo? = null,
    val oferta: DTOOferta? = null,
    val subCategoria: DTOSubCategoria? = null
) {
    fun calcularPrecioConDescuento(): Int {
        val precioBase = this.precio ?: 0f

        // Si no hay oferta, el descuento no es positivo, o la oferta no está activa → precio original
        val descuento = this.oferta?.descuento ?: 0f
        if (this.oferta == null || descuento <= 0f || this.ofertaActiva != true) {
            return precioBase.toInt()
        }

        // Validación de fechas
        val ahora = LocalDateTime.now()
        val inicio = this.oferta.fechaInicio
        val fin = this.oferta.fechaFin

        // Si hay fecha de inicio y aún no empezó, no aplica
        if (inicio != null && ahora.isBefore(inicio)) {
            return precioBase.toInt()
        }
        // Si hay fecha de fin y ya finalizó, no aplica
        if (fin != null && ahora.isAfter(fin)) {
            return precioBase.toInt()
        }

        // Oferta vigente, activa y con descuento > 0: aplicamos el descuento
        val factorDescuento = descuento / 100f
        val montoDescuento = precioBase * factorDescuento
        return precioBase.toInt() - montoDescuento.toInt()
    }
}

enum class EnumTipoProducto {
    Plato, Articulo, Combo
}
