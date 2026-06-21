package com.grupo6.trego.ui.home.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.EnumCategoriaRestaurante
import com.grupo6.trego.ui.componentes.StarRatingSelector
import com.grupo6.trego.ui.home.restaurantes.FilterState
import com.grupo6.trego.ui.theme.TregoOrange
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterState,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategoria: EnumCategoriaRestaurante? by remember { mutableStateOf(currentFilter.categoria) }
    var selectedCalificacion by remember { mutableStateOf(currentFilter.calificacionMinima ?: 0.0) }
    var selectedHoraDesde: LocalTime? by remember { mutableStateOf(currentFilter.horaDesde) }
    var selectedHoraHasta: LocalTime? by remember { mutableStateOf(currentFilter.horaHasta) }

    var showPickerDesde by remember { mutableStateOf(false) }
    var showPickerHasta by remember { mutableStateOf(false) }

    val categorias =
        EnumCategoriaRestaurante.entries.filter { it != EnumCategoriaRestaurante.SinCategoria }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Dialog: hora desde
    if (showPickerDesde) {
        val state = rememberTimePickerState(
            initialHour = selectedHoraDesde?.hour ?: 8,
            initialMinute = selectedHoraDesde?.minute ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPickerDesde = false },
            title = { Text("Abierto desde") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = state)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickerDesde = false }) { Text("Cancelar") }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHoraDesde = LocalTime.of(state.hour, state.minute)
                    showPickerDesde = false
                }) { Text("OK", color = TregoOrange) }
            }
        )
    }

    // Dialog: hora hasta
    if (showPickerHasta) {
        val state = rememberTimePickerState(
            initialHour = selectedHoraHasta?.hour ?: 22,
            initialMinute = selectedHoraHasta?.minute ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPickerHasta = false },
            title = { Text("Cerrado hasta") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = state)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickerHasta = false }) { Text("Cancelar") }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHoraHasta = LocalTime.of(state.hour, state.minute)
                    showPickerHasta = false
                }) { Text("OK", color = TregoOrange) }
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Filtros",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Categoría
            Text("Categoría", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategoria?.name ?: "Todas",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    // Opción "Todas" para limpiar el filtro de categoría
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            selectedCategoria = null
                            expandedDropdown = false
                        }
                    )

                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoria = cat
                                expandedDropdown = false
                            },
                            trailingIcon = {
                                if (selectedCategoria == cat) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TregoOrange
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Calificación mínima
            Text("Calificación mínima", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StarRatingSelector(
                    rating = selectedCalificacion.toInt(),
                    onRatingChange = { selectedCalificacion = it.toDouble() }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Horario de atención
            Text("Horario de atención", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showPickerDesde = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedHoraDesde?.format(timeFormatter) ?: "Desde",
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                Text("—", color = Color.Gray)
                OutlinedButton(
                    onClick = { showPickerHasta = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedHoraHasta?.format(timeFormatter) ?: "Hasta",
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }

            // Botón limpiar horario (solo si hay algo seleccionado)
            if (selectedHoraDesde != null || selectedHoraHasta != null) {
                TextButton(
                    onClick = { selectedHoraDesde = null; selectedHoraHasta = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Limpiar horario", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Botones principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onApply(FilterState()); onDismiss() },
                    modifier = Modifier.weight(1f)
                ) { Text("Limpiar") }

                Button(
                    onClick = {
                        onApply(
                            FilterState(
                                categoria = selectedCategoria,
                                calificacionMinima = if (selectedCalificacion > 0.0) selectedCalificacion else null,
                                horaDesde = selectedHoraDesde,
                                horaHasta = selectedHoraHasta
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TregoOrange)
                ) { Text("Aplicar", color = Color.White) }
            }
        }
    }
}