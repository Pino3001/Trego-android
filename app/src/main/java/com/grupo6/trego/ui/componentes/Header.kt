package com.grupo6.trego.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.ui.theme.TregoOrange

@Composable
fun TregoHeader(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottomPadding: Dp = 0.dp,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TregoOrange)
            .statusBarsPadding()
            .heightIn(95.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (navigationIcon != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    navigationIcon()
                }
            }

            // 2. Título (Centro absoluto)
            Text(
                text = title.uppercase(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 60.dp),
                textAlign = TextAlign.Center,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color = Color.White
            )

            // 3. Iconos Derechos (Acciones)
            if (actions != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    content = actions
                )
            }
        }

        // --- ÁREA INFERIOR: PÍLDORA O EXTRAS ---
        if (bottomContent != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = bottomContent
            )
        }
    }
}