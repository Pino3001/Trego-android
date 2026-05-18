package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.grupo6.trego.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FormInicio(onLoginSuccess: () -> Unit) {
// Obtiene el Contexto de Android necesario para funciones del sistema (como diálogos)
    val context = LocalContext.current

// Crea un "Scope" para ejecutar procesos en segundo plano y no bloquear la pantalla, no es obligatorio!
    val scope = rememberCoroutineScope()
// Instancia la herramienta principal de Firebase Auth para gestionar la sesión
    val auth = Firebase.auth
// Crea la ventana del administrador de credenciales de Google
    val credentialManager = CredentialManager.create(context)
// Variable de estado que controla si el proceso está en curso (sirve para mostrar un indicador de carga)
    var isLoading by remember { mutableStateOf(false) }

    fun registrarUsuarioGoogle() {
        // Evita que el usuario dispare múltiples veces el proceso si ya está cargando
        if (!isLoading) {
            isLoading = true

            // Inicia el proceso en segundo plano
            scope.launch {
                try {
                    // Prepara la opción de inicio con Google usando tu ID de cliente del backend
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false) // Permite elegir cualquier cuenta, no solo las ya autorizadas
                        .setServerClientId("360954493024-4l2rn17bngm5rjhdnrpnh10u6jbdel0i.apps.googleusercontent.com") // El id proviente del archivo google-services.json
                        .build()

                    // Empaqueta la opción de Google en una petición general de credenciales
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    // --- INTERACCIÓN CON EL USUARIO ---
                    // Muestra el selector nativo de cuentas de Google y espera a que el usuario elija una
                    val result = credentialManager.getCredential(
                        context = context,
                        request = request
                    )

                    // --- CONVERSIÓN DE CREDENCIALES ---
                    // Convierte la respuesta del sistema en una credencial de ID de Google legible
                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(result.credential.data)

                    // Crea la credencial específica que Firebase entiende usando el Token de Google
                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(googleCredential.idToken, null)

                    // --- AUTENTICACIÓN FINAL ---
                    // Envía la credencial a Firebase para iniciar sesión y espera la confirmación (.await())
                    val authResult = auth.signInWithCredential(firebaseCredential).await()

                    // Si Firebase confirma que hay un usuario válido, ejecutamos la navegación
                    if (authResult.user != null) {
                        onLoginSuccess() // Navegamos hacia la siguiente pagina
                    }

                } catch (e: Exception) {
                    println("Error en Auth: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp), // Un poco más de aire
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Lo Pedis, Trego!",
            style = MaterialTheme.typography.displaySmall,
            color = Color.Black // Texto en negro para que resalte
        )

        Spacer(modifier = Modifier.height(26.dp))

        Image(
            painter = painterResource(id = R.drawable.bolsa), // 'R.drawable.nombre_archivo'
            contentDescription = "Logo de la aplicación Trego", // Importante para accesibilidad (alt text)
            modifier = Modifier
                .size(250.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically // Centra el texto y las líneas verticalmente
        ) {
            // Primera línea
            HorizontalDivider(
                modifier = Modifier.weight(1f), // Ocupa el espacio disponible a la izquierda
                thickness = 1.dp,
                color = Color(0xFFB4A9A9)
            )

            // Texto central
            Text(
                text = "Elige el metodo de inicio",
                modifier = Modifier.padding(horizontal = 16.dp), // Espacio a los lados del texto
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black
            )

            // Segunda línea
            HorizontalDivider(
                modifier = Modifier.weight(1f), // Ocupa el espacio disponible a la derecha
                thickness = 1.dp,
                color = Color(0xFFB4A9A9)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        BotonInicio(
            text = if (isLoading) "Cargando..." else "Iniciar con Google",
            iconRes = R.drawable.brand_google,
            onClick = {
                registrarUsuarioGoogle()
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        BotonInicio(
            text = "Iniciar con el celular",
            iconRes = R.drawable.device_mobile,
            onClick = { /* Lógica de Firebase o Google Auth */ }
        )

        Spacer(modifier = Modifier.height(36.dp))


        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Al continuar, aceptas nuestros Términos y Condiciones",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray // Gris queda mejor para footers legales o secundarios
        )

    }
}