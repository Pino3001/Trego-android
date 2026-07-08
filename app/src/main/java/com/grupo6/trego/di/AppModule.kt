package com.grupo6.trego.di

import CarritoRepository
import com.google.gson.GsonBuilder
import com.grupo6.trego.data.notificaciones.PushNotificationManager
import com.grupo6.trego.data.remote.AuthApiService
import com.grupo6.trego.data.remote.CarritoApiService
import com.grupo6.trego.data.remote.PedidoApiService
import com.grupo6.trego.data.remote.ProductosApiService
import com.grupo6.trego.data.remote.RestaurantApiService
import com.grupo6.trego.data.remote.SubCategoriaApiService
import com.grupo6.trego.data.remote.UsuarioApiService
import com.grupo6.trego.data.repository.CloudinaryRepository
import com.grupo6.trego.data.repository.LocationRepository
import com.grupo6.trego.data.repository.PedidoRepository
import com.grupo6.trego.data.repository.ProductoRepository
import com.grupo6.trego.data.repository.RestauranteRepository
import com.grupo6.trego.data.repository.SubcategoriaRepository
import com.grupo6.trego.data.repository.UsuarioRepository
import com.grupo6.trego.data.utilities.AuthInterceptor
import com.grupo6.trego.data.utilities.LocalDateTimeAdapter
import com.grupo6.trego.data.utilities.LocalTimeAdapter
import com.grupo6.trego.data.utilities.TokenManager
import com.grupo6.trego.ui.auth.AuthViewModel
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.menu.MenuViewModel
import com.grupo6.trego.ui.pedidos.PedidoViewModel
import com.grupo6.trego.ui.home.restaurantes.RestauranteViewModel
import com.grupo6.trego.ui.home.platos.SubCategoriaViewModel
import com.grupo6.trego.ui.home.platos.PlatoViewModel
import com.grupo6.trego.ui.home.HomeViewModel
import com.grupo6.trego.ui.home.ofertas.OfertaViewModel
import com.grupo6.trego.ui.usuario.PerfilViewModel
import com.grupo6.trego.ui.usuario.MetodosAccesoViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.jvm.java

/**
 * En este archivo configuramos toda la inyección de dependencias de la app usando Koin.
 * Básicamente, acá definimos cómo se crean y se comparten los servicios, repositorios 
 * y ViewModels para que todo el sistema funcione de forma organizada.
 */
val appModule = module {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /* Guardamos el token de sesión de forma persistente en el dispositivo. */
    single { TokenManager(androidContext()) }

    /* Interceptamos las llamadas para meter el token de autorización automáticamente. */
    single { AuthInterceptor(androidContext(), get()) }

    /* Configuramos el cliente de red con seguridad y registro de llamadas. */
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(logging)
            .build()
    }

    /* Le enseñamos a Gson a entender los formatos de fecha que usa el servidor. */
    single {
        GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()
    }

    /* Definimos la base de nuestra conexión con la API del backend. */
    single {
        Retrofit.Builder()
            .baseUrl("http://ec2-3-84-203-146.compute-1.amazonaws.com/api/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    /* Creamos las instancias de todos los servicios de red definidos. */
    single { get<Retrofit>().create(AuthApiService::class.java) }
    single { get<Retrofit>().create(RestaurantApiService::class.java) }
    single { get<Retrofit>().create(CarritoApiService::class.java) }
    single { get<Retrofit>().create(PedidoApiService::class.java) }
    single { get<Retrofit>().create(UsuarioApiService::class.java) }
    single { get<Retrofit>().create(SubCategoriaApiService::class.java) }
    single { get<Retrofit>().create(ProductosApiService::class.java) }

    // Repositorios
    single { RestauranteRepository(get()) }
    single { CarritoRepository(get()) }
    single { PedidoRepository(get()) }
    single { UsuarioRepository(get()) }
    single { CloudinaryRepository(get(), get()) }
    single { PushNotificationManager() }
    single { SubcategoriaRepository(get()) }
    single { ProductoRepository(get()) }
    single { LocationRepository() }


    // ViewModels
    viewModel { RestauranteViewModel(get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { CarritoViewModel(get(), get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { PedidoViewModel(get(), get(), get()) }
    viewModel { PerfilViewModel(get(), get()) }
    viewModel { SubCategoriaViewModel(get()) }
    viewModel { PlatoViewModel(get()) }
    viewModel { OfertaViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { MetodosAccesoViewModel(get()) }

}
