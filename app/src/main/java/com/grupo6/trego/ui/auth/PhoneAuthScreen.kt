package com.grupo6.trego.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.grupo6.trego.ui.theme.TregoOrange
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit


enum class AuthStep { PHONE, VERIFY }

@Composable
fun PhoneAuthScreen(
    onAuthSuccess: () -> Unit
) {
    val viewModel: PhoneAuthViewModel = koinViewModel()
    val auth = remember { FirebaseAuth.getInstance() }
    val activity = LocalContext.current as Activity

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.isLoading = true
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        result.user?.getIdToken(false)?.addOnSuccessListener { tokenResult ->
                            val firebaseToken = tokenResult.token ?: return@addOnSuccessListener

                            // 🚀 ACTUALIZADO: Agregamos el onError para manejar la caída del backend
                            viewModel.sendSMSTokenToBackend(
                                firebaseToken = firebaseToken,
                                onSuccess = { onAuthSuccess() },
                                onError = {
                                    // Si el backend está apagado, cerramos sesión en Firebase
                                    // para no quedar trabados en el inicio automático
                                    auth.signOut()
                                }
                            )
                        }
                    }
                    .addOnFailureListener { viewModel.onVerificationFailed(it.message) }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                viewModel.onVerificationFailed(e.message)
            }

            override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                viewModel.onCodeSent(vId)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // <-- Esto hace la magia del centrado
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5) // ¡Aquí se asigna el color correctamente!
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Lo Pedis, Trego",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TregoOrange
                )
                Spacer(Modifier.height(24.dp))
                StepIndicator(currentStep = viewModel.step)
                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = viewModel.step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "auth_step"
                ) { targetStep ->
                    when (targetStep) {
                        AuthStep.PHONE -> PhoneStep(viewModel, auth, activity, callbacks)
                        AuthStep.VERIFY -> VerifyStep(
                            viewModel,
                            auth,
                            onAuthSuccess
                        ) // 👈 Limpio y directo
                    }
                }

                viewModel.errorMessage?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.onError, fontSize = 13.sp)
                }
            }
        }
    }

}

//componente de burbujas que indican el lugar donde se encuentra el usr -numero/codigo-
@Composable
fun StepIndicator(currentStep: AuthStep) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepBubble(number = "1", label = "Teléfono", active = currentStep == AuthStep.PHONE)
        HorizontalDivider(modifier = Modifier.width(60.dp))
        StepBubble(number = "2", label = "Verificar", active = currentStep == AuthStep.VERIFY)
    }
}

// Burbuja de numero de telefono a codigo de verificacion
@Composable
fun StepBubble(number: String, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (active) TregoOrange else Color.LightGray,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(label, fontSize = 11.sp, color = if (active) TregoOrange else Color.Gray)
    }
}

// Componente de ingreso del numero de telefono
@Composable
private fun PhoneStep(
    viewModel: PhoneAuthViewModel,
    auth: FirebaseAuth,
    activity: Activity,
    callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Iniciar sesión por SMS", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            "Ingresá tu número para recibir un código por SMS",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.phoneNumber,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Número de teléfono") },
            placeholder = { Text("+598 99 000 000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                // Texto principal que escribe el usuario
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.DarkGray,

                // Texto de la etiqueta (Label) "Número de teléfono"
                focusedLabelColor = TregoOrange, // Cuando el usuario hace clic cambia a tu naranja
                unfocusedLabelColor = Color.Gray,

                // Texto del ejemplo (Placeholder) "+598..."
                focusedPlaceholderColor = Color.LightGray,
                unfocusedPlaceholderColor = Color.LightGray,

                // Color de los bordes del OutlinedTextField
                focusedIndicatorColor = TregoOrange, // Borde naranja al seleccionar
                unfocusedIndicatorColor = Color.Gray, // Borde gris en reposo

                // Color de fondo interno (por si quieres quitar el gris por defecto)
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.isLoading = true
                viewModel.setError(null)
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(viewModel.phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            },
            enabled = viewModel.phoneNumber.isNotBlank() && !viewModel.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TregoOrange,          // Color cuando está habilitado
                disabledContainerColor = Color(0xFF424242), // 🎨 Gris oscuro (Charcoal) cuando está deshabilitado
                disabledContentColor = Color(0xFF9E9E9E)   // 🎨 Opcional: Color del texto/icono cuando está deshabilitado
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (viewModel.isLoading)
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else
                Text("Enviar código", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun VerifyStep(
    viewModel: PhoneAuthViewModel,
    auth: FirebaseAuth,
    onAuthSuccess: () -> Unit,
) {
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verificar código", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("Código enviado a", fontSize = 13.sp, color = Color.Gray)
        Text(viewModel.phoneNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.otpCode,
            onValueChange = viewModel::onOtpChange,
            label = { Text("Código de verificación") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.verificarCodigoManual(
                    auth = auth,
                    onSuccess = onAuthSuccess
                )
            },
            enabled = viewModel.otpCode.length == 6 && !viewModel.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Verificando...", color = Color.White)
            } else {
                Text("Verificar", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = viewModel::onChangeNumber,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TregoOrange)
        ) {
            Text("Cambiar número")
        }
    }
}