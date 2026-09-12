package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.domain.CreditCardCalculator
import com.example.domain.StatementSummary
import com.example.ui.components.ChartSlice
import com.example.ui.components.SpendingDonutChart
import com.example.ui.util.rememberPrivacyCurrencyFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatementsScreen(
    cards: List<CreditCard>,
    selectedYear: Int = 2026,
    availableYears: List<Int> = listOf(2026),
    onSelectYear: (Int) -> Unit = {},
    selectedMonth: String,
    availableMonths: List<String>,
    selectedCardId: Long?,
    statementSummary: StatementSummary,
    expenses: List<Expense>,
    payments: List<Payment>,
    onSelectMonth: (String) -> Unit,
    onSelectCard: (Long?) -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddPayment: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onDeletePayment: (Payment) -> Unit
) {
    val currencyFormat = rememberPrivacyCurrencyFormat()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cargos, 1: Abonos
    // Filtro por quincena (null: mes completo, 1: 1ª quincena <= día 15, 2: 2ª quincena > día 15)
    var selectedFortnight by remember { mutableStateOf<Int?>(null) }

    // Requisito 7: Filtro por responsable
    var selectedBeneficiaryFilter by remember { mutableStateOf<String?>(null) }

    // Requisito 4: Diálogos de confirmación y bloqueo para MSI y Suscripciones
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showMsiBlockedDialog by remember { mutableStateOf<Expense?>(null) }
    var showSubscriptionBlockedDialog by remember { mutableStateOf<Expense?>(null) }
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }
    var showInterestSimulatorDialog by remember { mutableStateOf(false) }

    val normSelectedMonth = CreditCardCalculator.normalizeMonth(selectedMonth)
    val cardMap = remember(cards) { cards.associateBy { it.id } }
    val selectedCard = remember(cards, selectedCardId) {
        cards.firstOrNull { it.id == selectedCardId }
    }

    // Requisito 3: Almacenamiento y filtrado por año y mes
    val allMonthExpenses = remember(expenses, normSelectedMonth, selectedYear, selectedCardId) {
        expenses.filter {
            CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == selectedYear &&
            CreditCardCalculator.normalizeMonth(it.targetStatementMonth) == normSelectedMonth &&
            (selectedCardId == null || it.cardId == selectedCardId)
        }
    }

    val allMonthPayments = remember(payments, normSelectedMonth, selectedYear, selectedCardId) {
        payments.filter {
            CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == selectedYear &&
            CreditCardCalculator.normalizeMonth(it.targetStatementMonth) == normSelectedMonth &&
            (selectedCardId == null || it.cardId == selectedCardId)
        }
    }

    // Cálculos de 1ª Quincena (límite de pago <= 15)
    val q1Expenses = remember(allMonthExpenses, cardMap) {
        allMonthExpenses.filter { (cardMap[it.cardId]?.paymentDueDay ?: 5) <= 15 }
    }
    val q1Payments = remember(allMonthPayments, cardMap) {
        allMonthPayments.filter { (cardMap[it.cardId]?.paymentDueDay ?: 5) <= 15 }
    }
    val q1TotalCharges = q1Expenses.sumOf { it.amount }
    val q1TotalPayments = q1Payments.sumOf { it.amount }
    val q1Remaining = (q1TotalCharges - q1TotalPayments).coerceAtLeast(0.0)

    // Cálculos de 2ª Quincena (límite de pago > 15)
    val q2Expenses = remember(allMonthExpenses, cardMap) {
        allMonthExpenses.filter { (cardMap[it.cardId]?.paymentDueDay ?: 20) > 15 }
    }
    val q2Payments = remember(allMonthPayments, cardMap) {
        allMonthPayments.filter { (cardMap[it.cardId]?.paymentDueDay ?: 20) > 15 }
    }
    val q2TotalCharges = q2Expenses.sumOf { it.amount }
    val q2TotalPayments = q2Payments.sumOf { it.amount }
    val q2Remaining = (q2TotalCharges - q2TotalPayments).coerceAtLeast(0.0)

    // Listas filtradas según la quincena seleccionada
    val filteredExpenses = when (selectedFortnight) {
        1 -> q1Expenses
        2 -> q2Expenses
        else -> allMonthExpenses
    }

    val filteredPayments = when (selectedFortnight) {
        1 -> q1Payments
        2 -> q2Payments
        else -> allMonthPayments
    }

    val displayExpenses = remember(filteredExpenses, selectedBeneficiaryFilter) {
        if (selectedBeneficiaryFilter == null) {
            filteredExpenses
        } else {
            filteredExpenses.filter { it.beneficiary.trim().equals(selectedBeneficiaryFilter, ignoreCase = true) }
        }
    }

    // Totales dinámicos para la tarjeta principal
    val activeTotalCharges = when (selectedFortnight) {
        1 -> q1TotalCharges
        2 -> q2TotalCharges
        else -> statementSummary.totalCharges
    }
    val activeTotalPayments = when (selectedFortnight) {
        1 -> q1TotalPayments
        2 -> q2TotalPayments
        else -> statementSummary.totalPayments
    }
    val activeRemaining = when (selectedFortnight) {
        1 -> q1Remaining
        2 -> q2Remaining
        else -> statementSummary.remainingBalance
    }
    val isPeriodPaid = when (selectedFortnight) {
        1 -> q1Remaining <= 0
        2 -> q2Remaining <= 0
        else -> statementSummary.isFullyPaid
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Requisito 3: Filtros de Año y Mes (mostrando solo aquellos con información)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Años disponibles (solo los que tienen información)
                availableYears.forEach { yr ->
                    val isYearSelected = yr == selectedYear
                    FilterChip(
                        selected = isYearSelected,
                        onClick = { onSelectYear(yr) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = {
                            Text(
                                text = "$yr",
                                fontWeight = if (isYearSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isYearSelected,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("year_chip_$yr")
                    )
                }

                if (availableYears.isNotEmpty() && availableMonths.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )
                }

                // Meses disponibles para el año seleccionado
                availableMonths.forEach { m ->
                    val isSelected = m == selectedMonth
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMonth(m) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = {
                            Text(
                                text = CreditCardCalculator.normalizeMonth(m),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("month_chip_$m")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Card Filter Chips (filtradas por quincena si hay un filtro de quincena activo)
            val visibleCards = remember(cards, selectedFortnight) {
                when (selectedFortnight) {
                    1 -> cards.filter { it.paymentDueDay <= 15 }
                    2 -> cards.filter { it.paymentDueDay > 15 }
                    else -> cards
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCardId == null,
                    onClick = { onSelectCard(null) },
                    label = { 
                        Text(
                            text = when (selectedFortnight) {
                                1 -> "Todas (1ª Q)"
                                2 -> "Todas (2ª Q)"
                                else -> "Todas las tarjetas"
                            }, 
                            fontWeight = if (selectedCardId == null) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCardId == null,
                        borderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("card_filter_all")
                )

                visibleCards.forEach { card ->
                    val isSelected = card.id == selectedCardId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCard(card.id) },
                        leadingIcon = {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color(card.primaryColorHex),
                                modifier = Modifier.size(12.dp)
                            ) {}
                        },
                        label = { Text(card.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("card_filter_${card.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Requisito 3: Si se toca una tarjeta en particular, la vista cambia a mostrar 'Se paga en la quincena del...'
            // Si no hay tarjeta seleccionada, muestra los filtros y totales de la 1ª y 2ª quincena
            if (selectedCard != null) {
                val due = selectedCard.paymentDueDay
                val fortnightPeriod = if (due <= 15) "1 al 15" else "16 al fin de mes"
                val fortnightName = if (due <= 15) "1ª Quincena" else "2ª Quincena"
                val (_, paymentDueDate, isShifted) = remember(selectedCard) { CreditCardCalculator.calculateCycleDatesDetailed(selectedCard) }
                val dueDateFormat = remember { SimpleDateFormat("dd 'de' MMMM", Locale("es", "MX")) }
                val effectiveDueStr = dueDateFormat.format(paymentDueDate)

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("single_card_fortnight_notice")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PROGRAMACIÓN DE PAGO QUINCENAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Se paga en la quincena del $fortnightPeriod",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "$fortnightName • Límite oficial: $effectiveDueStr",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                                if (isShifted) {
                                    Text(
                                        text = "📅 Recorrido al siguiente día hábil bancario por calendario Banxico",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (statementSummary.remainingBalance > 0.0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { showInterestSimulatorDialog = true },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Simular Pago Mínimo vs Totalero", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "PAGOS POR QUINCENA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Chips para selección rápida de quincena
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFortnight == null,
                        onClick = { selectedFortnight = null },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Mes Completo", fontWeight = if (selectedFortnight == null) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("filter_fortnight_all")
                    )

                    FilterChip(
                        selected = selectedFortnight == 1,
                        onClick = { selectedFortnight = if (selectedFortnight == 1) null else 1 },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Antes del 15", fontWeight = if (selectedFortnight == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("filter_fortnight_1")
                    )

                    FilterChip(
                        selected = selectedFortnight == 2,
                        onClick = { selectedFortnight = if (selectedFortnight == 2) null else 2 },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Fin de mes", fontWeight = if (selectedFortnight == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("filter_fortnight_2")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Requisito 3: Visualización de totales de gastos y abonos por cada quincena
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tarjeta A pagar antes del 15 (límite de pago <= día 15)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFortnight = if (selectedFortnight == 1) null else 1 }
                            .border(
                                width = if (selectedFortnight == 1) 2.dp else 1.dp,
                                color = if (selectedFortnight == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("card_q1_summary"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedFortnight == 1) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1ª Quincena",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Antes del 15",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Gastos:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(q1TotalCharges),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Abonos:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(q1TotalPayments),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Por pagar:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(q1Remaining),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (q1Remaining <= 0) MaterialTheme.colorScheme.primary else Color(0xFFBA1A1A)
                            )
                        }
                    }

                    // Tarjeta A pagar antes de fin de mes (límite de pago > día 15)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFortnight = if (selectedFortnight == 2) null else 2 }
                            .border(
                                width = if (selectedFortnight == 2) 2.dp else 1.dp,
                                color = if (selectedFortnight == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("card_q2_summary"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedFortnight == 2) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2ª Quincena",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Fin de mes",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Gastos:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(q2TotalCharges),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("Abonos:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(q2TotalPayments),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Por pagar:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(q2Remaining),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (q2Remaining <= 0) MaterialTheme.colorScheme.primary else Color(0xFFBA1A1A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Statement Main Header Card (Natural Tones rounded-3xl)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("statement_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = statementSummary.cardName.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val periodLabel = when (selectedFortnight) {
                                1 -> "Periodo: ${statementSummary.statementMonth} • 1ª Q"
                                2 -> "Periodo: ${statementSummary.statementMonth} • 2ª Q"
                                else -> "Periodo: ${statementSummary.statementMonth}"
                            }
                            Text(
                                text = periodLabel,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = if (isPeriodPaid) MaterialTheme.colorScheme.primary else Color(0xFFBA1A1A),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (isPeriodPaid) "LIQUIDADO" else "POR PAGAR",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dates row (Corte & Límite)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Corte", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(statementSummary.cutoffDateString, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Límite Pago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(statementSummary.paymentDueDateString, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Financial metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL GASTOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(activeTotalCharges),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL ABONOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(activeTotalPayments),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("RESTANTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(activeRemaining),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeRemaining <= 0) MaterialTheme.colorScheme.primary else Color(0xFFBA1A1A)
                            )
                        }
                    }
                }
            }

            // Fase 3: Gráfico de Distribución de Gastos (Dona Interactiva)
            var chartDisplayMode by remember { mutableIntStateOf(0) } // 0: Categoría, 1: Responsable

            if (allMonthExpenses.isNotEmpty() && activeTotalCharges > 0) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISTRIBUCIÓN VISUAL DE GASTOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = chartDisplayMode == 0,
                            onClick = { chartDisplayMode = 0 },
                            label = { Text("Categorías", fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                        FilterChip(
                            selected = chartDisplayMode == 1,
                            onClick = { chartDisplayMode = 1 },
                            label = { Text("Personas", fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val palette = listOf(
                    Color(0xFF2E6C38),
                    Color(0xFF0D5EAF),
                    Color(0xFFD97706),
                    Color(0xFFBA1A1A),
                    Color(0xFF7C3AED),
                    Color(0xFF0284C7),
                    Color(0xFF059669),
                    Color(0xFFDC2626)
                )

                val slices = remember(allMonthExpenses, chartDisplayMode) {
                    if (chartDisplayMode == 0) {
                        allMonthExpenses.groupBy { it.category.ifBlank { "General" } }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .entries.sortedByDescending { it.value }
                            .mapIndexed { idx, (cat, amt) ->
                                ChartSlice(cat, amt, palette[idx % palette.size])
                            }
                    } else {
                        allMonthExpenses.groupBy { it.beneficiary.trim().ifBlank { "Personal" } }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .entries.sortedByDescending { it.value }
                            .mapIndexed { idx, (ben, amt) ->
                                ChartSlice(ben, amt, palette[idx % palette.size])
                            }
                    }
                }

                SpendingDonutChart(
                    slices = slices,
                    currencyFormat = currencyFormat,
                    title = if (chartDisplayMode == 0) "Gastos por Categoría" else "Gastos por Responsable",
                    onSliceSelected = { slice ->
                        if (chartDisplayMode == 1 && slice != null) {
                            selectedBeneficiaryFilter = slice.label
                        }
                    }
                )
            }

            // Requisito 7: Filtro por Responsable para saber cuánto y qué debe cada persona, cuándo debe pagarse y a qué tarjeta
            val availableBeneficiaries = remember(allMonthExpenses) {
                allMonthExpenses.map { it.beneficiary.trim().ifBlank { "Personal" } }.distinct().sorted()
            }

            if (availableBeneficiaries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILTRAR POR RESPONSABLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedBeneficiaryFilter != null) {
                        Text(
                            text = "Ver todos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { selectedBeneficiaryFilter = null }
                                .testTag("clear_beneficiary_filter")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedBeneficiaryFilter == null,
                        onClick = { selectedBeneficiaryFilter = null },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Todos (${availableBeneficiaries.size})", fontWeight = if (selectedBeneficiaryFilter == null) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("beneficiary_filter_all")
                    )

                    availableBeneficiaries.forEach { person ->
                        val isPersonSelected = person.equals(selectedBeneficiaryFilter, ignoreCase = true)
                        val personTotal = allMonthExpenses.filter { it.beneficiary.trim().equals(person, ignoreCase = true) }.sumOf { it.amount }
                        FilterChip(
                            selected = isPersonSelected,
                            onClick = {
                                selectedBeneficiaryFilter = if (isPersonSelected) null else person
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = {
                                Text(
                                    text = "$person • ${currencyFormat.format(personTotal)}",
                                    fontWeight = if (isPersonSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("beneficiary_filter_$person")
                        )
                    }
                }

                // Requisito 7: Si hay un responsable seleccionado, mostramos la tarjeta detallada de cuánto y qué debe, cuándo pagar y a qué tarjeta
                if (selectedBeneficiaryFilter != null) {
                    val activePerson = selectedBeneficiaryFilter!!
                    val personExpenses = remember(allMonthExpenses, activePerson) {
                        allMonthExpenses.filter { it.beneficiary.trim().equals(activePerson, ignoreCase = true) }
                    }
                    val totalOwedByPerson = personExpenses.sumOf { it.amount }
                    val expensesByCard = remember(personExpenses) {
                        personExpenses.groupBy { it.cardId }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                            .testTag("beneficiary_breakdown_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "DEUDA DE $activePerson",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = currencyFormat.format(totalOwedByPerson),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "DESGLOSE POR TARJETA Y VENCIMIENTO:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            expensesByCard.forEach { (cardId, expList) ->
                                val card = cardMap[cardId]
                                val subtotal = expList.sumOf { it.amount }
                                val dueDay = card?.paymentDueDay ?: 5
                                val isQ1 = dueDay <= 15
                                val payTiming = if (isQ1) "A pagar antes del 15 (Día $dueDay)" else "A pagar antes de fin de mes (Día $dueDay)"

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = Color(card?.primaryColorHex ?: 0xFF1B365D),
                                                    modifier = Modifier.size(10.dp)
                                                ) {}
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${card?.name ?: "TDC"} (${card?.bank ?: "Banco"})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = currencyFormat.format(subtotal),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "🗓️ $payTiming",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Qué debe: " + expList.joinToString(", ") { "${it.concept} (${currencyFormat.format(it.amount)})" },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Row (Cargos vs Abonos)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Cargos (${filteredExpenses.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_charges")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Abonos (${filteredPayments.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_payments")
                )
            }
        }

        // Section Content
        if (selectedTab == 0) {
            // CARGOS LIST
            if (displayExpenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedBeneficiaryFilter != null) "No hay cargos para $selectedBeneficiaryFilter en este periodo" else "No hay cargos registrados en este periodo",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenAddExpense,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Añadir Gasto")
                        }
                    }
                }
            } else {
                items(displayExpenses) { exp ->
                    val card = cardMap[exp.cardId]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .testTag("expense_row_${exp.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = exp.concept,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (exp.isMsi) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "MSI [${exp.msiCurrentInstallment}/${exp.msiTotalMonths}]",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (exp.isSubscription) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Suscripción",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isQ1 = (card?.paymentDueDay ?: 5) <= 15
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isQ1) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (isQ1) "Antes del 15" else "Fin de mes",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isQ1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${card?.name ?: "TDC"} • ${CreditCardCalculator.formatDate(exp.dateMillis)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!exp.beneficiary.equals("Personal", ignoreCase = true)) {
                                        Text(
                                            text = " • Para: ${exp.beneficiary}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = currencyFormat.format(exp.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Requisito 2 & 4 & 5: Confirmación al eliminar; MSI tiene hasta 60 días para eliminarse de forma invisible para el usuario
                            val msiAgeMillis = System.currentTimeMillis() - exp.dateMillis
                            val isMsiOver60Days = exp.isMsi && msiAgeMillis > (60L * 24 * 60 * 60 * 1000)

                            IconButton(
                                onClick = {
                                    if (isMsiOver60Days) {
                                        showMsiBlockedDialog = exp
                                    } else if (exp.isSubscription) {
                                        showSubscriptionBlockedDialog = exp
                                    } else {
                                        expenseToDelete = exp
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_expense_${exp.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = if (isMsiOver60Days || exp.isSubscription) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ABONOS LIST
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onOpenAddPayment,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("statement_add_payment_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar Abono", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredPayments.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay abonos registrados para este periodo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredPayments) { pay ->
                    val card = cardMap[pay.cardId]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .testTag("payment_row_${pay.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pay.concept,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isQ1 = (card?.paymentDueDay ?: 5) <= 15
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isQ1) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (isQ1) "Antes del 15" else "Fin de mes",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isQ1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${card?.name ?: "TDC"} • ${CreditCardCalculator.formatDate(pay.dateMillis)} • Por: ${pay.sourcePayer}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = currencyFormat.format(pay.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = { paymentToDelete = pay },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_payment_${pay.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Requisito 4: Diálogo cuando se intenta eliminar un cargo a MSI
    if (showMsiBlockedDialog != null) {
        val blockedExp = showMsiBlockedDialog!!
        AlertDialog(
            onDismissRequest = { showMsiBlockedDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Cargo a Meses Sin Intereses", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "El cargo \"${blockedExp.concept}\" forma parte de un plan a Meses Sin Intereses [${blockedExp.msiCurrentInstallment} de ${blockedExp.msiTotalMonths}].\n\nPor seguridad contable y para mantener la integridad de las amortizaciones y proyecciones de flujo, no se permite eliminar cargos individuales a MSI."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showMsiBlockedDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Requisito 5: Diálogo cuando se intenta eliminar un cargo de suscripción recurrente
    if (showSubscriptionBlockedDialog != null) {
        val blockedSub = showSubscriptionBlockedDialog!!
        AlertDialog(
            onDismissRequest = { showSubscriptionBlockedDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Suscripción Recurrente Protegida", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "El cargo \"${blockedSub.concept}\" proviene de una suscripción recurrente y no puede eliminarse manualmente para no alterar la contabilidad histórica.\n\nSolo puede modificarse en caso de que la suscripción cambie de precio o de tarjeta desde la sección \"Suscripciones\", afectando únicamente a los movimientos del mes en curso o posteriores."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSubscriptionBlockedDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Requisito 4: Diálogo de confirmación para eliminar cargo normal
    if (expenseToDelete != null) {
        val exp = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("¿Eliminar cargo?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                val msiNote = if (exp.isMsi) {
                    "\n\nNota MSI: Este cargo forma parte de un plan a ${exp.msiTotalMonths} meses sin intereses registrado hace menos de 60 días. Al eliminarlo, se retirará de tu estado de cuenta."
                } else ""
                Text(
                    "¿Estás seguro de que deseas eliminar el cargo \"${exp.concept}\" por ${currencyFormat.format(exp.amount)}?$msiNote\n\nEsta acción recalculará los balances del estado de cuenta."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = expenseToDelete
                        expenseToDelete = null
                        if (toDel != null) onDeleteExpense(toDel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Requisito 4: Diálogo de confirmación para eliminar abono
    if (paymentToDelete != null) {
        val pay = paymentToDelete!!
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("¿Eliminar abono?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar este abono de ${currencyFormat.format(pay.amount)} (${pay.concept})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = paymentToDelete
                        paymentToDelete = null
                        if (toDel != null) onDeletePayment(toDel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo del Simulador de Intereses (Pago Mínimo vs Pago Totalero)
    val dialogCard = selectedCard
    if (showInterestSimulatorDialog && dialogCard != null) {
        val remaining = statementSummary.remainingBalance
        val estimatedRate = 60.0 // Tasa promedio ponderada anual bancaria
        val sim = remember(dialogCard, remaining) {
            CreditCardCalculator.simulateMinimumPaymentPayoff(remaining, estimatedRate)
        }

        AlertDialog(
            onDismissRequest = { showInterestSimulatorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Simulador de Pago Mínimo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tarjeta: ${dialogCard.name} (Tasa anual promedio: ${estimatedRate.toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Saldo pendiente a financiar: ${currencyFormat.format(remaining)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ Si sólo pagas el mínimo (~${currencyFormat.format(sim.firstMonthMinimumPayment)}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Tardarás ${sim.monthsToPayOff} meses (${String.format(Locale.US, "%.1f", sim.monthsToPayOff / 12.0)} años) en liquidar.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "• Pagarás ${currencyFormat.format(sim.totalInterestPaid)} SOLO de intereses.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "• Costo total final: ${currencyFormat.format(sim.totalAmountPaidWithMinimums)}.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 Recomendación Totalero:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¡Paga ${currencyFormat.format(remaining)} antes de tu fecha límite y ahórrate ${currencyFormat.format(sim.totalInterestPaid)} en intereses!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showInterestSimulatorDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}
