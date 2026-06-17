package com.grupo6.trego.ui.menu.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.model.EnumTipoProducto
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.ComboGreen
import com.grupo6.trego.ui.theme.ComboGreenDark
import com.grupo6.trego.ui.theme.ComboGreenLight
import com.grupo6.trego.ui.theme.GreenPlaceholder
import com.grupo6.trego.ui.theme.OrangePillBg
import com.grupo6.trego.ui.theme.OrangePillText
import com.grupo6.trego.ui.theme.OrangePlaceholder
import com.grupo6.trego.ui.theme.TregoOrange



@Composable
fun ProductoItem(
    producto: DTOProducto,
    onAgregar: () -> Unit
) {
    val esCombo = producto.tipo == EnumTipoProducto.Combo

    // Acento de color: naranja para productos, verde para combos
    val accentColor         = if (esCombo) ComboGreen   else TregoOrange
    val pricePillBg         = if (esCombo) ComboGreenLight  else OrangePillBg
    val pricePillTextColor  = if (esCombo) ComboGreenDark   else OrangePillText
    val placeholderBg       = if (esCombo) GreenPlaceholder else OrangePlaceholder

    val textoCombo: String? = remember(producto.combo, esCombo) {
        if (!esCombo) return@remember null
        val combo = producto.combo
        when {
            combo == null -> "Datos del combo no recibidos"
            combo.productosIncluidos.isEmpty() -> "El combo no tiene productos asignados"
            else -> {
                val conteo = combo.obtenerConteoPorNombre() // Map<String, Int>
                conteo.entries.joinToString(", ") { (nombre, count) ->
                    if (count > 1) "${count}x $nombre" else nombre
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // Row exterior con IntrinsicSize.Min para que la barra de acento
        // tome exactamente la altura del contenido
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // ── Contenido principal ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Imagen del producto
                if (producto.urlImagen != null) {
                    AsyncImage(
                        model = producto.urlImagen,
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(placeholderBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = accentColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Nombre, subtítulo y precio
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = producto.nombre ?: "",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF1A1A1A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    when {
                        // Subtítulo de combo en verde
                        textoCombo != null -> Text(
                            text = "Incluye: $textoCombo",
                            fontSize = 11.sp,
                            color = ComboGreen,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                        // Descripción de producto normal
                        producto.descripcion != null -> Text(
                            text = producto.descripcion,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    // Fila de precio
                    val isOferta = producto.ofertaActiva == true
                    val precioFinal = if (isOferta) producto.calcularPrecioConDescuento() else producto.precio?.toInt()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pill con el precio final y su DESCRIPCIÓN explícita
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp)) // Mantenemos tu diseño redondeado original
                                .background(pricePillBg)
                                .padding(horizontal = 10.dp, vertical = 2.dp) // Un poquito más de padding horizontal para el texto extra
                        ) {
                            // Aquí definimos la descripción dependiendo de si hay oferta o no
                            val prefijoTexto = if (isOferta) "Oferta: " else "Precio: "

                            Text(
                                text = "$prefijoTexto$$precioFinal", // Se mostrará ej: "Oferta: $150" o "Precio: $200"
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = pricePillTextColor
                            )
                        }

                        // Precio original tachado (solo si hay oferta)
                        if (isOferta && producto.precio != null) {
                            Text(
                                text = "$${producto.precio.toInt()}", // Moví el símbolo de $ adelante, se ve más profesional
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Botón agregar — hereda el color de acento del tipo de producto
                FilledIconButton(
                    onClick = onAgregar,
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar al carrito",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}