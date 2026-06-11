package com.grupo6.trego.data.model

import java.time.LocalDateTime

data class DTOProductoSimplificado(
    val idProducto: Int,
    val idRestaurante: Int?,
    val nombre: String?,
    val precio: Float?,
    val urlImagen: String?,
    val oferta: DTOOferta?,
    val ingredientes: List<DTOIngrediente>?,
    val descripcion: String?
){
    fun calcularPrecioConDescuentoEnSimplificado(): Float {
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