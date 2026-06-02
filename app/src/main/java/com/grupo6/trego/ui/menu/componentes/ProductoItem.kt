package com.grupo6.trego.ui.menu.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.grupo6.trego.ui.theme.PrecioColor
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun ProductoItem(
    producto: DTOProducto,
    onAgregar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Imagen más pequeña
            if (producto.urlImagen != null) {
                AsyncImage(
                    model = producto.urlImagen,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (producto.descripcion != null) {
                    Text(
                        text = producto.descripcion,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Precio con label
                if (producto.oferta != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Precio:",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${producto.calcularPrecioConDescuento().toInt()}$",
                            color = TregoOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${producto.precio?.toInt()}$",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Precio:",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${producto.precio?.toInt()}$",
                            color = PrecioColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.width(6.dp))

            // Botón agregar más pequeño
            FilledIconButton(
                onClick = onAgregar,
                modifier = Modifier.size(30.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = TregoOrange
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar al carrito",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}