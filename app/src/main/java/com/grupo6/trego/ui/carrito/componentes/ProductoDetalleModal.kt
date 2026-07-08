package com.grupo6.trego.ui.carrito.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOIngrediente
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.data.utilities.ensureCloudinaryTransformation
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.ComboGreen
import com.grupo6.trego.ui.theme.ComboGreenDark
import com.grupo6.trego.ui.theme.GreenPlaceholder
import com.grupo6.trego.ui.theme.TregoOrange

/**
 * Este modal muestra toda la información detallada de un producto cuando el usuario 
 * lo toca en el menú o en el carrito. Permite elegir la cantidad, pedir que saquen 
 * ingredientes, ver qué trae un combo y dejar una nota especial para el restaurante.
 */
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
        mutableStateOf<Set<DTOIngrediente>>(item.ingredientesAQuitar?.toSet() ?: emptySet())
    }
    val mapProductosCombo = remember(item.producto?.combo) {
        item.producto?.combo?.obtenerConteoPorNombre() ?: emptyMap()
    }

    val scrollState = rememberScrollState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (available.y < 0f) Offset(0f, available.y) else Offset.Zero
            }
        }
    }

    val configuration = LocalConfiguration.current
    val maxModalHeight = configuration.screenHeightDp.dp * 0.90f

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = BlancoCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxModalHeight)
                .nestedScroll(nestedScrollConnection)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState) // ← usar el state extraído
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 15.dp)
        ) {

            // Imagen del producto
            if (item.producto?.urlImagen != null && item.producto.urlImagen != "") {
                AsyncImage(
                    model = item.producto.urlImagen.ensureCloudinaryTransformation("w_800,h_400,c_fill,g_auto"),
                    contentDescription = item.producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
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

            /* Controles para sumar o restar unidades y ver el precio por unidad del producto. */
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
                        "${(item.producto?.calcularPrecioConDescuento())}$",
                        color = TregoOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (!item.producto?.ingredientes.isNullOrEmpty()) {
                /* Si el producto tiene ingredientes, permitimos al usuario marcar cuáles quiere quitar. */
                Text("Ingredientes a quitar:", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))

                val ingredientes = item.producto.ingredientes
                val horizontalScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ingredientes.forEach { ing ->
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
                            label = {
                                Text(
                                    text = ing.nombre,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TregoOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            if (!item.producto?.combo?.productosIncluidos.isNullOrEmpty()) {
                /* Si es un combo, listamos de forma visual todos los productos que vienen incluidos. */
                Text("Incluye en el combo", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Productos que forman parte de este combo",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    mapProductosCombo.entries.forEach { (nombre, cantidad) ->
                        Row(
                            modifier = Modifier
                                .border(1.dp, ComboGreen, RoundedCornerShape(99.dp))
                                .clip(RoundedCornerShape(99.dp))
                                .background(GreenPlaceholder)
                                .padding(
                                    start = if (cantidad > 1) 3.dp else 12.dp,
                                    end = 12.dp,
                                    top = 5.dp,
                                    bottom = 5.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (cantidad > 1) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(ComboGreen),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$cantidad",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        style = TextStyle(
                                            lineHeight = 11.sp,
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        ),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .wrapContentSize(Alignment.Center)
                                    )
                                }
                            }
                            Text(
                                text = nombre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ComboGreenDark
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            /* Campo para escribir notas adicionales, como "sin sal" o "tocar el timbre fuerte". */
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

            val precioUnitario = (item.producto?.calcularPrecioConDescuento() ?: 0)
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
                            ingredientesAQuitar = ingredientesQuitados.toList(),
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