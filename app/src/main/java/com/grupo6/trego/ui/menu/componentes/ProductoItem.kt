package com.grupo6.trego.ui.menu.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.ui.carrito.CarritoViewModel
import com.grupo6.trego.ui.theme.PrecioColor
import com.grupo6.trego.ui.theme.TregoSecondary

@Composable
fun ProductoItem(
    producto: DTOProducto,
    onAgregar: () -> Unit
) {
    val carritoViewModel: CarritoViewModel = viewModel()

    // Card de los productos que no son ofertas
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp),

    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hay que implementar lo de las imagenes con cloudinary
            AsyncImage(
                model = producto.urlImagen ?: "https://via.placeholder.com/70",
                contentDescription = producto.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                producto.nombre?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
                producto.descripcion?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${producto.precio?.toInt()}$",
                    color = PrecioColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Boton para agregar item al carrito
            IconButton(
                onClick = onAgregar,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = TregoSecondary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .then(
                            Modifier
                                .padding(4.dp)
                        )
                )
            }
        }
    }
}