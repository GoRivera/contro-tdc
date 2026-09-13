package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.domain.StatementPeriod
import com.example.ui.util.LocalPrivacyMode
import com.example.ui.util.PrivacyFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla de tendencias de gasto: total mensual de los últimos meses (para detectar de un vistazo
 * en qué mes se disparó el gasto) y desglose por categoría del mes más reciente con datos.
 */
@Composable
fun SpendingTrendsScreen(
    expenses: List<Expense>,
    onClose: () -> Unit
) {
    val isPrivate = LocalPrivacyMode.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    // Agrupar por periodo de corte y sumar montos; ordenar cronológicamente y quedarse con los
    // últimos 6. StatementPeriod ya sabe compararse/ordenarse, así que no hace falta un comparador
    // manual con el índice del mes.
    val monthlyTotals = remember(expenses) {
        expenses.groupBy { StatementPeriod.from(it.dateMillis, it.targetStatementMonth) }
            .map { (period, list) -> period to list.sumOf { it.amount } }
            .sortedBy { it.first }
            .takeLast(6)
    }

    // Desglose por categoría del mes más reciente con información
    val latestPeriod = monthlyTotals.lastOrNull()?.first
    val categoryBreakdown = remember(expenses, latestPeriod) {
        if (latestPeriod == null) {
            emptyList()
        } else {
            expenses.filter { StatementPeriod.from(it.dateMillis, it.targetStatementMonth) == latestPeriod }
                .groupBy { it.category.ifBlank { "Otros" } }
                .map { (category, list) -> category to list.sumOf { it.amount } }
                .sortedByDescending { it.second }
        }
    }
    val categoryTotal = categoryBreakdown.sumOf { it.second }.coerceAtLeast(0.01)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("spending_trends_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tendencias de Gasto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (monthlyTotals.size < 2) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Necesitas al menos dos meses con gastos registrados para ver la tendencia.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                MonthlySpendingBarChart(monthlyTotals, currencyFormat, isPrivate)
            }
        }

        if (categoryBreakdown.isNotEmpty()) {
            item {
                Text(
                    text = "DESGLOSE POR CATEGORÍA (${latestPeriod?.label ?: ""})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(categoryBreakdown) { (category, amount) ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = PrivacyFormat.format(amount, isPrivate, currencyFormat),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val ratio = (amount / categoryTotal).coerceIn(0.0, 1.0).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ratio)
                                    .height(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun MonthlySpendingBarChart(
    monthlyTotals: List<Pair<StatementPeriod, Double>>,
    currencyFormat: NumberFormat,
    isPrivate: Boolean
) {
    val maxValue = (monthlyTotals.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .testTag("monthly_spending_chart")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "GASTO TOTAL POR MES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyTotals.forEach { (period, total) ->
                    val ratio = (total / maxValue).coerceIn(0.0, 1.0).toFloat()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = PrivacyFormat.format(total, isPrivate, currencyFormat),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Canvas(
                            modifier = Modifier
                                .width(28.dp)
                                .height((100 * ratio).coerceAtLeast(2f).dp)
                        ) {
                            drawRoundRect(
                                color = primaryColor,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = period.normalizedMonthName.take(3),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
