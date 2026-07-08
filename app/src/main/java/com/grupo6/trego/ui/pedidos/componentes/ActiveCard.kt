package com.grupo6.trego.ui.pedidos.componentes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo6.trego.data.model.DTOCrearReclamoRequest
import com.grupo6.trego.data.model.EnumEstadoPedido
import com.grupo6.trego.data.model.PedidoUiModel
import com.grupo6.trego.ui.componentes.ConfirmDialogComponent
import com.grupo6.trego.ui.theme.BlancoCard
import com.grupo6.trego.ui.theme.TregoOrange
import com.grupo6.trego.ui.theme.TregoSecondary
import java.time.format.DateTimeFormatter

data class BadgeColors(
    val backgroundColor: Color,
    val textColor: Color
)

fun getEstadoPedidoColors(estado: EnumEstadoPedido): BadgeColors = when (estado) {
    EnumEstadoPedido.Pagado -> BadgeColors(Color(0xFFE3F2FD), Color(0xFF1565C0))
    EnumEstadoPedido.PagoRechazado -> BadgeColors(Color(0xFFFFEBEE), Color(0xFFC62828))
    EnumEstadoPedido.EnPreparacion -> BadgeColors(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    EnumEstadoPedido.EnCamino -> BadgeColors(Color(0xFFFFF3E0), Color(0xFFEF6C00))
    EnumEstadoPedido.Entregado -> BadgeColors(Color(0xFFE0F2F1), Color(0xFF00695C))
    EnumEstadoPedido.Cancelado -> BadgeColors(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
    EnumEstadoPedido.Reembolsado -> BadgeColors(Color(0xFFE8EAF6), Color(0xFF283593))
    else -> BadgeColors(Color(0xFF000000), Color(0xFFF5F5F5))
}

val timeFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm")

private val estadosPedido = listOf(
    EnumEstadoPedido.Pagado,
    EnumEstadoPedido.EnPreparacion,
    EnumEstadoPedido.EnCamino,
    EnumEstadoPedido.Entregado
)

private fun estadoIndex(estado: EnumEstadoPedido?): Int =
    estadosPedido.indexOf(estado).takeIf { it >= 0 } ?: 0

/**
 * Esta tarjeta muestra toda la información de un pedido que está "vivo". 
 * Incluye un rastreador de pasos para ver si ya se está preparando o si viene en camino, 
 * permite llamar al restaurante directamente y ofrece la opción de cancelar o reclamar.
 */
@Composable
fun ActivePedidoCard(
    order: PedidoUiModel,
    onCancelClick: () -> Unit = {},
    onClickReclamo: (request: DTOCrearReclamoRequest, onSuccess: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val puedeReclamar = order.pedido.estado == EnumEstadoPedido.EnPreparacion ||
            order.pedido.estado == EnumEstadoPedido.EnCamino || order.pedido.estado == EnumEstadoPedido.EnCamino
    var reclamar by remember { mutableStateOf(false) }
    var reclamoTexto by remember { mutableStateOf("") }
    var mostrarConfirmarCancelReclamo by remember { mutableStateOf(false) }
    val maxChars = 200
    val context = LocalContext.current
    val currentStep = estadoIndex(order.pedido.estado)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* Cabezal de la tarjeta con el nombre del restaurante y el botón rápido para llamar por teléfono. */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.nombreRestaurante,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Pedido #${order.pedido.idPedido}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TregoOrange.copy(alpha = 0.12f))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${order.telefonoRestaurante}")
                            }
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Llamar al restaurante",
                        tint = TregoOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            /* Dibujamos la línea de tiempo que muestra en qué etapa está el pedido actualmente. */
            OrderProgressStepper(currentStep = currentStep)

            // --- Divisor ---
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFEBEBEB)
            )

            // --- Productos (expandible) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val cantidad = order.pedido.productos?.size ?: 0
                Text(
                    text = "$cantidad productos pedidos",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    order.pedido.productos?.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.cantidad}x ${item.producto?.nombre ?: "Sin nombre"}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${item.subtotal}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- Divisor ---
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFEBEBEB)
            )

            // --- Hora y total ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hora: ${order.pedido.fechaCreacion?.format(timeFormatter) ?: "--:--"}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total: ",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "$${order.pedido.total}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = TregoOrange
                    )
                }
            }

            // --- Botón cancelar ---
            if (order.pedido.estado == EnumEstadoPedido.Pagado) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancelar pedido",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            /* Sección donde el usuario puede escribir y mandar un mensaje al restaurante si hay un problema. */
            if (puedeReclamar) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = Color(0xFFEBEBEB)
                )
                // AQUI USAMOS EL NUEVO CAMPO DEL BACKEND
                if (order.pedido.tieneReclamo == true) {
                    // Ya tiene reclamo -> Mostramos el mensaje de éxito
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reclamo en proceso",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                } else {
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
                                            idPedido = order.pedido.idPedido,
                                            texto = reclamoTexto
                                        )
                                        onClickReclamo(reclamo) {
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
    }

    if (mostrarConfirmarCancelReclamo) {
        ConfirmDialogComponent(
            title = "Cancelar Reclamo",
            message = "¿Estás seguro que deseas cancelar el reclamo para ${order.nombreRestaurante} (Pedido #${order.pedido.idPedido})? Se perderá el mensaje escrito.",
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

@Composable
private fun OrderProgressStepper(currentStep: Int) {
    val steps = listOf("Pagado", "En Preparacion", "En camino", "Entregado")
    val dotSize = 16.dp
    val trackColor = Color(0xFFE2E2E2)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dotSize)
        ) {
            // Línea de track y progreso dibujadas con Canvas para precisión al píxel
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = dotSize.toPx() / 2f
                val stepWidth = (size.width - 2f * dotRadius) / (steps.size - 1)
                val y = size.height / 2f

                // Track completo
                drawLine(
                    color = trackColor,
                    start = Offset(dotRadius, y),
                    end = Offset(size.width - dotRadius, y),
                    strokeWidth = 2.dp.toPx()
                )

                // Progreso hasta el paso actual
                if (currentStep > 0) {
                    drawLine(
                        color = TregoOrange,
                        start = Offset(dotRadius, y),
                        end = Offset(dotRadius + stepWidth * currentStep, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Dots encima del Canvas, alineados con SpaceBetween
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(if (index <= currentStep) TregoOrange else trackColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Labels debajo de los dots
        Row(modifier = Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                    color = if (index <= currentStep) TregoOrange else Color(0xFFBBBBBB),
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        steps.size - 1 -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}