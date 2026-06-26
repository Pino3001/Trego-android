package com.grupo6.trego.ui.home.ofertas.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOProductoZona
import com.grupo6.trego.data.utilities.ensureCloudinaryTransformation
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.ComboGreenDark
import com.grupo6.trego.ui.theme.PurpleGrey40
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun OfertaListItem(
    productoZona: DTOProductoZona,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val producto = productoZona.producto
    val nombre = producto.nombre.orEmpty()
    val descripcion = producto.descripcion?.takeIf { it.isNotBlank() }
    val precio = producto.calcularPrecioConDescuento() ?: 0f
    val imagenUrl = producto.oferta?.urlImagen
    val restaurante = productoZona.nombreRestaurante.orEmpty()
    val calificacion = productoZona.calificacionProm
    val calle = productoZona.direccion.calle.orEmpty()
    val numero = productoZona.direccion.numero?.takeIf { it.isNotBlank() }
    val direccion = if (numero != null) "$calle $numero" else calle
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            // ── 1. IMAGEN CON PRECIO FLOTANTE (0 Height extra) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                // Imagen
                AsyncImage(
                    model = imagenUrl?.ensureCloudinaryTransformation("w_150,h_150,c_fill,g_auto"),
                    contentDescription = nombre,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    placeholder = placeholder,
                    error = placeholder,
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = TregoOrange.copy(alpha = 0.9f), // Un poco de transparencia queda bien
                ) {
                    Text(
                        text = "${producto.oferta?.descuento?.toInt()}% off",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlancoCard,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ComboGreenDark.copy(alpha = 0.9f), // Un poco de transparencia queda bien
                ) {
                    Text(
                        text = "\$${precio.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BlancoCard,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // ── 2. INFO DEL PLATO (Súper compacta) ──
            Column(
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ), // Padding vertical reducido
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (descripcion != null) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        lineHeight = 12.sp,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp, modifier = Modifier.padding(
                    horizontal =
                        8.dp
                ), color = MaterialTheme.colorScheme.outlineVariant
            )

            // ── 3. INFO DEL RESTAURANTE ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlancoCard)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Renglón 1: Nombre y Calificación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Store,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = restaurante,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFFC107)
                        )
                        Text(
                            text = String.format("%.1f", calificacion),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Renglón 2: Dirección
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = PurpleGrey40
                    )
                    Text(
                        text = direccion,
                        style = MaterialTheme.typography.labelSmall,
                        color = PurpleGrey40,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}