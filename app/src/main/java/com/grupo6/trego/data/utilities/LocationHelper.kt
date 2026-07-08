package com.grupo6.trego.data.utilities

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.ResultadoGeoapify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/**
 * Acá tenemos varias herramientas para manejar la ubicación: desde pedir los permisos
 * necesarios al usuario, hasta convertir coordenadas en direcciones reales usando Geoapify.
 */
sealed class LocationState {
    object Idle : LocationState()
    object RequestingPermission : LocationState()
    object PermissionDenied : LocationState()
    object LocationUnavailable : LocationState()
    data class Available(val lat: Double, val lon: Double) : LocationState()
}

/**
 * Pide el permiso de ubicación y devuelve las coordenadas via [onStateChange].
 * @param retryKey  Cambiá este valor para forzar un reintento (ej: un contador que
 *                  incrementás al tocar "Reintentar" o al volver de Configuración).
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocation(
    retryKey: Int = 0,
    onStateChange: (LocationState) -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    when (val status = permissionState.status) {

        is PermissionStatus.Granted -> {
            // retryKey como key: se re-ejecuta cada vez que el padre lo incrementa
            LaunchedEffect(retryKey) {
                onStateChange(LocationState.RequestingPermission)

                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationTokenSource = CancellationTokenSource()

                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()

                fusedClient.getCurrentLocation(request, cancellationTokenSource.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            onStateChange(
                                LocationState.Available(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        } else {
                            onStateChange(LocationState.LocationUnavailable)
                        }
                    }
                    .addOnFailureListener {
                        onStateChange(LocationState.LocationUnavailable)
                    }
            }
        }

        is PermissionStatus.Denied -> {
            if (status.shouldShowRationale) {
                LaunchedEffect(Unit) {
                    onStateChange(LocationState.PermissionDenied)
                }
            } else {
                LaunchedEffect(retryKey) {
                    onStateChange(LocationState.RequestingPermission)
                    permissionState.launchPermissionRequest()
                }
            }
        }
    }
}

// Función de extensión para llamar a Geoapify
suspend fun reverseGeocode(lat: Double, lon: Double): DTODireccion? {
    return try {
        val apiKey =
            "d8f9a863d62a4f869bb5cac555b14ef3" // Ojo: en producción es mejor tener esto en un archivo de configuración/secrets
        val url = "https://api.geoapify.com/v1/geocode/reverse?lat=$lat&lon=$lon&apiKey=$apiKey"
        val response = withContext(Dispatchers.IO) {
            java.net.URL(url).readText()
        }
        val json = org.json.JSONObject(response)
        val props = json
            .getJSONArray("features")
            .getJSONObject(0)
            .getJSONObject("properties")

        val rawHouseNumber = props.optString("housenumber", "")
        val numeroLimpio = rawHouseNumber
            .split(";", ",")
            .firstOrNull()
            ?.trim() ?: ""

        DTODireccion(
            calle = props.optString("street", null.toString()).takeIf { it != "null" },
            numero = numeroLimpio, // Pasamos el número ya formateado
            esquina = null,
            latitud = lat,
            longitud = lon
        )
    } catch (e: Exception) {
        null
    }
}

suspend fun autocompletarDireccion(query: String): List<ResultadoGeoapify> {
    return try {
        val apiKey = "d8f9a863d62a4f869bb5cac555b14ef3"
        val q = java.net.URLEncoder.encode(query, "UTF-8")

        // Se agrega limit=5 y filter=countrycode:uy para hacer la búsqueda mas rapida
        val url =
            "https://api.geoapify.com/v1/geocode/autocomplete?text=$q&lang=es&limit=5&filter=countrycode:uy&bias=proximity:-56.1645,-34.9011&apiKey=$apiKey"

        // Poner un límite de tiempo para que no se quede colgado
        val response = withTimeout(3000L.milliseconds) {
            withContext(Dispatchers.IO) { java.net.URL(url).readText() }
        }

        val features = org.json.JSONObject(response).getJSONArray("features")
        (0 until features.length()).map { i ->
            val props = features.getJSONObject(i).getJSONObject("properties")
            ResultadoGeoapify(
                descripcion = props.optString("formatted", ""),
                calle = props.optString("street").takeIf { it.isNotEmpty() && it != "null" },
                numero = props.optString("housenumber").takeIf { it.isNotEmpty() && it != "null" },
                ciudad = props.optString("city").takeIf { it.isNotEmpty() && it != "null" },
                latitud = props.optDouble("lat", 0.0),
                longitud = props.optDouble("lon", 0.0)
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}