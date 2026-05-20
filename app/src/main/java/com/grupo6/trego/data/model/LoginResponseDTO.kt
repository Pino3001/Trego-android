package com.grupo6.trego.data.model

data class LoginResponseDTO(
    val jwtToken: String,
    val rol: String,
    val nombre: String,
    val email: String
)