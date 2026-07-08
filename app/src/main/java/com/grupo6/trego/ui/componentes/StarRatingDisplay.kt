package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo6.trego.ui.theme.TregoOrange

/**
 * Este componente solo sirve para mostrar una calificación con estrellas de 
 * forma visual. No se puede interactuar con él, es solo para lectura en las 
 * tarjetas o cabezales.
 */
@Composable
fun StarRatingDisplay(
    rating: Int,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    color: Color = TregoOrange,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (star <= rating) color else Color(0xFFDDDDDD),
                modifier = Modifier.size(size)
            )
        }
    }
}