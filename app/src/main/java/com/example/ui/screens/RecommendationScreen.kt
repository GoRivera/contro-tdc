package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardRecommendation
import com.example.data.model.TrafficLight
import com.example.domain.CreditCardCalculator
import com.example.ui.AppViewModel
import com.example.ui.components.CreditCardVisual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    viewModel: AppViewModel,
    onOpenAddExpense: (cardId: Long) -> Unit
) {
    val recommendations = viewModel.getRecommendations()
    val bestCard = recommendations.firstOrNull { it.isBestOption }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Column {
                Text(
                    text = "Mejor Tarjeta Hoy",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Algoritmo de ciclo de facturación y días de financiamiento libre de intereses",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Best Card Feature Card
        if (bestCard != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF10B981))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Recomendada",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RECOMENDADA PARA COMPRAR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF047857),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = "${bestCard.daysOfFinancing} DÍAS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // The visual card
                        val balance = viewModel.getCardBalance(bestCard.card)
                        CreditCardVisual(
                            card = bestCard.card,
                            availableCredit = balance.availableCredit
                        )

                        Text(
                            text = bestCard.recommendationReason,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Próximo Corte",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = bestCard.nextCutoffDateFormatted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Fecha Límite de Pago",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = bestCard.paymentDueDateFormatted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (bestCard.isPaymentShiftedByHoliday) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (bestCard.isPaymentShiftedByHoliday) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0284C7).copy(alpha = 0.1f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Feriado",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Vencimiento recorrido por fin de semana o día feriado bancario en México (Art. 65 LIC).",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7),
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Button(
                            onClick = { onOpenAddExpense(bestCard.card.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrar Compra con esta Tarjeta", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section Title: Other Cards
        item {
            Text(
                text = "Otras Opciones para Hoy",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List of other cards
        items(recommendations.filter { !it.isBestOption }) { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Traffic light badge
                        val trafficColor = when (rec.trafficLight) {
                            TrafficLight.GREEN -> Color(0xFF10B981)
                            TrafficLight.YELLOW -> Color(0xFFF59E0B)
                            TrafficLight.RED -> Color(0xFFEF4444)
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(trafficColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = rec.card.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${rec.card.bank} • Corte: día ${rec.card.cutoffDay}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Límite de pago: ${rec.paymentDueDateFormatted}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (rec.trafficLight) {
                                TrafficLight.GREEN -> Color(0xFF10B981).copy(alpha = 0.15f)
                                TrafficLight.YELLOW -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                TrafficLight.RED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = "${rec.daysOfFinancing} días",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = when (rec.trafficLight) {
                                    TrafficLight.GREEN -> Color(0xFF047857)
                                    TrafficLight.YELLOW -> Color(0xFFB45309)
                                    TrafficLight.RED -> Color(0xFFB91C1C)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        IconButton(
                            onClick = { onOpenAddExpense(rec.card.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Registrar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
