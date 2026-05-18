package com.grupo6.trego

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import com.grupo6.trego.AppNavigation
import com.grupo6.trego.ui.theme.TregoTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Pedir permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // El usuario permitio enviar el token al back para mandar notificaciones
                } else {
                    // El usuario no permitio las notificaciones
                }
            }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Obtener el Token para la prueba
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_POC", "Fallo al obtener el token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            // ESTE ES EL TOKEN QUE COPIARÁS A FIREBASE
            Log.d("FCM_POC", "TU TOKEN ACTUAL ES: $token")
        }

        setContent {
            TregoTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color(0xFFFDF6F6))
                { innerPadding ->
                    AppNavigation()
                }
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