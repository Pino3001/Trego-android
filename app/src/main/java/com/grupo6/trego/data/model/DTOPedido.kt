package com.grupo6.trego.data.model

import java.time.LocalDateTime

data class DTOPedido(
    val idPedido: Int? = null,
    val idCliente: Int? = null,
    val idRestaurante: Int? = null,
    val productos: List<DTOProductoPedido>? = null,
    val direccionEntrega: DTODireccion? = null,
    val total: Double? = null,
    val estado: EnumEstadoPedido? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaExpiracion: LocalDateTime? = null,
    val horaEntregaEstimada: LocalDateTime? = null,
    val tiempoPreparacion: Int? = null
)