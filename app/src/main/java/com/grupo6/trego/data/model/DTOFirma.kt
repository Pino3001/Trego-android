package com.grupo6.trego.data.model

data class DTOFirma(
    val firma: String,
    val timestamp: Long,
    val apiKey: String,
    val cloudName: String,
    val uploadUrl: String,
    val publicId: String
)