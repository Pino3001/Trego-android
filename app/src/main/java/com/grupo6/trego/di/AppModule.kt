package com.grupo6.trego.di

import CarritoRepository
import com.google.gson.GsonBuilder
import com.grupo6.trego.data.notificaciones.PushNotificationManager
import com.grupo6.trego.data.remote.AuthApiService
import com.grupo6.trego.data.remote.CarritoApiService
import com.grupo6.trego.data.remote.PedidoApiService
import com.grupo6.trego.data.remote.RestaurantApiService
import com.grupo6.trego.data.remote.UsuarioApiService
import com.grupo6.trego.data.repository.CloudinaryRepository
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.RestauranteRepository
import com.grupo6.trego.data.repository.UsuarioRepository
import com.grupo6.trego.data.utilities.AuthInterceptor
import com.grupo6.trego.data.utilities.LocalDateTimeAdapter
import com.grupo6.trego.data.utilities.LocalTimeAdapter
import com.grupo6.trego.data.utilities.TokenManager
import com.grupo6.trego.ui.auth.AuthViewModel
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.menu.MenuViewModel
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.restaurantes.RestauranteViewModel
import com.grupo6.trego.ui.usuario.PerfilViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.LocalTime

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
    single { get<Retrofit>().create(UsuarioApiService::class.java) }

    // Repositorios
    single { RestauranteRepository(get()) }
    single { CarritoRepository(get()) }
    single { PedidoRepository(get()) }
    single { UsuarioRepository(get()) }
    single { CloudinaryRepository(get(), get()) }
    single { PushNotificationManager() }

    // ViewModels
    viewModel { RestauranteViewModel(get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { CarritoViewModel(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { PedidoViewModel(get(), get(), get()) }
    viewModel { PerfilViewModel(get(), get()) }

}
