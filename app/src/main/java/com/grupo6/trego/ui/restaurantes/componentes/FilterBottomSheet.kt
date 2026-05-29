package com.grupo6.trego.ui.restaurantes.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.grupo6.trego.data.model.EnumCategoriaRestaurante
import com.grupo6.trego.ui.restaurantes.FilterState
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterState,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategoria: EnumCategoriaRestaurante? by remember { mutableStateOf(currentFilter.categoria) }
    var selectedCalificacion by remember { mutableStateOf(currentFilter.calificacionMinima) }
    var soloAbiertos by remember { mutableStateOf(currentFilter.soloAbiertos) }

    val categorias: List<EnumCategoriaRestaurante> = enumValues<EnumCategoriaRestaurante>().toList()
    val calificaciones = listOf(3.0, 3.5, 4.0, 4.5)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Filtros", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            // Categoría
            Text("Categoría", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            categorias.chunked(3).forEach { row ->
                Row {
                    row.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoria == cat,
                            onClick = {
                                selectedCategoria = if (selectedCategoria == cat) null else cat
                            },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Calificación mínima
            Text("Calificación mínima", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row {
                calificaciones.forEach { cal ->
                    FilterChip(
                        selected = selectedCalificacion == cal,
                        onClick = {
                            selectedCalificacion = if (selectedCalificacion == cal) null else cal
                        },
                        label = { Text("⭐ $cal", fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Solo abiertos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Solo abiertos", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = soloAbiertos,
                    onCheckedChange = { soloAbiertos = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TregoOrange)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botones
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        selectedCategoria = null
                        selectedCalificacion = null
                        soloAbiertos = false
                        onApply(FilterState())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
                Button(
                    onClick = {
                        onApply(FilterState(selectedCategoria, selectedCalificacion, soloAbiertos))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TregoOrange)
                ) {
                    Text("Aplicar", color = androidx.compose.ui.graphics.Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}