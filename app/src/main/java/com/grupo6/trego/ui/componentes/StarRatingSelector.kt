package com.grupo6.trego.ui.componentes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.ui.theme.TregoOrange


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificacionModal(
    nombreResto: String?,
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, comentario: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating     by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    val maxChars = 200

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState     = sheetState,
        containerColor = Color.White,
        shape          = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle     = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("¿Cómo estuvo el restaurante?",
                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (!nombreResto.isNullOrBlank()) {
                    Text(nombreResto, fontSize = 13.sp, color = Color.Gray)
                }
            }

            StarRatingSelector(
                rating         = rating,
                onRatingChange = { rating = it },
                starSize       = 48.dp
            )

            OutlinedTextField(
                value         = comentario,
                onValueChange = { if (it.length <= maxChars) comentario = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = {
                    Text("Contá tu experiencia...", fontSize = 13.sp, color = Color.Gray)
                },
                minLines  = 3,
                maxLines  = 5,
                shape     = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                colors    = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TregoOrange,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor          = TregoOrange,
                ),
                supportingText = {
                    Text(
                        "${comentario.length} / $maxChars",
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize  = 11.sp,
                        color     = if (comentario.length > maxChars * 0.85f) TregoOrange else Color.Gray
                    )
                }
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(50),
                    border   = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) { Text("Cancelar", fontSize = 13.sp) }

                Button(
                    onClick  = { onConfirm(rating, comentario.trim()) },
                    enabled  = rating > 0,
                    modifier = Modifier.weight(2f),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor        = TregoOrange,
                        disabledContainerColor = TregoOrange.copy(alpha = 0.35f)
                    )
                ) { Text("Enviar opinión", fontSize = 13.sp) }
            }
        }
    }
}

@Composable
fun StarRatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 38.dp,
) {
    val labels = remember {
        listOf("Sin calificar", "Malo", "Regular", "Bueno", "Muy bueno", "Excelente")
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..maxStars).forEach { star ->
                StarItem(
                    filled = star <= rating,
                    isLast = star == rating,
                    size = starSize,
                    onClick = { onRatingChange(if (rating == star) 0 else star) }
                )
            }
        }

        AnimatedContent(
            targetState = rating,
            transitionSpec = {
                (fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 3 })
                    .togetherWith(fadeOut(tween(100)))
            },
            label = "rating_label"
        ) { r ->
            Text(
                text = labels[r],
                fontSize = 13.sp,
                fontWeight = if (r > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (r > 0) TregoOrange else Color.Gray
            )
        }
    }
}

@Composable
private fun StarItem(
    filled: Boolean,
    isLast: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isLast) 1.25f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "star_scale"
    )
    val tint by animateColorAsState(
        targetValue = if (filled) TregoOrange else Color(0xFFDDDDDD),
        animationSpec = tween(150),
        label = "star_tint"
    )

    Icon(
        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    )
}