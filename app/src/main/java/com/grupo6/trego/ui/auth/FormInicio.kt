package com.grupo6.trego.ui.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.grupo6.trego.R
import com.grupo6.trego.ui.auth.componentes.BotonInicio
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FormInicio(
    onLoginSuccess: () -> Unit,
    onNavigateToPhone: () -> Unit,
    viewModel: AuthViewModel
) {
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

    // ESTADO PARA LA ALERTA DE ERROR
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red) },
            title = { Text("Error de Inicio de Sesión", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("Reintentar", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    fun registrarUsuarioGoogle() {
        val TAG = "GoogleAuthDebug" // 👈 Etiqueta para filtrar en el Logcat

        if (!isLoading) {
            isLoading = true

            scope.launch {
                try {
                    val serverClientId =
                        "360954493024-4l2rn17bngm5rjhdnrpnh10u6jbdel0i.apps.googleusercontent.com"

                    // 1er intento: cuentas ya autorizadas en el dispositivo (bottom sheet rápido).
                    // Evita el "dispositivo no válido" cuando ya hay una cuenta vinculada.
                    val result: GetCredentialResponse = try {
                        Log.d(TAG, "🟢 1. Abriendo selector con cuentas autorizadas...")
                        val authorizedOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(true)
                            .setServerClientId(serverClientId)
                            .setAutoSelectEnabled(false)
                            .build()
                        credentialManager.getCredential(
                            context = context,
                            request = GetCredentialRequest.Builder()
                                .addCredentialOption(authorizedOption)
                                .build()
                        )
                    } catch (e: NoCredentialException) {
                        // Fallback: botón "Sign in with Google", muestra TODAS las cuentas.
                        Log.d(TAG, "🟡 Sin cuentas autorizadas, usando GetSignInWithGoogleOption")
                        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
                        credentialManager.getCredential(
                            context = context,
                            request = GetCredentialRequest.Builder()
                                .addCredentialOption(signInOption)
                                .build()
                        )
                    }

                    val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                    // 🔍 LOG 1: Verificamos si Google nos dio su token inicial (IdToken)
                    Log.d(TAG, "✅ 2. ¡Google obtuvo el Token con éxito!")
                    Log.d(TAG, "   👉 Correo del usuario: ${googleCredential.id}")
                    Log.d(TAG, "   👉 Google IdToken (primeros 30 caracteres): ${googleCredential.idToken.take(30)}...")

                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

                    Log.d(TAG, "🔄 3. Iniciando sesión en Firebase con la credencial de Google...")
                    val authResult = auth.signInWithCredential(firebaseCredential).await()

                    if (authResult.user != null) {
                        val idTokenResult = authResult.user!!.getIdToken(false).await()
                        val firebaseTokenString = idTokenResult.token

                        if (firebaseTokenString != null) {

                            // 🔍 LOG 2: Este es el token que viaja a tu backend
                            Log.d(TAG, "🚀 4. ¡Token de Firebase generado listo para enviar al Backend!")
                            Log.d(TAG, "   👉 Token Completo (Cópialo si lo necesitas para Postman):")
                            Log.d(TAG, "   👉 $firebaseTokenString")

                            viewModel.sendGoogleTokenToBackend(
                                idToken = firebaseTokenString,
                                onSuccess = {
                                    Log.d(TAG, "🎉 5. El Backend respondió EXITOSAMENTE. Logueado.");
                                    isLoading = false
                                    onLoginSuccess()
                                },
                                onError = { mensajeError ->
                                    Log.e(TAG, "❌ Fallo en la respuesta del Backend: $mensajeError")
                                    isLoading = false
                                    errorMessage = "El servidor no reconoció tu cuenta: $mensajeError"
                                    auth.signOut()
                                }
                            )
                        } else {
                            Log.e(TAG, "❌ El token string de Firebase vino nulo")
                            isLoading = false
                        }
                    }

                } catch (e: NoCredentialException) {
                    // El usuario llego al selector pero Google NO emitio token.
                    Log.e(TAG, "❌ NoCredentialException (revisar SHA-1 en Firebase): ${e.message}", e)
                    errorMessage = "Google no pudo validar la app. Verificá que el SHA-1 de la " +
                            "firma esté registrado en Firebase."
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "❌ ${e.javaClass.simpleName} - type=${e.type} - ${e.message}", e)
                    errorMessage = "Error de Google (${e.javaClass.simpleName}): ${e.message}"
                } catch (e: Exception) {
                    // 🔍 LOG 3: Si el selector de Google se cae o se cierra solo, saltará aquí
                    Log.e(TAG, "❌ ERROR CRÍTICO en el proceso de Autenticación: ${e.message}", e)
                    errorMessage = "Error de conexión: ${e.localizedMessage ?: "No se pudo conectar con Google"}"
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
            onClick = { onNavigateToPhone() }
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