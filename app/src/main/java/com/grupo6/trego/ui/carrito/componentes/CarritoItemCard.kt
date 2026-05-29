package com.grupo6.trego.ui.carrito.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOProductoPedido
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            AsyncImage(
                model = item.producto?.urlImagen ?: "https://picsum.photos/seed/${item.producto?.idProducto}/200",
                contentDescription = item.producto?.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Nombre + editar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.producto?.nombre?.let {
                        Text(
                            it,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                    }
                }

                // Ingredientes quitados
                if (item.ingredientes?.isNotEmpty() == true) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Quitar:", fontSize = 11.sp, color = Color.Gray)
                        item.ingredientes.forEach { ing ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = TregoOrange
                            ) {
                                Text(
                                    text = ing.nombre,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Comentario
                if (item.observaciones?.isNotBlank() == true) {
                    Text(
                        "\"${item.observaciones}\"",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Cantidad + eliminar + total
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cantidad
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onCambiarCantidad(-1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("<", fontWeight = FontWeight.Bold, color = TregoOrange)
                        }
                        Text(
                            item.cantidad.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onCambiarCantidad(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text(">", fontWeight = FontWeight.Bold, color = TregoOrange)
                        }
                    }

                    // Eliminar + total
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Total: ${item.subtotal?.toInt()}$",
                            fontWeight = FontWeight.SemiBold,
                            color = TregoOrange,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFF8B0000))
                        }
                    }
                }
            }
        }
    }
}