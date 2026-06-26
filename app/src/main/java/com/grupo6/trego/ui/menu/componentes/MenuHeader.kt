package com.grupo6.trego.ui.menu.componentes

import android.R.attr.rating
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.data.utilities.ensureCloudinaryTransformation
import com.grupo6.trego.ui.componentes.StarRatingSelector
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun MenuHeader(
    restaurante: DTORestaurante,
    onBack: () -> Unit,
    onStarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Altura fija para dar presencia a la imagen
            .background(TregoOrange)
    ) {
        // 1. Imagen de fondo (Ocupará toda la caja, incluso detrás de la barra de estado)
        AsyncImage(
            model = restaurante.fotoPortada?.ensureCloudinaryTransformation("w_800,h_400,c_fill,g_auto"),
            contentDescription = "Portada del restaurante",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // 2. Gradiente superpuesto (Garantiza que el texto blanco siempre se lea bien)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            // 0.0f es el tope superior. Nace 100% naranja para fundirse con la barra
                            0.0f to TregoOrange,

                            // En el 15% de la imagen, el naranja se vuelve semi-transparente
                            0.15f to TregoOrange.copy(alpha = 0.5f),

                            // Del 30% al 60% la imagen es totalmente transparente (se ve la foto limpia)
                            0.30f to Color.Transparent,
                            0.60f to Color.Transparent,

                            // Del 60% hacia abajo, empieza a oscurecerse para proteger los textos
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack, modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    tint = Color.White,
                    contentDescription = "Volver"
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                restaurante.nombre?.let {
                    Text(
                        text = it,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeSuperficie(icono = "⭐", texto = restaurante.calificacionProm.toString(), onClicStar = onStarClick)
                    Spacer(modifier = Modifier.width(8.dp))

                    BadgeSuperficie(
                        icono = "",
                        texto = if (restaurante.abierto == true) "Abierto" else "Cerrado",
                        dotColor = if (restaurante.abierto == true) Color(0xFF4CAF50) else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    BadgeSuperficie(icono = "🍽️", texto = restaurante.categoria.toString() ?: "Sin categoría")
                }
            }
        }
    }
}

// Componente auxiliar para que los Badges queden limpios en código
@Composable
private fun BadgeSuperficie(icono: String, texto: String, dotColor: Color? = null, onClicStar: () -> Unit = {}) {

    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.4f), // Fondo traslúcido elegante
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
        onClick = onClicStar
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )
            } else if (icono.isNotEmpty()) {
                Text(icono, fontSize = 12.sp)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = texto,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}