package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.DTOComentario
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import kotlin.math.roundToInt

/**
 * Este componente muestra el resumen de las opiniones de un restaurante. 
 * Si no hay ninguna, invita al usuario a ser el primero; y si ya existen, 
 * muestra el puntaje promedio bien grande y llamativo.
 */
@Composable
fun ResenasHeader(resenas: List<DTOComentario>, promedio: Float, onClick: () -> Unit = {}) {

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier
                .padding(top = 42.dp, start = 12.dp, end = 12.dp)
                .height(0.5.dp),
            color = TregoOrange.copy(alpha = 0.32f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Opiniones", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

            if (resenas.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = TregoOrange.copy(alpha = 0.10f),
                ) {
                    Text(
                        "${resenas.size} ${if (resenas.size == 1) "reseña" else "reseñas"}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = TregoOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (resenas.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoCard)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✍️", fontSize = 32.sp)
                    Text(
                        "Sé el primero en opinar", fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = Color.DarkGray
                    )
                    Text(
                        "Tus comentarios ayudan a otros usuarios",
                        fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoCard)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        String.format("%.1f", promedio),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TregoOrange,
                        lineHeight = 48.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StarRatingDisplay(rating = promedio.roundToInt(), size = 22.dp)
                        Text("de 5.0", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "Basado en ${resenas.size} ${if (resenas.size == 1) "opinión" else "opiniones"}",
                            fontSize = 11.sp, color = Color.Gray
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                color = Color(0xFFF0F0F0)
            )
        }
    }
}