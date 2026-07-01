package com.grupo6.trego.ui.usuario

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.grupo6.trego.data.model.VincularRequest
import com.grupo6.trego.data.remote.AuthApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

enum class SmsLinkStep { IDLE, PHONE, CODE }

class MetodosAccesoViewModel(
    private val authApiService: AuthApiService
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
    var mensajeExito by mutableStateOf<String?>(null)

    var smsStep by mutableStateOf(SmsLinkStep.IDLE)
        private set
    var phoneNumber by mutableStateOf("")
    var otpCode by mutableStateOf("")

    private var verificationId by mutableStateOf("")
    private var pendienteOnVinculado: (() -> Unit)? = null

    private val serverClientId =
        "360954493024-4l2rn17bngm5rjhdnrpnh10u6jbdel0i.apps.googleusercontent.com"

    fun onPhoneChange(value: String) {
        phoneNumber = value
    }

    fun onOtpChange(value: String) {
        if (value.length <= 6) otpCode = value
    }

    fun iniciarSms() {
        error = null
        mensajeExito = null
        smsStep = SmsLinkStep.PHONE
    }

    fun cancelarSms() {
        smsStep = SmsLinkStep.IDLE
        phoneNumber = ""
        otpCode = ""
        verificationId = ""
        error = null
    }

    fun vincularGoogle(context: Context, onVinculado: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            error = "Necesitás haber iniciado sesión para vincular un método."
            return
        }
        error = null
        mensajeExito = null
        isLoading = true

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val result: GetCredentialResponse = try {
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
                    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
                    credentialManager.getCredential(
                        context = context,
                        request = GetCredentialRequest.Builder()
                            .addCredentialOption(signInOption)
                            .build()
                    )
                }

                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val firebaseCredential =
                    GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                user.linkWithCredential(firebaseCredential).await()

                val token = user.getIdToken(true).await().token
                if (token == null) {
                    error = "No se pudo obtener el token de Firebase."
                    isLoading = false
                    return@launch
                }
                finalizarVinculacion(token, "Google vinculado correctamente.", onVinculado)
            } catch (e: Exception) {
                error = traducirFirebase(e)
                isLoading = false
            }
        }
    }

    fun enviarCodigoSms(activity: Activity, onVinculado: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            error = "Necesitás haber iniciado sesión para vincular un método."
            return
        }
        error = null
        mensajeExito = null
        isLoading = true
        pendienteOnVinculado = onVinculado

        // ojo: esto es para los numeros de prueba de firebase, sacar antes de subir a prod
        FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(true)

        val numero = if (phoneNumber.startsWith("+")) phoneNumber
        else "+598${phoneNumber.removePrefix("0")}"

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                vincularConCredencialTelefono(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                error = traducirFirebase(e)
                isLoading = false
            }

            override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = vId
                isLoading = false
                smsStep = SmsLinkStep.CODE
            }
        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(numero)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun confirmarCodigoSms(onVinculado: () -> Unit) {
        if (otpCode.length != 6) {
            error = "Ingresá el código de 6 dígitos."
            return
        }
        pendienteOnVinculado = onVinculado
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        vincularConCredencialTelefono(credential)
    }

    private fun vincularConCredencialTelefono(credential: PhoneAuthCredential) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            error = "Necesitás haber iniciado sesión para vincular un método."
            isLoading = false
            return
        }
        error = null
        isLoading = true

        viewModelScope.launch {
            try {
                user.linkWithCredential(credential).await()
                val token = user.getIdToken(true).await().token
                if (token == null) {
                    error = "No se pudo obtener el token de Firebase."
                    isLoading = false
                    return@launch
                }
                finalizarVinculacion(token, "Teléfono vinculado correctamente.") {
                    smsStep = SmsLinkStep.IDLE
                    phoneNumber = ""
                    otpCode = ""
                    verificationId = ""
                    pendienteOnVinculado?.invoke()
                }
            } catch (e: Exception) {
                error = traducirFirebase(e)
                isLoading = false
            }
        }
    }

    private suspend fun finalizarVinculacion(
        firebaseToken: String,
        mensaje: String,
        onVinculado: () -> Unit
    ) {
        try {
            val response = authApiService.vincularProveedor(VincularRequest(firebaseToken))
            if (response.isSuccessful) {
                mensajeExito = mensaje
                onVinculado()
            } else {
                error = when (response.code()) {
                    409 -> "Ese método ya está asociado a otra cuenta de Trego."
                    403 -> "Tu cuenta se encuentra deshabilitada."
                    401 -> "No se pudo validar el método. Intentá de nuevo."
                    else -> "El servidor respondió con un error."
                }
            }
        } catch (e: Exception) {
            error = "Error de conexión: ${e.localizedMessage ?: e.message}"
        } finally {
            isLoading = false
        }
    }

    private fun traducirFirebase(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("credential-already-in-use") ->
                "Ese método ya está vinculado a otra cuenta."
            msg.contains("already associated") ->
                "Ese método ya está vinculado a otra cuenta."
            msg.contains("provider-already-linked") ->
                "Ese método ya está vinculado a tu cuenta."
            msg.contains("email-already-in-use") ->
                "Ese correo ya está en uso por otra cuenta."
            msg.contains("invalid-verification-code") ->
                "El código es incorrecto o expiró."
            else -> msg.ifBlank { "No se pudo vincular el método. Intentá de nuevo." }
        }
    }
}
