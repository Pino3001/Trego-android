package com.grupo6.trego.ui.restaurantes.componentes

import androidx.compose.foundation.background
import com.grupo6.trego.ui.theme.TregoOrange
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.grupo6.trego.data.model.DTORestaurante
import com.grupo6.trego.R

@Composable
fun RestaurantItem(
    restaurant: DTORestaurante,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },          // sin height fijo
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = restaurant.fotoPerfil,   // sin placeholder externo
                contentDescription = restaurant.nombre,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.tregologo),
                error = painterResource(id = R.drawable.tregologo),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, TregoOrange, CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.nombre ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis   // evita overflow en nombres largos
                )
                Text(
                    text = restaurant.categoria?.name ?: "",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                // Dirección separada del rating
                restaurant.direccion?.calle?.let {
                    Text(
                        text = it,
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TregoOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = restaurant.calificacionProm?.toString() ?: "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val esAbierto = restaurant.abierto == true
                // Badge Abierto/Cerrado
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (esAbierto) Color(0xFF4CAF50) else Color.Gray
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)   // FIX: color faltante
                        )
                        Text(
                            text = if (esAbierto) "Abierto" else "Cerrado",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (restaurant.ofertas) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = TregoOrange
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ofertas",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Horario con ícono
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${restaurant.horaApertura ?: "N/H"} - ${restaurant.horaCierre ?: "N/H"}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}