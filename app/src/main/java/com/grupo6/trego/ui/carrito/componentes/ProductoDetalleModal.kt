package com.grupo6.trego.ui.carrito.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
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
    esEdicion: Boolean,
) {
    var cantidad by remember { mutableStateOf(item.cantidad ?: 1) }
    var comentario by remember { mutableStateOf(item.observaciones ?: "") }
    var ingredientesQuitados by remember {
        mutableStateOf<Set<DTOIngrediente>>(item.ingredientes?.toSet() ?: emptySet())
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
            if (item.producto?.urlImagen != null && item.producto.urlImagen != "") {
                AsyncImage(
                    model = item.producto.urlImagen,
                    contentDescription = item.producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                // Placeholder cuando no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(62.dp)
                        )
                        Text(
                            "Sin imagen disponible",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            item.producto?.nombre?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item.producto?.descripcion?.let {
                Text(
                    text = it,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                            Text(
                                "<",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TregoOrange
                            )
                        }
                        Text("$cantidad", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { cantidad++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(
                                ">",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TregoOrange
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Precio unidad", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${(item.producto?.precio)?.toInt()}$",
                        color = TregoOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (item.producto?.ingredientes?.isEmpty() == true) {
                Text("Ingredientes opcionales", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))

                val ingredientes = item.producto.ingredientes

                ingredientes.chunked(4).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { ing ->
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
                                label = { Text(ing.nombre, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TregoOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }


            Spacer(Modifier.height(8.dp))

            // Comentario — siempre visible, FIX del bug del ?.let
            Text("Comentarios", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                placeholder = { Text("Extra sobre el pedido.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
            )

            Spacer(Modifier.height(12.dp))

            // Subtotal
            val precioUnitario =
                (item.producto?.precio?.toInt() ?: 0)
            val subtotal = precioUnitario * cantidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    "$subtotal$",
                    color = TregoOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    onConfirm(
                        item.copy(
                            cantidad = cantidad,
                            observaciones = comentario.ifBlank { null },
                            ingredientes = ingredientesQuitados.toList(),
                            subtotal = subtotal.toFloat()
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
                    if (esEdicion) "Confirmar cambios" else "Agregar al carrito",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}