package com.grupo6.trego.ui.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.grupo6.trego.data.model.GoogleLoginRequest
import com.grupo6.trego.data.model.SmsLoginRequest
import com.grupo6.trego.data.remote.AuthApiService
import com.grupo6.trego.data.repository.UsuarioRepository
import com.grupo6.trego.data.utilities.TokenManager
import kotlinx.coroutines.launch

/**
 * Este ViewModel es el cerebro de la autenticación. Se encarga de manejar los estados
 * del login por SMS y Google, guardar los tokens que nos da el servidor y registrar
 * el dispositivo para las notificaciones apenas el usuario entra.
 */
class AuthViewModel(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    var phoneNumber by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var step by mutableStateOf(AuthStep.PHONE)
    var verificationId by mutableStateOf("")

    fun onPhoneChange(value: String) {
        phoneNumber = value
    }

    fun onOtpChange(value: String) {
        if (value.length <= 6) otpCode = value
    }

    fun onCodeSent(vId: String) {
        verificationId = vId
        isLoading = false
        step = AuthStep.VERIFY
    }

    fun onVerificationFailed(message: String?) {
        errorMessage = message
        isLoading = false
    }

    fun onChangeNumber() {
        step = AuthStep.PHONE
        otpCode = ""
        errorMessage = null
    }

    fun setError(message: String?) {
        errorMessage = message
    }

    /* Mandamos el token que nos dio Firebase al servidor para validar el login por SMS y guardar la sesión. */
    fun sendSMSTokenToBackend(
        firebaseToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = SmsLoginRequest(firebaseToken = firebaseToken)
                val response = authApiService.loginConSMS(request)

                if (response.isSuccessful && response.body() != null) {
                    val jwtDelBackend = response.body()!!.jwtToken
                    tokenManager.saveToken(jwtDelBackend)
                    obtenerYRegistrarFcmToken()
                    onSuccess()
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: ""
                    errorMessage = "Error del servidor (${response.code()}): $errorBodyString"
                    onError(errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage ?: e.message}"
                onError(errorMessage!!)
            } finally {
                isLoading = false
            }
        }
    }

    /* Hacemos lo mismo pero para los usuarios que prefieren entrar con su cuenta de Google. */
    fun sendGoogleTokenToBackend(
        idToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = GoogleLoginRequest(idToken = idToken)
                val response = authApiService.loginConGoogle(request)

                if (response.isSuccessful && response.body() != null) {
                    val jwtDelBackend = response.body()!!.jwtToken
                    tokenManager.saveToken(jwtDelBackend)
                    obtenerYRegistrarFcmToken()
                    onSuccess()
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: ""
                    errorMessage = "Error de servidor: $errorBodyString"
                    onError(errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "No se pudo conectar al servidor"
                onError(errorMessage!!)
            } finally {
                isLoading = false
            }
        }
    }

    /* Validamos el código de 6 dígitos que el usuario escribe cuando le llega el SMS. */
    fun verificarCodigoManual(auth: FirebaseAuth, onSuccess: () -> Unit) {
        isLoading = true

        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                result.user?.getIdToken(false)?.addOnSuccessListener { tokenResult ->
                    val firebaseToken = tokenResult.token ?: return@addOnSuccessListener

                    sendSMSTokenToBackend(
                        firebaseToken = firebaseToken,
                        onSuccess = onSuccess,
                        onError = {
                            // Si el backend falla, cerramos sesión de Firebase
                            auth.signOut()
                        }
                    )
                }
            }
            .addOnFailureListener {
                onVerificationFailed(it.message ?: "Código incorrecto")
            }
    }

    /* Apenas logueamos, le pedimos a Firebase su token de notificaciones y se lo pasamos al backend. */
    private fun obtenerYRegistrarFcmToken() {
        Log.d("FCM_AUTH", "Antes del getInstance de firebaseMessaging")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM_AUTH", "Token obtenido tras login: $fcmToken")

                // Lanzamos corrutina en el scope del ViewModel para la petición de red
                viewModelScope.launch {
                    val resultado = usuarioRepository.actualizarFcmToken(fcmToken)
                    if (resultado.isSuccess) {
                        Log.d("FCM_AUTH", "Token FCM registrado exitosamente en el Backend.")
                    } else {
                        // Nota: Si esto falla, el usuario igual está logueado en la app,
                        // pero, noo recibirá notificaciones hasta que se vuelva a intentar.
                        Log.e("FCM_AUTH", "Error registrando FCM", resultado.exceptionOrNull())
                    }
                }
            } else {
                Log.e("FCM_AUTH", "No se pudo obtener el token desde Firebase", task.exception)
            }
        }
    }
}