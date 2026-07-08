package com.grupo6.trego.ui.home.platos.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOSubCategoria
import com.grupo6.trego.data.utilities.ensureCloudinaryTransformation
import com.grupo6.trego.ui.theme.BlancoCard

/**
 * Esta tarjeta representa una subcategoría (como "Hamburguesas" o "Pizzas") en 
 * la lista general. Es compacta, muestra una imagen representativa y el nombre 
 * debajo, diseñada para que el usuario pueda identificar rápido lo que busca.
 */
@Composable
fun CardSubcategoria(
    subCategoria: DTOSubCategoria,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column {
            // ── Imagen del plato ──────────────────────────────────────────
            AsyncImage(
                model = subCategoria.urlImagen.ensureCloudinaryTransformation("w_200,h_200,c_fill,g_auto"),
                contentDescription = subCategoria.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp),
                contentScale = ContentScale.FillBounds,
                placeholder = ColorPainter(placeholderColor),
                error = ColorPainter(placeholderColor),
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // ── Nombre del plato ──────────────────────────────────────────
            Text(
                text = subCategoria.nombre,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
            )
        }
    }
}