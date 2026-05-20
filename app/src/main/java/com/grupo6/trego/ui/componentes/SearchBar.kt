package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    query: String, // El texto actual
    onQueryChange: (String) -> Unit, // Lo que pasa cuando el usuario escribe
    onClear: () -> Unit, // Lo que pasa al tocar la "X"
    modifier: Modifier = Modifier, // Para darle peso o márgenes por fuera
    placeholderText: String = "Buscar..." // Texto por defecto personalizable
) {
    // El estado del foco vive dentro del componente (cada barra maneja el suyo)
    var isFocused by remember { mutableStateOf(false) }

    // Colores dinámicos
    val borderColor = if (isFocused) Color(0xFFF9A825) else Color.Gray
    val borderWidth = if (isFocused) 2.dp else 1.dp
    val iconColor = if (isFocused) Color(0xFFF9A825) else Color.Gray

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
        modifier = modifier
            .height(40.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .border(borderWidth, borderColor, RoundedCornerShape(50)),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(placeholderText, color = Color.Gray, fontSize = 14.sp)
                    }
                    innerTextField()
                }

                if (query.isNotBlank()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    )
}