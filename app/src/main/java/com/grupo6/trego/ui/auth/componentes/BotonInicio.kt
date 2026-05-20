package com.grupo6.trego.ui.auth.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo6.trego.R

@Composable
fun BotonInicio(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    // Usamos Button en lugar de OutlinedButton para manejar mejor la elevación
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp), // Un poquito más alto para que luzca mejor el volumen
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD0C9C6),    // Fondo blanco
            contentColor = Color.Black       // Texto/Icono negro
        ),
        // ESTO DA EL VOLUMEN (Sombra)
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,    // Elevación normal
            pressedElevation = 8.dp,    // Se hunde un poco más al presionar
            hoveredElevation = 6.dp
        ),
        // Añadimos el borde manualmente ya que no es un OutlinedButton
        border = BorderStroke(1.5.dp, Color(0xFF737070))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho interno
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // Todo al centro
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp) // Icono un pelín más grande
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold, // Negrita para más presencia
                // Evitamos que el texto se rompa en dos líneas si es muy largo
                maxLines = 1
            )

        }
    }
}

@Preview(showBackground = true, name = "Vista Previa Boton")
@Composable
fun BotonInicioPreview() {
    // Aquí llamamos a tu componente real pero le pasamos datos fijos "de mentira"
    BotonInicio(
        text = "Continuar con Google",
        iconRes = R.drawable.ic_launcher_foreground, // Asegúrate de que este archivo exista en drawable
        onClick = { /* No hace nada en el preview */ }
    )
}