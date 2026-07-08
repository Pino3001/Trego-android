package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.DTOComentario
import com.grupo6.trego.ui.theme.TregoOrange
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Esta tarjeta muestra una reseña individual dejada por un usuario. Incluye sus 
 * iniciales en un círculo, la fecha, la calificación con estrellas y el comentario 
 * que escribió sobre su experiencia.
 */
@Composable
fun ResenaCard(resena: DTOComentario) {
    val iniciales = resena.nombreCliente
        ?.split(" ")
        ?.take(2)
        ?.joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }

    fun String?.formatearFecha(): String {
        if (this == null) return ""
        return try {
            val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val salida = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            salida.format(entrada.parse(this)!!)
        } catch (e: Exception) {
            this
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(TregoOrange.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,

            ) {
            Text(
                iniciales ?: "",
                modifier = Modifier.padding(4.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TregoOrange
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(resena.nombreCliente ?: "", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(resena.fechaCreacion.formatearFecha(), fontSize = 12.sp)
            }

            StarRatingDisplay(rating = resena.calificacion ?: 0, size = 13.dp)

            if (resena.texto?.isNotBlank() == true) {
                Text(
                    resena.texto ?: "",
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    lineHeight = 19.sp
                )
            }
        }
    }
}