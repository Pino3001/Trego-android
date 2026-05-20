package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.GoogleLoginRequest
import com.grupo6.trego.data.model.LoginResponseDTO
import com.grupo6.trego.data.model.SmsLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // FLUJO 2: Cliente mediante Google
    @POST("auth/google")
    suspend fun loginConGoogle(
        @Body body: GoogleLoginRequest
    ): Response<LoginResponseDTO>

    // FLUJO 3: Cliente mediante SMS
    @POST("auth/sms")
    suspend fun loginConSMS(
        @Body body: SmsLoginRequest
    ): Response<LoginResponseDTO>
}