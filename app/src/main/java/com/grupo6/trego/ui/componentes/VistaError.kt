package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun VistaEstado(
    mensaje: String,
    titulo: String = "¡Ups! Algo salió mal",
    icono: ImageVector? = Icons.Default.Warning,
    iconoResId: Int? = null,
    colorIcono: Color = Color(0xFFE53935),
    botonTexto: String = "Reintentar",
    onAccion: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (iconoResId != null) {
            Image(
                painter = painterResource(id = iconoResId),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        } else if (icono != null) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mensaje,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )

        if (onAccion != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAccion,
                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(min = 80.dp, max = 300.dp)
            ) {
                if (botonTexto == "Reintentar") {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(botonTexto, color = Color.White)
            }
        }
    }
}

@Composable
fun VistaError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    VistaEstado(
        mensaje = mensaje,
        onAccion = onReintentar
    )
}
