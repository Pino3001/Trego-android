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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.grupo6.trego.data.model.NavigationTarget
import com.grupo6.trego.data.utilities.AppReadyState
import com.grupo6.trego.ui.theme.TregoTheme
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Esta es la actividad principal y el punto de entrada de la aplicación. 
 * Se encarga de mostrar el splash screen, pedir los permisos de notificaciones 
 * y procesar cualquier dato que venga de afuera, como los deep links de pagos 
 * o los avisos de Firebase.
 */
class MainActivity : ComponentActivity() {

    val pendingPaymentStatus = MutableStateFlow<String?>(null)
    val navigationTarget = MutableStateFlow<NavigationTarget?>(null)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val startTime = System.currentTimeMillis()

        /* Configuramos el Splash Screen para que se quede visible hasta que los datos iniciales de la app estén listos para el usuario. */
        splashScreen.setKeepOnScreenCondition {
            val user = FirebaseAuth.getInstance().currentUser
            val elapsed = System.currentTimeMillis() - startTime

            if (user == null || elapsed > 5000) {
                // Si no hay sesión o pasaron 5 segundos (timeout), soltamos el splash
                false
            } else {
                // Si hay sesión, esperamos a que el backend de restaurantes responda
                !AppReadyState.isDataReady.value
            }
        }

        super.onCreate(savedInstanceState)

        /* Pedimos permiso para mandar notificaciones en las versiones de Android que lo requieren obligatoriamente. */
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
                AppNavigation(pendingPaymentStatus, navigationTarget)
            }
        }

        handleDeepLink(intent)
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
        handleNotificationIntent(intent)
    }

    /* Analizamos la URL que viene de Mercado Pago para saber si el pago se completó bien o si hubo algún problema. */
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "trego" && uri.host == "pago") {
            val status = uri.lastPathSegment
            Log.d("DeepLink", "Status guardado: $status")
            if (status != null) {
                pendingPaymentStatus.value = status
            }
        }
        intent.data = null
    }

    /* Cuando el usuario toca una notificación, miramos qué estado trae para mandarlo directo a la pantalla que corresponde. */
    private fun handleNotificationIntent(currentIntent: Intent?) {
        currentIntent?.let {
            if (it.hasExtra("estado")) {
                val estado = it.getStringExtra("estado")

                Log.e("estados", estado.toString())
                if (estado == "PAGO_PROCESADO") {
                    navigationTarget.value = NavigationTarget.PAGO_REALIZADO
                }
                else if (estado == "Reembolsado" || estado == "Resuelto") {
                    navigationTarget.value = NavigationTarget.HISTORIAL
                } else {
                    navigationTarget.value = NavigationTarget.PEDIDO
                }
                it.removeExtra("estado")
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
