package com.grupo6.trego.ui.home.platos.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.SortOrder
import com.grupo6.trego.ui.componentes.StarRatingSelector
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatoFilterBottomSheet(
    currentRating: Float,
    currentSortOrder: SortOrder,
    onApply: (Float, SortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    var tempRating by remember { mutableStateOf(currentRating) }
    var tempSortOrder by remember { mutableStateOf(currentSortOrder) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        FilterSheetContent(
            tempRating = tempRating,
            tempSortOrder = tempSortOrder,
            onRatingChange = { tempRating = it },
            onSortChange = { tempSortOrder = it },
            onLimpiar = {
                tempRating = 0f
                tempSortOrder = SortOrder.DEFAULT
                onApply(0f, SortOrder.DEFAULT)
                onDismiss()
            },
            onAplicar = {
                onApply(tempRating, tempSortOrder)
                onDismiss()
            },
        )
    }
}

// ─── Contenido del sheet (separado para facilitar el Preview) ─────────────────

@Composable
private fun FilterSheetContent(
    tempRating: Float,
    tempSortOrder: SortOrder,
    onRatingChange: (Float) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onLimpiar: () -> Unit,
    onAplicar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {

        // ── Título ────────────────────────────────────────────────────────
        Text(
            text = "Filtros de platos",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )

        Spacer(Modifier.height(22.dp))

        // ── Ordenar por ───────────────────────────────────────────────────
        Text(
            text = "Ordenar por",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            SortOrder.entries.forEach { order ->
                SortOrderItem(
                    label = when (order) {
                        SortOrder.DEFAULT    -> "Sin Orden"
                        SortOrder.PRICE_ASC  -> "Precio: Bajo a Alto"
                        SortOrder.PRICE_DESC -> "Precio: Alto a Bajo"
                    },
                    selected = tempSortOrder == order,
                    onClick = { onSortChange(order) },
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        // ── Calificación mínima ───────────────────────────────────────────
        Text(
            text = "Calificación mínima",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))

        StarRatingSelector(
            rating = tempRating.toInt(),
            onRatingChange = { onRatingChange(it.toFloat()) },
        )

        Spacer(Modifier.height(28.dp))

        // ── Botones de acción ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onLimpiar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
            ) {
                Text("Limpiar")
            }
            Button(
                onClick = onAplicar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
            ) {
                Text("Aplicar", color = Color.White)
            }
        }
    }
}

// ─── Sub-composables privados ─────────────────────────────────────────────────

/**
 * Fila seleccionable para una opción de orden.
 * Cuando está seleccionada muestra borde y fondo naranja tenue + ícono de check.
 */
@Composable
private fun SortOrderItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val borderColor = if (selected) TregoOrange else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 1.5.dp else 0.5.dp
    val bgColor     = if (selected) TregoOrange.copy(alpha = 0.08f) else Color.Transparent
    val textColor   = if (selected) TregoOrange else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = textColor,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = TregoOrange,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}



