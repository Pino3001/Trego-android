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
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.grupo6.trego.ui.theme.TregoTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    val pendingPaymentStatus = MutableStateFlow<String?>(null)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Pedir permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Ver que hacer si el usuario rechaza las notificaciones
            }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            TregoTheme {
                // Quitamos el Scaffold de aquí para que lo maneje AppNavigation internamente
                AppNavigation(pendingPaymentStatus)
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    // Fuera de onCreate, como miembro privado de la clase
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "trego" && uri.host == "pago") {
            val status = uri.lastPathSegment
            Log.d("DeepLink", "Status guardado: $status")
            if (status != null) {
                pendingPaymentStatus.value = status
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TregoTheme {
        AppNavigation()
    }
}
