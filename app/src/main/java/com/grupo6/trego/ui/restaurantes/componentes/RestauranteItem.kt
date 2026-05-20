package com.grupo6.trego.ui.restaurantes.componentes

import com.grupo6.trego.ui.theme.TregoOrange
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.RestaurantDTO
import com.grupo6.trego.R

@Composable
fun RestaurantItem(
    restaurant: RestaurantDTO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del restaurante
            AsyncImage(
                model = restaurant.imagenUrl ?: "https://via.placeholder.com/60",
                contentDescription = restaurant.nombre,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.tregologo), // imagen local mientras carga
                error = painterResource(id = R.drawable.tregologo),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, TregoOrange, CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            // Info central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = restaurant.categoria,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = restaurant.zona,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("⭐", fontSize = 12.sp)
                    Text(
                        text = restaurant.calificacion.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Info derecha
            Column(horizontalAlignment = Alignment.End) {
                // Badge Abierto/Cerrado
                Surface(
                    modifier = Modifier.width(70.dp).height(30.dp),
                    shape = RoundedCornerShape(50),
                    color = if (restaurant.abierto) Color(0xFF4CAF50) else Color.Gray
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                            // color del punto interior blanco
                        )
                        Text(
                            text = if (restaurant.abierto) "Abierto" else "Cerrado",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Badge Ofertas
                if (restaurant.tieneOfertas) {
                    Surface(
                        modifier = Modifier.width(70.dp).height(30.dp),
                        shape = RoundedCornerShape(50),
                        color = TregoOrange
                    ) {
                        Text(
                            text = "🏷 Ofertas",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Horario
                Text(
                    text = "· ${restaurant.horarioApertura} - ${restaurant.horarioCierre}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}