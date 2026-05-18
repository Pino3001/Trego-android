package com.grupo6.trego.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.ui.data.remote.RetrofitClient
import com.grupo6.trego.ui.data.remote.TokenRequest
import kotlinx.coroutines.launch


class PhoneAuthViewModel : ViewModel() {

    var phoneNumber by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var step by mutableStateOf(AuthStep.PHONE)
    var verificationId by mutableStateOf("")

    fun onPhoneChange(value: String) { phoneNumber = value }
    fun onOtpChange(value: String) { if (value.length <= 6) otpCode = value }

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

    fun setError(message: String?) { errorMessage = message }

    fun sendTokenToBackend(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.verifyToken(TokenRequest(idToken))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    errorMessage = "Error del servidor: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}

