package com.grupo6.trego.data.remote

import com.grupo6.trego.data.model.DTOUsuario
import com.grupo6.trego.data.model.GoogleLoginRequest
import com.grupo6.trego.data.model.LoginResponseDTO
import com.grupo6.trego.data.model.SmsLoginRequest
import com.grupo6.trego.data.model.VincularRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Este service maneja todo lo que tiene que ver con la autenticación de usuarios.
 * Desde acá nos conectamos con el backend para los inicios de sesión con Google o SMS.
 */
interface AuthApiService {

    @POST("auth/google")
    suspend fun loginConGoogle(
        @Body body: GoogleLoginRequest
    ): Response<LoginResponseDTO>

    @POST("auth/sms")
    suspend fun loginConSMS(
        @Body body: SmsLoginRequest
    ): Response<LoginResponseDTO>

    @POST("auth/vincular")
    suspend fun vincularProveedor(
        @Body body: VincularRequest
    ): Response<DTOUsuario>

}