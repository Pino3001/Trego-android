package com.grupo6.trego.ui.carrito.componentes

import android.R.attr.maxLines
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOIngrediente
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleModal(
    item: DTOProductoPedido,
    onConfirm: (DTOProductoPedido) -> Unit,
    onDismiss: () -> Unit,
) {
    var cantidad by remember { mutableStateOf(item.cantidad ?: 1) }
    var comentario by remember { mutableStateOf(item.observaciones) }
    var ingredientesQuitados by remember {
        mutableStateOf<Set<DTOIngrediente>>(item.ingredientes?.toSet() ?: emptySet<DTOIngrediente>())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            // Imagen del producto
            AsyncImage(
                model = item.producto?.urlImagen
                    ?: "https://picsum.photos/seed/${item.producto?.idProducto}/400/200",
                contentDescription = item.producto?.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.height(8.dp))

            // Nombre y precio
            item.producto?.nombre?.let { Text(it, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
            Spacer(Modifier.height(4.dp))


            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cantidad
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Cantidad", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { if (cantidad > 1) cantidad-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TregoOrange)
                        }

                        Text(
                            text = "$cantidad",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { cantidad++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TregoOrange)
                        }
                    }
                }
                // Precio Unidad
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Precio Unidad", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${(item.producto?.precioOferta ?: item.producto?.precio)?.toInt()}$",
                        color = TregoOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Quitar ingredientes
            Text("Quitar ingredientes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            item.producto?.ingredientes?.chunked(4)?.forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fila.forEach { ing -> // 'ing' aquí ahora es un objeto DTOIngrediente
                        val quitado = ingredientesQuitados.contains(ing)
                        FilterChip(
                            selected = quitado,
                            onClick = {
                                ingredientesQuitados = if (quitado) {
                                    ingredientesQuitados - ing
                                } else {
                                    ingredientesQuitados + ing
                                }
                            },
                            // IMPORTANTE: Modifica 'ing.nombre' por la propiedad real que tenga tu DTO
                            // para mostrar el texto (puede ser ing.nombre, ing.descripcion, etc.)
                            label = { Text(text = ing.nombre, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TregoOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Comentario
            Text("Comentarios", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            comentario?.let { it1 ->
                OutlinedTextField(
                    value = it1,
                    onValueChange = { comentario = it },
                    placeholder = { Text("Comentario...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Subtotal
            val subtotal = (item.producto?.precioOferta ?: item.producto?.precio)?.toInt()
                ?.times(cantidad)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    "${subtotal?.toString()}$",
                    color = TregoOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Botón confirmar
            Button(
                onClick = {
                    onConfirm(
                        item.copy(
                            cantidad = cantidad,
                            observaciones = comentario,
                            producto = item.producto,
                            ingredientes = ingredientesQuitados as List<DTOIngrediente>?,
                            subtotal = item.subtotal,
                            cantidadDisponible = item.subtotal as Int?,
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TregoOrange),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    if (item.cantidad == 0) "Agregar al carrito" else "Confirmar cambios",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}