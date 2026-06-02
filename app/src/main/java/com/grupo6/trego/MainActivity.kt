package com.grupo6.trego

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.theme.TregoTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    // ← NUEVO: misma instancia que usa CarritoScreen (mismo ViewModelStore)
    private val carritoViewModel: CarritoViewModel by viewModel()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pedir permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // El usuario permitió enviar el token al back para mandar notificaciones
                } else {
                    // El usuario no permitió las notificaciones
                }
            }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Obtener el Token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_POC", "Fallo al obtener el token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_POC", "TU TOKEN ACTUAL ES: $token")
        }

        setContent {
            TregoTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color(0xFFE1DBDB)) { innerPadding ->
                    AppNavigation()
                }
            }
        }
    }

    // ← NUEVO: captura el deep link cuando el usuario vuelve de MercadoPago
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // actualiza el intent activo de la Activity
        val uri = intent.data ?: return
        if (uri.scheme == "trego" && uri.host == "pago") {
            when (uri.lastPathSegment) {
                "exito"     -> carritoViewModel.marcarPagoExitoso()
                "rechazado" -> carritoViewModel.marcarPagoRechazado()
                "pendiente" -> carritoViewModel.marcarPagoPendiente()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TregoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppNavigation()
        }
    }
}