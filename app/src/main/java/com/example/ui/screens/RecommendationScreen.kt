package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CardRecommendation
import com.example.domain.CreditCardCalculator
import com.example.domain.StatementSummary
import com.example.domain.TrafficLightStatus
import com.example.ui.util.LocalPrivacyMode
import com.example.ui.util.PrivacyFormat
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RecommendationScreen(
    recommendations: List<CardRecommendation>,
    statementSummary: StatementSummary,
    onOpenAddExpense: () -> Unit,
    onOpenAddPayment: () -> Unit,
    onSelectCardForStatement: (Long) -> Unit,
    onAddCard: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val isPrivate = LocalPrivacyMode.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    // Requisito 2: Las tarjetas departamentales nunca deben mostrarse como la principal recomendación
    val bestCard = recommendations.firstOrNull { it.isBestOption && !it.card.isDepartmental }
        ?: recommendations.firstOrNull { !it.card.isDepartmental }
        ?: recommendations.firstOrNull()

    // Filtro de pestañas: 0 = Todas, 1 = Bancarias / Tradicionales, 2 = Tiendas Departamentales (Liverpool, Sears, etc.)
    var selectedStoreCategoryTab by remember { mutableIntStateOf(0) }

    val filteredRecommendations = remember(recommendations, selectedStoreCategoryTab) {
        when (selectedStoreCategoryTab) {
            1 -> recommendations.filter { !it.card.isDepartmental }
            2 -> recommendations.filter { it.card.isDepartmental }
            else -> recommendations
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAddExpense,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_add_expense_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar Gasto", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAddPayment,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_add_payment_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                    )
                ) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar Abono", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Estado vacío de primer uso: sin tarjetas registradas todavía
            if (recommendations.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("home_empty_state_no_cards")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Agrega tu primera tarjeta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registra una tarjeta de crédito para empezar a recibir recomendaciones y llevar el control de tus gastos.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddCard,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agregar Tarjeta", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Resumen activo del periodo de facturación
            if (statementSummary.totalCharges > 0.0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTADO DE CUENTA: ${statementSummary.statementMonth.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Saldo: ${PrivacyFormat.format(statementSummary.remainingBalance, isPrivate, currencyFormat)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val fullyPaidText = if (isDark) Color(0xFF81C784) else Color(0xFF2E6C38)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (statementSummary.isFullyPaid) fullyPaidText.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (statementSummary.isFullyPaid) "Al corriente" else "Por liquidar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (statementSummary.isFullyPaid) fullyPaidText else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Top Recommendation Highlight with Traffic Light
            if (bestCard != null) {
                val (bestStatusColor, bestContainerColor) = getTrafficLightColors(bestCard.trafficLight)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            bestStatusColor,
                            RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = bestContainerColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = bestStatusColor,
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SEMÁFORO: ${bestCard.trafficLight.title.uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bestStatusColor,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+${bestCard.daysOfFinancing} días de financiamiento",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = bestStatusColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bestCard.card.name,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = bestCard.recommendationReason,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Guía del Semáforo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SEMÁFORO DE TARJETAS PARA HOY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFF2E6C38), modifier = Modifier.size(8.dp)) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verde: Óptima (>38d)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFF856404), modifier = Modifier.size(8.dp)) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Amarillo: Moderada", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFFB3261E), modifier = Modifier.size(8.dp)) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rojo: Corte próximo", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header y Pestañas de Filtrado (Departamentales vs Bancarias)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESTADO Y RECOMENDACIÓN POR TARJETA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pestañas / Filtros por tipo de tarjeta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStoreCategoryTab == 0,
                        onClick = { selectedStoreCategoryTab = 0 },
                        label = { Text("Todas (${recommendations.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedStoreCategoryTab == 1,
                        onClick = { selectedStoreCategoryTab = 1 },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            val bankCount = recommendations.count { !it.card.isDepartmental }
                            Text("Bancarias ($bankCount)", fontSize = 12.sp)
                        }
                    )
                    FilterChip(
                        selected = selectedStoreCategoryTab == 2,
                        onClick = { selectedStoreCategoryTab = 2 },
                        leadingIcon = {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            val deptCount = recommendations.count { it.card.isDepartmental }
                            Text("Departamentales ($deptCount)", fontSize = 12.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // List of all cards ranked with traffic light
        items(filteredRecommendations) { rec ->
            val (statusColor, containerColor) = getTrafficLightColors(rec.trafficLight)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (rec.isBestOption) 2.dp else 1.dp,
                        color = if (rec.isBestOption) statusColor else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("card_rank_${rec.card.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left color traffic light indicator dot & icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = rec.card.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Traffic light status badge
                            Surface(
                                color = containerColor,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = statusColor,
                                        modifier = Modifier.size(6.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = rec.trafficLight.title,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }

                            if (rec.card.isDepartmental) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Departamental",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${rec.card.bank} • Corte: día ${rec.card.cutoffDay} • Pagar: día ${rec.card.paymentDueDay}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = rec.recommendationReason,
                            fontSize = 11.sp,
                            fontWeight = if (rec.isBestOption) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (rec.trafficLight == com.example.domain.TrafficLightStatus.GREEN) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Alerta Banxico (pago recorrido a día hábil)
                        if (rec.isPaymentShiftedByHoliday) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "📅 Banxico: Pago recorrido al sig. día hábil",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Days badge with color
                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${rec.daysOfFinancing}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = "días",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun getTrafficLightColors(status: TrafficLightStatus): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    val statusColor = when (status) {
        TrafficLightStatus.GREEN -> if (isDark) Color(0xFF81C784) else Color(0xFF2E6C38)
        TrafficLightStatus.YELLOW -> if (isDark) Color(0xFFFFB74D) else Color(0xFF856404)
        TrafficLightStatus.RED -> if (isDark) Color(0xFFE57373) else Color(0xFFB3261E)
    }
    val containerColor = when (status) {
        TrafficLightStatus.GREEN -> if (isDark) Color(0xFF143019) else Color(0xFFD8ECD5)
        TrafficLightStatus.YELLOW -> if (isDark) Color(0xFF332005) else Color(0xFFFFF3CD)
        TrafficLightStatus.RED -> if (isDark) Color(0xFF381414) else Color(0xFFF9DEDC)
    }
    return Pair(statusColor, containerColor)
}
