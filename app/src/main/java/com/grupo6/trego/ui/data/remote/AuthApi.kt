package com.grupo6.trego.ui.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/verify-token")
    suspend fun verifyToken(@Body body: TokenRequest): Response<TokenResponse>
}

data class TokenRequest(val idToken: String)
data class TokenResponse(val userId: String, val message: String)