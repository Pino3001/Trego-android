package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.ui.theme.TregoOrange

/**
 * Un selector de estrellas muy simple que permite al usuario filtrar o calificar.
 * Si toca una estrella, se marcan todas las anteriores, y si toca la que ya 
 * estaba seleccionada, se limpia la elección.
 */
@Composable
fun StarRatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            (1..5).forEach { star ->
                IconButton(
                    onClick = {
                        // toca la misma estrella → resetea a 0
                        onRatingChange(if (rating == star) 0 else star)
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "$star estrella${if (star != 1) "s" else ""}",
                        tint = if (star <= rating) TregoOrange
                        else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (rating == 0) "Sin calificación mínima"
            else "$rating.0 ★ o más",
            fontSize = 12.sp,
            color = if (rating > 0) TregoOrange
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}