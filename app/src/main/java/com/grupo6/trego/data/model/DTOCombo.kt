package com.grupo6.trego.data.model

data class DTOCombo(
    val productosIncluidos: List<ProductoIncluido> = emptyList()
) {
    data class ProductoIncluido(
        val id: Int,
        val nombre: String
    )

    /**
     * Devuelve un mapa con el nombre de cada producto incluido y la cantidad
     * de veces que aparece en el combo (respeta duplicados).
     */
    fun obtenerConteoPorNombre(): Map<String, Int> {
        return productosIncluidos.groupingBy { it.nombre }.eachCount()
    }
}
