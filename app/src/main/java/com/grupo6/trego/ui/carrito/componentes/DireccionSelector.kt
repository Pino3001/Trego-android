package com.grupo6.trego.ui.carrito.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.DireccionDTO
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun DireccionSelector(
    direcciones: List<DireccionDTO>,
    seleccionada: DireccionDTO?,
    onSeleccionar: (DireccionDTO) -> Unit,
    onUbicacionActual: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Direcciones guardadas
        Column(modifier = Modifier.weight(1f)) {
            Text("Tus Direcciones", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                direcciones.forEach { dir ->
                    val esSeleccionada = seleccionada?.id == dir.id
                    FilterChip(
                        selected = esSeleccionada,
                        onClick = { onSeleccionar(dir) },
                        label = { Text("dir.etiqueta", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.DarkGray,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Ubicación actual
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text("Ubicacion Actual", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            IconButton(onClick = onUbicacionActual) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Ubicación actual",
                    tint = Color(0xFF8B0000),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}