package com.grupo6.trego.ui.carrito.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun CarritoItemCard(
    item: DTOProductoPedido,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarCantidad: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {

            // Imagen
            if (item.producto?.urlImagen != null) {
                AsyncImage(
                    model = item.producto.urlImagen,
                    contentDescription = item.producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {

                // Nombre + botón editar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.producto?.nombre ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Ingredientes quitados
                if (item.ingredientesAQuitar?.isNotEmpty() == true) {
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sin:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        item.ingredientesAQuitar.forEach { ing ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = TregoOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = ing.nombre,
                                    fontSize = 10.sp,
                                    color = TregoOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Comentario
                if (item.observaciones?.isNotBlank() == true) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "\"${item.observaciones}\"",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Cantidad + subtotal + eliminar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Control cantidad
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE8E8E8)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            IconButton(
                                onClick = { onCambiarCantidad(-1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text(
                                    text = "−",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TregoOrange
                                )
                            }
                            Text(
                                text = "${item.cantidad}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { onCambiarCantidad(1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text(
                                    text = "+",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TregoOrange
                                )
                            }
                        }
                    }

                    // Subtotal + eliminar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.subtotal?.toInt()}$",
                            fontWeight = FontWeight.Bold,
                            color = TregoOrange,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}