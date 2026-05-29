package com.grupo6.trego.data.utilities

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

sealed class LocationState {
    object Idle : LocationState()
    object RequestingPermission : LocationState()
    object PermissionDenied : LocationState()
    object LocationUnavailable : LocationState()
    data class Available(val lat: Double, val lon: Double) : LocationState()
}

/**
 * Pide el permiso de ubicación y devuelve las coordenadas via [onStateChange].
 *
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
                            onStateChange(LocationState.Available(location.latitude, location.longitude))
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