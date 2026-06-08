package com.grupo6.trego.ui.pedidos.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.grupo6.trego.ui.componentes.ConfirmDialogComponent
import com.grupo6.trego.data.model.DTOCrearReclamoRequest
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.theme.TregoSecondary
import java.time.format.DateTimeFormatter

@Composable
fun HistorialCard(
    item: PedidoUiModel,
    onClick: (request: DTOCrearReclamoRequest, onSuccess: () -> Unit) -> Unit
) {
    var reclamar      by remember { mutableStateOf(false) }
    var reclamoTexto  by remember { mutableStateOf("") }
    var mostrarConfirmarCancelReclamo by remember { mutableStateOf(false) }
    val maxChars = 200
    val puedeReclamar = item.pedido.estado == EnumEstadoPedido.Entregado
    val estadoColors  = getEstadoPedidoColors(item.pedido.estado ?: EnumEstadoPedido.Entregado)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // --- Cuerpo principal ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        item.nombreRestaurante ?: "Sin nombre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Pedido #${item.pedido.idPedido}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        item.pedido.fechaCreacion?.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        ) ?: "---",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "$${item.pedido.total}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TregoOrange
                    )
                    // Badge de estado (reutiliza el mismo sistema que ActivePedidoCard)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = estadoColors.backgroundColor
                    ) {
                        Text(
                            text = item.pedido.estado?.name ?: "",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = estadoColors.textColor
                        )
                    }
                }
            }

            // --- Footer "Enviar reclamo" — solo si el pedido fue entregado ---
            if (puedeReclamar) {
                HorizontalDivider(color = Color(0xFFF0F0F0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reclamar = !reclamar }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = TregoSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Enviar reclamo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TregoSecondary
                        )
                    }
                    Icon(
                        if (reclamar) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (reclamar) "Cerrar" else "Abrir reclamo",
                        tint = TregoSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // --- Formulario expandible ---
                AnimatedVisibility(
                    visible = reclamar,
                    enter = expandVertically() + fadeIn(initialAlpha = 0.3f),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAFAFA))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = reclamoTexto,
                            onValueChange = { if (it.length <= maxChars) reclamoTexto = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Describe el problema con tu pedido...",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TregoSecondary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                cursorColor = TregoSecondary,
                            ),
                            supportingText = {
                                Text(
                                    "${reclamoTexto.length} / $maxChars",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    fontSize = 11.sp,
                                    color = if (reclamoTexto.length > maxChars * 0.85)
                                        TregoSecondary else Color.Gray
                                )
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (reclamoTexto.isNotBlank()) {
                                        mostrarConfirmarCancelReclamo = true
                                    } else {
                                        reclamar = false
                                        reclamoTexto = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Gray
                                )
                            ) {
                                Text("Cancelar", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val reclamo = DTOCrearReclamoRequest(
                                        idPedido = item.pedido.idPedido,
                                        texto = reclamoTexto
                                    )
                                    onClick(reclamo) {
                                        reclamar = false
                                        reclamoTexto = ""
                                    }
                                },
                                enabled = reclamoTexto.isNotBlank(),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TregoSecondary,
                                    disabledContainerColor = TregoSecondary.copy(alpha = 0.35f)
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enviar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    if (mostrarConfirmarCancelReclamo) {
        ConfirmDialogComponent(
            title = "Cancelar Reclamo",
            message = "¿Estás seguro que deseas cancelar el reclamo para ${item.nombreRestaurante} (Pedido #${item.pedido.idPedido})? Se perderá el mensaje escrito.",
            confirmText = "Sí, Cancelar",
            dismissText = "No, Continuar",
            onConfirm = {
                reclamar = false
                reclamoTexto = ""
                mostrarConfirmarCancelReclamo = false
            },
            onDismiss = { mostrarConfirmarCancelReclamo = false }
        )
    }
}