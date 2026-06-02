package com.grupo6.trego.di

import CarritoRepository
import com.grupo6.trego.ui.menu.MenuViewModel
import com.grupo6.trego.data.remote.AuthApiService
import com.grupo6.trego.data.remote.CarritoApiService
import com.grupo6.trego.data.remote.PedidoApiService
import com.grupo6.trego.data.remote.RestaurantApiService
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.RestauranteRepository
import com.grupo6.trego.data.utilities.AuthInterceptor
import com.grupo6.trego.data.utilities.TokenManager
import com.grupo6.trego.ui.auth.PhoneAuthViewModel
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.restaurantes.RestauranteViewModel
import com.google.gson.GsonBuilder
import com.grupo6.trego.data.utilities.LocalDateTimeAdapter
import com.grupo6.trego.data.utilities.LocalTimeAdapter
import java.time.LocalDateTime
import java.time.LocalTime
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // TokenManager
    single { TokenManager(androidContext()) }

    // Interceptor de autenticación
    single { AuthInterceptor(androidContext(), get()) }

    // OkHttpClient configurado con el interceptor
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(logging)
            .build()
    }

    // Gson configurado con adaptadores para Java Time
    single {
        GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()
    }

    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl("http://localhost:8080/api/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    // Servicios API (las interfaces que usas con Retrofit)
    single { get<Retrofit>().create(AuthApiService::class.java) }
    single { get<Retrofit>().create(RestaurantApiService::class.java) }
    single { get<Retrofit>().create(CarritoApiService::class.java) }
    single { get<Retrofit>().create(PedidoApiService::class.java) }

    // Repositorios
    single { RestauranteRepository(get()) }
    single { CarritoRepository(get()) }
    single { PedidoRepository(get()) }

    // ViewModels
    viewModel { RestauranteViewModel(get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { CarritoViewModel(get(), get()) }
    viewModel { PhoneAuthViewModel(get(), get()) }  // AuthApiService + TokenManager
    viewModel { PedidoViewModel(get(), get()) }
}
