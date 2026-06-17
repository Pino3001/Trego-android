package com.grupo6.trego.ui.home.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.EnumCategoriaProducto
import com.grupo6.trego.ui.theme.TregoOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterBottomSheet(
    selectedCategory: EnumCategoriaProducto?,
    onApply: (EnumCategoriaProducto?) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedCategory by remember { mutableStateOf(selectedCategory) }
    val categorias = EnumCategoriaProducto.entries

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filtrar por Categoría", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            categorias.chunked(3).forEach { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chunk.forEach { cat ->
                        FilterChip(
                            selected = tempSelectedCategory == cat,
                            onClick = {
                                tempSelectedCategory = if (tempSelectedCategory == cat) null else cat
                            },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        tempSelectedCategory = null
                        onApply(null)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
                Button(
                    onClick = {
                        onApply(tempSelectedCategory)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TregoOrange)
                ) {
                    Text("Aplicar", color = Color.White)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
