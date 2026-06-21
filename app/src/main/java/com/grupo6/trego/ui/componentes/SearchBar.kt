package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar...",
    onSearch: (() -> Unit)? = null,
    showBorder: Boolean = true,
    backgroundColor: Color = Color.Transparent
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val borderColor = if (isFocused) Color(0xFFF9A825) else Color.Gray
    val borderWidth = if (isFocused) 2.dp else 1.dp

    // Construimos el modificador base
    var textFieldModifier = modifier
        .height(40.dp)
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
        .background(backgroundColor, RoundedCornerShape(50)) // Aplicamos el fondo

    // Aplicamos el borde SOLO si está permitido
    if (showBorder) {
        textFieldModifier =
            textFieldModifier.border(borderWidth, borderColor, RoundedCornerShape(50))
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch?.invoke()
                focusManager.clearFocus()
            }
        ),
        modifier = textFieldModifier,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = TregoOrange,
                            shape = CircleShape
                        )
                        .padding(6.dp)
                        .clickable {
                            onSearch?.invoke()
                            focusManager.clearFocus()
                        }
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