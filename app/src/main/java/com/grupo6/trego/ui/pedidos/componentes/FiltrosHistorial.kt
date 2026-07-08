package com.grupo6.trego.ui.pedidos.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.ui.theme.BackgroundColor
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Este componente agrupa todas las herramientas de búsqueda y filtrado para el historial. 
 * Permite buscar por texto, cambiar el orden de la lista (más nuevos o más viejos) 
 * y filtrar rápidamente usando chips para la fecha o el estado del pedido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosHistorial(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedEstado: EnumEstadoPedido?,
    onEstadoClick: (EnumEstadoPedido) -> Unit,
    selectedDate: LocalDate?,
    onDateClick: () -> Unit,
    onDateClear: () -> Unit,
    ordenMasRecientes: Boolean,
    onOrdenClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }

    Surface(color = BackgroundColor, shadowElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .height(50.dp),
                    placeholder = {
                        Text(
                            "Restaurante o Nro. pedido...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, contentDescription = null,
                            tint = if (searchQuery.isNotEmpty()) TregoOrange else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    Icons.Default.Close, contentDescription = "Limpiar",
                                    tint = Color.Gray, modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TregoOrange,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = TregoOrange,
                    )
                )

                TextButton(
                    onClick = onOrdenClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (ordenMasRecientes) "ASC" else "DESC",
                            color = TregoOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Icon(
                            imageVector = if (ordenMasRecientes) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = "Cambiar orden",
                            tint = TregoOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Chip fecha
                item {
                    TregoFilterChip(
                        selected = selectedDate != null,
                        label = selectedDate?.format(dateFormatter) ?: "Fecha",
                        leadingIcon = Icons.Default.CalendarMonth,
                        onClick = onDateClick,
                        onClear = onDateClear.takeIf { selectedDate != null }
                    )
                }
                // Chips por estado
                val estadosAMostrar = listOf(
                    EnumEstadoPedido.Entregado,
                    EnumEstadoPedido.Cancelado,
                    EnumEstadoPedido.Reembolsado
                )
                items(estadosAMostrar) { estado ->
                    TregoFilterChip(
                        selected = selectedEstado == estado,
                        label = estado.name + "s",
                        onClick = { onEstadoClick(estado) },
                        onClear = if (selectedEstado == estado) {
                            { onEstadoClick(estado) }
                        } else null
                    )
                }
            }
        }
    }
}

// Chip reutilizable con estilo Trego
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TregoFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, modifier = Modifier.size(14.dp)) }
        },
        trailingIcon = onClear?.let {
            {
                Icon(
                    Icons.Default.Close, contentDescription = "Quitar filtro",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = it)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TregoOrange.copy(alpha = 0.10f),
            selectedLabelColor = TregoOrange,
            selectedLeadingIconColor = TregoOrange,
            selectedTrailingIconColor = TregoOrange,
            containerColor = BlancoCard
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = TregoOrange,
            borderColor = Color(0xFFE0E0E0),
            selectedBorderWidth = 1.dp
        )
    )
}