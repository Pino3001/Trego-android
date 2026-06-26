package com.grupo6.trego.ui.menu.componentes


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTOProducto
import com.grupo6.trego.data.utilities.ensureCloudinaryTransformation
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun OfertaItem(producto: DTOProducto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.7f)
                ) {
                    if (producto.urlImagen != null) {
                        AsyncImage(
                            model = producto.oferta?.urlImagen?.ensureCloudinaryTransformation("w_150,h_150,c_fill,g_auto") ?: producto.urlImagen.ensureCloudinaryTransformation("w_150,h_150,c_fill,g_auto"),
                            contentDescription = producto.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
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
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = producto.nombre ?: "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis

                    )
                    Row(
                        modifier = Modifier.padding(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${producto.calcularPrecioConDescuento()}$",
                            fontSize = 13.sp,
                            color = TregoOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${producto.precio?.toInt()}$",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            producto.oferta?.descuento?.let {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp),
                    shape = RoundedCornerShape(50),
                    color = TregoOrange
                ) {
                    Text(
                        text = "-${it.toInt()}%",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

