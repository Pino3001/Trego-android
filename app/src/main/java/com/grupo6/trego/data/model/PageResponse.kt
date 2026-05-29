package com.grupo6.trego.data.model

data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val number: Int = 0, // Página actual
    val last: Boolean = true, // Te avisa si es la última página
    val first: Boolean = true
)