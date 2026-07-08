package com.grupo6.trego.ui.usuario.componentes

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.DTOUsuario
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.usuario.MetodosAccesoViewModel
import com.grupo6.trego.ui.usuario.SmsLinkStep
import org.koin.androidx.compose.koinViewModel

/**
 * Este componente agrupa las opciones para que el usuario vincule su cuenta. 
 * Muestra si ya tiene Google o su teléfono asociados y, si no, despliega 
 * los formularios necesarios para realizar la vinculación por SMS o por 
 * la cuenta de Google directamente.
 */
@Composable
fun MetodosAcceso(
    usuario: DTOUsuario,
    onVinculado: () -> Unit
) {
    val vm: MetodosAccesoViewModel = koinViewModel()
    val context = LocalContext.current
    val activity = context as Activity

    val googleVinculado = !usuario.email.isNullOrBlank()
    val smsVinculado = !usuario.telefono.isNullOrBlank()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Métodos de acceso", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Vinculá ambos métodos para ingresar por Google y por SMS.",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))

        vm.error?.let {
            Text(
                it,
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        vm.mensajeExito?.let {
            Text(
                it,
                color = Color(0xFF2E7D32),
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        /* Fila dedicada a la vinculación con Google. Si ya está listo, muestra el email asociado. */
        FilaMetodo(
            titulo = "Google",
            detalle = if (googleVinculado) usuario.email!! else "Sin vincular",
            vinculado = googleVinculado,
            mostrarBoton = !googleVinculado,
            cargando = vm.isLoading,
            onVincular = { vm.vincularGoogle(context) { onVinculado() } }
        )

        Spacer(Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF9F9F9),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                /* Sección para el teléfono. Si no está vinculado, muestra el flujo de ingreso de número y después el de código SMS. */
                FilaMetodo(
                    titulo = "Teléfono",
                    detalle = if (smsVinculado) usuario.telefono!! else "Sin vincular",
                    vinculado = smsVinculado,
                    mostrarBoton = !smsVinculado && vm.smsStep == SmsLinkStep.IDLE,
                    cargando = vm.isLoading,
                    onVincular = { vm.iniciarSms() }
                )

                if (!smsVinculado && vm.smsStep == SmsLinkStep.PHONE) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = vm.phoneNumber,
                            onValueChange = vm::onPhoneChange,
                            label = { Text("Número de teléfono") },
                            placeholder = { Text("+598 99 000 000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { vm.enviarCodigoSms(activity) { onVinculado() } },
                                enabled = vm.phoneNumber.isNotBlank() && !vm.isLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (vm.isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.width(20.dp)
                                    )
                                } else {
                                    Text("Enviar código", color = Color.White)
                                }
                            }
                            OutlinedButton(
                                onClick = { vm.cancelarSms() },
                                modifier = Modifier.height(46.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", color = Color.Gray)
                            }
                        }
                    }
                }

                if (!smsVinculado && vm.smsStep == SmsLinkStep.CODE) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = vm.otpCode,
                            onValueChange = vm::onOtpChange,
                            label = { Text("Código de verificación") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { vm.confirmarCodigoSms { onVinculado() } },
                                enabled = vm.otpCode.length == 6 && !vm.isLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (vm.isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.width(20.dp)
                                    )
                                } else {
                                    Text("Confirmar", color = Color.White)
                                }
                            }
                            OutlinedButton(
                                onClick = { vm.cancelarSms() },
                                modifier = Modifier.height(46.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaMetodo(
    titulo: String,
    detalle: String,
    vinculado: Boolean,
    mostrarBoton: Boolean,
    cargando: Boolean,
    onVincular: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(detalle, color = Color.Gray, fontSize = 12.sp)
        }
        when {
            vinculado -> {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Vinculado",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.width(22.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Vinculado", color = Color(0xFF2E7D32), fontSize = 13.sp)
            }

            mostrarBoton -> {
                TextButton(onClick = onVincular, enabled = !cargando) {
                    Text("Vincular", color = TregoOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
