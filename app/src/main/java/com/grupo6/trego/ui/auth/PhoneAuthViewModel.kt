package com.grupo6.trego.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.grupo6.trego.data.model.GoogleLoginRequest
import com.grupo6.trego.data.model.SmsLoginRequest
import com.grupo6.trego.data.remote.AuthApiService
import com.grupo6.trego.data.utilities.TokenManager
import kotlinx.coroutines.launch

class PhoneAuthViewModel(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
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

    // Enviar token firebase al backend con login vía SMS
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
}