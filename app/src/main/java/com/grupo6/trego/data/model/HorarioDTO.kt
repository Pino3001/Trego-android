package com.grupo6.trego.data.model

data class HorarioDTO(
    val hour: Int,
    val minute: Int
) {
    fun toFormattedString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}