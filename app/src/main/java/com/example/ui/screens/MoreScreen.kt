package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CreditCardCalculator
import com.example.ui.AppViewModel

@Composable
fun MoreScreen(
    viewModel: AppViewModel,
    onOpenAddSubscription: () -> Unit,
    onOpenAddFuel: () -> Unit,
    onOpenAddService: () -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Subs, 1: Gasolina, 2: Servicios

    val subscriptions by viewModel.subscriptions.collectAsState()
    val fuelEntries by viewModel.fuelEntries.collectAsState()
    val serviceEntries by viewModel.serviceEntries.collectAsState()
    val cards by viewModel.cards.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Servicios y Gastos Fijos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Sub Tabs
        TabRow(selectedTabIndex = subTab) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Suscripciones") },
                icon = { Icon(Icons.Default.Repeat, contentDescription = null) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Gasolina") },
                icon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) }
            )
            Tab(
                selected = subTab == 2,
                onClick = { subTab = 2 },
                text = { Text("Servicios") },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
            )
        }

        when (subTab) {
            0 -> {
                // Subscriptions Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalSubs = subscriptions.sumOf { it.totalMonthlyAmount }
                    Column {
                        Text(
                            text = "Compromiso Mensual",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CreditCardCalculator.formatCurrency(totalSubs),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenAddSubscription,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(subscriptions) { sub ->
                        val card = cards.firstOrNull { it.id == sub.cardId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = CreditCardCalculator.formatCurrency(sub.totalMonthlyAmount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "Cobro día ${sub.billingDayOfMonth} en ${card?.name ?: "TDC"} • ${sub.category}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (sub.participantsSummary.isNotBlank()) {
                                    Text(
                                        text = "División: ${sub.participantsSummary}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Fuel Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avgEff = if (fuelEntries.isNotEmpty()) fuelEntries.map { it.efficiencyKmPerL }.average() else 0.0
                    Column {
                        Text(
                            text = "Eficiencia Promedio",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${"%.2f".format(avgEff)} km/L",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenAddFuel,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Carga")
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(fuelEntries) { entry ->
                        val card = cards.firstOrNull { it.id == entry.cardId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = entry.fuelType,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${entry.kmDriven.toInt()} km recorridos con ${entry.litersLoaded} L ($${entry.pricePerLiter}/L)",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CreditCardCalculator.formatCurrency(entry.totalCost),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${"%.2f".format(entry.efficiencyKmPerL)} km/L",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }

                                if (entry.isDivided) {
                                    Text(
                                        text = "Dividido con ${entry.dividedWith}: Mi parte = ${CreditCardCalculator.formatCurrency(entry.personalShare)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Utilities Services Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalServ = serviceEntries.sumOf { it.amount }
                    Column {
                        Text(
                            text = "Servicios Registrados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CreditCardCalculator.formatCurrency(totalServ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenAddService,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recibo")
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(serviceEntries) { s ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = s.serviceType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = s.notes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (s.consumption > 0) {
                                        Text(
                                            text = "Consumo: ${s.consumption}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = CreditCardCalculator.formatCurrency(s.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
