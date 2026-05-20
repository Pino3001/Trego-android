package com.grupo6.trego.data.remote

import android.content.Context
import com.grupo6.trego.data.utilities.AuthInterceptor
import com.grupo6.trego.data.utilities.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/api/"

    private var retrofit: Retrofit? = null

    // Esta función la vas a llamar al inicio de tu app
    fun init(context: Context) {
        val tokenManager = TokenManager(context.applicationContext)

        // Creamos el cliente HTTP de OkHttp y le metemos nuestro interceptor
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // se vincula el interceptor con el token de sesion (Es lo que envia el token al back)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getRetrofitInstance(): Retrofit {
        return retrofit ?: throw IllegalStateException("RetrofitClient no ha sido inicializado. Llama a init(context).")
    }

    // 1. Servicio para la autenticación (Login / Registro)
    val authService: AuthApiService by lazy {
        getRetrofitInstance().create(AuthApiService::class.java)
    }

    // 2. Servicio para los restaurantes ✨
    val restaurantService: RestaurantApiService by lazy {
        getRetrofitInstance().create(RestaurantApiService::class.java)
    }
}