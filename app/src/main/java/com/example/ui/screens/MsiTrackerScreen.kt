package com.example.ui.screens

import com.example.domain.CreditCardCalculator
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.domain.CashFlowRelease
import com.example.domain.MsiSummary
import com.example.ui.components.CashFlowBarChart
import com.example.ui.components.MsiAmortizationDialog
import com.example.ui.util.rememberPrivacyCurrencyFormat
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MsiTrackerScreen(
    msiList: List<MsiSummary>,
    cashFlowProjections: List<CashFlowRelease>,
    cards: List<CreditCard> = emptyList(),
    allExpenses: List<Expense> = emptyList(),
    allPayments: List<Payment> = emptyList(),
    onAdvanceInstallment: ((Expense) -> Unit)? = null,
    onUndoAdvanceInstallment: ((Expense) -> Unit)? = null,
    onOpenAddExpense: () -> Unit = {},
    onUpdateMsiExpense: (
        expense: Expense,
        concept: String,
        totalAmount: Double,
        monthlyPayment: Double,
        cardId: Long,
        category: String,
        beneficiary: String,
        msiTotalMonths: Int,
        msiCurrentInstallment: Int,
        notes: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onDeleteMsiExpense: ((Expense) -> Unit)? = null
) {
    val currencyFormat = rememberPrivacyCurrencyFormat()
    val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Requisito 2: Pestañas separadas para aquellos que ya hayan finalizado de los activos
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Activos, 1: Finalizados

    // Requisito 5: Filtro por tarjeta con la que se realizó la compra
    var selectedCardFilterId by remember { mutableStateOf<Long?>(null) }

    // Requisito 1: Diálogo de tabla de amortización al tocar un plan de MSI
    var selectedMsiForAmortization by remember { mutableStateOf<MsiSummary?>(null) }

    // Edición y confirmaciones de MSI
    var msiToEdit by remember { mutableStateOf<Expense?>(null) }
    var msiToDelete by remember { mutableStateOf<Expense?>(null) }
    var msiToAdvance by remember { mutableStateOf<Expense?>(null) }
    var msiToUndoAdvance by remember { mutableStateOf<Expense?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Clasificación de MSI activos vs finalizados
    val activeMsiList = remember(msiList) {
        msiList.filter { !it.isCompleted }
    }
    val completedMsiList = remember(msiList) {
        msiList.filter { it.isCompleted }
    }

    val totalMonthlyMsi = activeMsiList.sumOf { it.monthlyPayment }
    val totalRemainingBalance = activeMsiList.sumOf { it.remainingBalance }

    // Lista según la pestaña seleccionada
    val currentTabItems = if (selectedTab == 0) activeMsiList else completedMsiList

    // Lista de tarjetas disponibles en los MSI
    val availableCards = remember(msiList, cards) {
        if (cards.isNotEmpty()) {
            cards
        } else {
            msiList.mapNotNull { it.card }.distinctBy { it.id }
        }
    }

    // Aplicación de filtros (por tarjeta)
    val filteredMsiList = currentTabItems.filter { item ->
        selectedCardFilterId == null || item.expense.cardId == selectedCardFilterId
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // MSI Hero KPI Card (Natural Tones rounded-3xl en contenedor primario)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("msi_kpi_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "COMPROMISO MENSUAL EN MSI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormat.format(totalMonthlyMsi),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "${activeMsiList.size} activos • ${completedMsiList.size} fin.",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saldo total por liquidar:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currencyFormat.format(totalRemainingBalance),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fase 3: Gráfico de Barras de Liberación de Flujo
            CashFlowBarChart(
                projections = cashFlowProjections,
                currencyFormat = currencyFormat
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Proyección de liberación de flujo de efectivo
            Text(
                text = "PLANES POR MES DE LIBERACIÓN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Carrusel de desbloqueos de flujo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                cashFlowProjections.take(6).forEach { proj ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .width(220.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .testTag("cashflow_card_${proj.monthYearLabel}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = proj.monthYearLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "+${currencyFormat.format(proj.monthlyAmountFreed)}/mes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Termina: ${proj.finishingItems.firstOrNull() ?: ""}",
                                fontSize = 11.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Requisito 2: Pestañas de Activos y Finalizados
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("msi_status_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Activos",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${activeMsiList.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_msi_active")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Finalizados",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${completedMsiList.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_msi_completed")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Requisito 5: Filtro por Tarjeta
            Text(
                text = "FILTRAR POR TARJETA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCardFilterId == null,
                    onClick = { selectedCardFilterId = null },
                    label = { Text("Todas las tarjetas", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_card_all")
                )
                availableCards.forEach { card ->
                    val isSelected = selectedCardFilterId == card.id
                    val cardColor = Color(card.primaryColorHex)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCardFilterId = if (isSelected) null else card.id
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cardColor)
                            )
                        },
                        label = { Text(card.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_card_${card.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Encabezado de la lista actual.
            // Corrección: se quitó el texto "Limpiar filtro" que aparecía aquí — era redundante con
            // el chip "Todas las tarjetas" de arriba, que ya hace exactamente lo mismo.
            Text(
                text = if (selectedTab == 0) "PLANES MSI ACTIVOS (${filteredMsiList.size})" else "PLANES MSI FINALIZADOS (${filteredMsiList.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Estado vacío si no hay elementos con los filtros seleccionados
        if (filteredMsiList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Schedule else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0) {
                                if (selectedCardFilterId != null) {
                                    "No hay planes activos que coincidan con la tarjeta seleccionada."
                                } else {
                                    "No tienes compras a meses sin intereses activas en este momento."
                                }
                            } else {
                                if (selectedCardFilterId != null) {
                                    "No hay planes finalizados que coincidan con la tarjeta seleccionada."
                                } else {
                                    "Aún no tienes planes de MSI finalizados."
                                }
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Lista de compras a MSI
        items(filteredMsiList) { item ->
            val exp = item.expense
            val isCompleted = selectedTab == 1 || item.isCompleted
            
            // Requisito 5: Diferenciación visual de acuerdo con la tarjeta
            val cardColor = Color(item.card?.primaryColorHex ?: 0xFF386B1DL)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMsiForAmortization = item }
                    .border(
                        width = 1.dp,
                        color = if (selectedCardFilterId == null) cardColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("msi_item_${exp.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Requisito 5: Diferenciación visual de la tarjeta mediante badge / pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge distintivo de la tarjeta con contraste garantizado en modo oscuro
                        val badgeTextColor = if (isDark) MaterialTheme.colorScheme.onSurface else cardColor
                        val badgeBg = if (isDark) cardColor.copy(alpha = 0.24f) else cardColor.copy(alpha = 0.12f)
                        val badgeBorderColor = if (isDark) cardColor.copy(alpha = 0.55f) else cardColor.copy(alpha = 0.35f)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg,
                            modifier = Modifier.border(0.8.dp, badgeBorderColor, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(cardColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${item.cardName} (${item.card?.bank ?: "TDC"})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor
                                )
                            }
                        }

                        // Beneficiario del gasto y opciones
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Para: ${exp.beneficiary}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            IconButton(
                                onClick = { msiToEdit = exp },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("btn_edit_msi_${exp.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar MSI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Menú de opciones adicionales (oculta el botón de avance para evitar toques accidentales)
                            var showOptionsMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showOptionsMenu = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("btn_menu_msi_${exp.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Más opciones",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showOptionsMenu,
                                    onDismissRequest = { showOptionsMenu = false }
                                ) {
                                    if (!isCompleted && onAdvanceInstallment != null) {
                                        DropdownMenuItem(
                                            text = { Text("Adelantar mensualidad...") },
                                            onClick = {
                                                showOptionsMenu = false
                                                msiToAdvance = exp
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )
                                    }
                                    if (exp.msiCurrentInstallment > 1 && onUndoAdvanceInstallment != null) {
                                        DropdownMenuItem(
                                            text = { Text("Deshacer adelanto (retroceder)") },
                                            onClick = {
                                                showOptionsMenu = false
                                                msiToUndoAdvance = exp
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Undo,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )
                                    }
                                    if (onDeleteMsiExpense != null) {
                                        DropdownMenuItem(
                                            text = { Text("Eliminar plan", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showOptionsMenu = false
                                                msiToDelete = exp
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val icon = when {
                                        exp.concept.contains("iPhone", ignoreCase = true) ||
                                        exp.concept.contains("Galaxy", ignoreCase = true) ||
                                        exp.concept.contains("Pixel", ignoreCase = true) ||
                                        exp.concept.contains("Laptop", ignoreCase = true) -> Icons.Default.Devices
                                        exp.concept.contains("Lavadora", ignoreCase = true) ||
                                        exp.concept.contains("Microondas", ignoreCase = true) ||
                                        exp.concept.contains("Refrigerador", ignoreCase = true) -> Icons.Default.Home
                                        else -> Icons.Default.ShoppingCart
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.concept,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    softWrap = true
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currencyFormat.format(item.monthlyPayment)} / mes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Requisito 3: Monto total de la compra no tan visual o relevante para evitar estrés financiero
                                val totalCost = if (item.totalPurchaseAmount > 0) {
                                    item.totalPurchaseAmount
                                } else {
                                    item.monthlyPayment * exp.msiTotalMonths
                                }
                                Text(
                                    text = "Monto total compra: ${currencyFormat.format(totalCost)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            // Requisito 7: Porcentaje de avance de cada deuda
                            val progressPct = if (isCompleted) 100 else (item.progressPercent * 100).toInt().coerceIn(0, 100)
                            val compContainer = if (isDark) Color(0xFF1B3820) else Color(0xFFD8ECD5)
                            val compText = if (isDark) Color(0xFF81C784) else Color(0xFF2E6C38)
                            
                            if (item.isLastInstallmentPending && !isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF3E2723) else Color(0xFFFFF3CD),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDark) Color(0xFFFFB74D).copy(alpha = 0.7f) else Color(0xFFFFCA28)
                                    )
                                ) {
                                    Text(
                                        text = "Última cuota",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFFB74D) else Color(0xFF856404),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCompleted) compContainer else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (isCompleted) "100% liquidado" else "$progressPct% pagado",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) compText else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Mes ${if (isCompleted) exp.msiTotalMonths else exp.msiCurrentInstallment}/${exp.msiTotalMonths}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (item.isLastInstallmentPending && !isCompleted) {
                                Text(
                                    text = "Por liquidar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color(0xFFFFB74D) else Color(0xFF856404)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra de progreso visual
                    val progressValue = if (isCompleted) 1.0f else item.progressPercent
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCompleted) Color(0xFF2E6C38) else MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFC1CCB9).copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            val compText = if (isDark) Color(0xFF81C784) else Color(0xFF2E6C38)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = compText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Deuda totalmente liquidada",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = compText
                                )
                            }
                            Text(
                                text = "Completado en: ${item.completionDateString}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Faltan ${currencyFormat.format(item.remainingBalance)} (${item.installmentsRemaining} meses)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Requisito 6: Fecha/mes en el que termina la deuda
                            Text(
                                text = "Termina: ${item.completionDateString}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Requisito 1: Indicador de toque para abrir tabla de amortización
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Toca para ver tabla de amortización",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
    )
}

    // Requisito 1: Diálogo con la tabla de amortización e indicación visual de la posición actual
    selectedMsiForAmortization?.let { msiSummary ->
        MsiAmortizationDialog(
            msiSummary = msiSummary,
            allExpenses = allExpenses,
            allPayments = allPayments,
            onDismiss = { selectedMsiForAmortization = null },
            onEdit = {
                val exp = msiSummary.expense
                selectedMsiForAmortization = null
                msiToEdit = exp
            }
        )
    }

    // Diálogo de Edición de MSI
    msiToEdit?.let { expenseToEdit ->
        EditMsiDialog(
            expense = expenseToEdit,
            cards = availableCards,
            onDismiss = { msiToEdit = null },
            onSave = { concept, totalAmount, monthlyPayment, cardId, category, beneficiary, totalMonths, currentInst, notes ->
                onUpdateMsiExpense(
                    expenseToEdit,
                    concept,
                    totalAmount,
                    monthlyPayment,
                    cardId,
                    category,
                    beneficiary,
                    totalMonths,
                    currentInst,
                    notes
                )
                msiToEdit = null
            },
            onDelete = if (onDeleteMsiExpense != null) {
                {
                    val toDel = expenseToEdit
                    msiToEdit = null
                    msiToDelete = toDel
                }
            } else null
        )
    }

    // Diálogo de Confirmación para Eliminar MSI
    msiToDelete?.let { expenseToDelete ->
        AlertDialog(
            onDismissRequest = { msiToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar plan de MSI?", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que deseas eliminar '${expenseToDelete.concept}'? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMsiExpense?.invoke(expenseToDelete)
                        msiToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { msiToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Advertencia y Confirmación para Adelantar Mensualidad Manualmente
    msiToAdvance?.let { exp ->
        val nextInst = exp.msiCurrentInstallment + 1
        AlertDialog(
            onDismissRequest = { msiToAdvance = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿Adelantar mensualidad manualmente?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Estás a punto de avanzar el plan \"${exp.concept}\" de la cuota ${exp.msiCurrentInstallment} a la cuota $nextInst de ${exp.msiTotalMonths}.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Esta acción actualizará el mes de corte al siguiente periodo en tus cuentas asociadas. Recuerda que siempre podrás deshacer esta acción si lo requieres.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentExp = exp
                        msiToAdvance = null
                        onAdvanceInstallment?.invoke(currentExp)
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Se adelantó a la cuota $nextInst de ${currentExp.concept}",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onUndoAdvanceInstallment?.invoke(currentExp.copy(msiCurrentInstallment = nextInst))
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Adelantar Cuota", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { msiToAdvance = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Confirmación para Deshacer Adelanto (Retroceder Mensualidad)
    msiToUndoAdvance?.let { exp ->
        val prevInst = exp.msiCurrentInstallment - 1
        AlertDialog(
            onDismissRequest = { msiToUndoAdvance = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿Deshacer adelanto de mensualidad?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Se retrocederá el plan \"${exp.concept}\" de la cuota ${exp.msiCurrentInstallment} a la cuota $prevInst de ${exp.msiTotalMonths}.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "El mes de corte regresará al periodo anterior correspondiente en tus cuentas asociadas.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentExp = exp
                        msiToUndoAdvance = null
                        onUndoAdvanceInstallment?.invoke(currentExp)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Deshacer Adelanto", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { msiToUndoAdvance = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EditMsiDialog(
    expense: Expense,
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onSave: (
        concept: String,
        totalAmount: Double,
        monthlyPayment: Double,
        cardId: Long,
        category: String,
        beneficiary: String,
        totalMonths: Int,
        currentInst: Int,
        notes: String
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val initialTotal = if (expense.msiTotalPurchaseAmount > 0) expense.msiTotalPurchaseAmount else (expense.amount * expense.msiTotalMonths)
    var conceptText by remember { mutableStateOf(expense.concept) }
    var totalAmountText by remember { mutableStateOf(initialTotal.toString()) }
    var monthlyPaymentText by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCardId by remember { mutableStateOf(expense.cardId) }
    var beneficiaryText by remember { mutableStateOf(expense.beneficiary) }
    var categoryText by remember { mutableStateOf(expense.category) }
    var totalMonthsText by remember { mutableStateOf(expense.msiTotalMonths.toString()) }
    var currentInstText by remember { mutableStateOf(expense.msiCurrentInstallment.toString()) }
    var notesText by remember { mutableStateOf(expense.notes) }
    // Corrección: antes, editar "Monto total" o "Plazo" sobrescribía en silencio la "Mensualidad" aunque
    // el usuario ya la hubiera escrito a mano. Ahora, en cuanto el usuario edita la Mensualidad
    // directamente, se deja de recalcularla automáticamente.
    var monthlyManuallyEdited by remember { mutableStateOf(false) }

    val totalAmount = CreditCardCalculator.parseLocalizedDouble(totalAmountText) ?: 0.0
    val monthlyPayment = CreditCardCalculator.parseLocalizedDouble(monthlyPaymentText) ?: 0.0
    val totalMonths = totalMonthsText.toIntOrNull() ?: 1
    val currentInst = currentInstText.toIntOrNull() ?: 0
    val currencyFormat = rememberPrivacyCurrencyFormat()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Compra a MSI", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = conceptText,
                        onValueChange = { conceptText = it },
                        label = { Text("Concepto de la compra") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_msi_concept")
                    )
                }

                // Selector de tarjeta
                item {
                    Column {
                        Text(
                            text = "Tarjeta asignada:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cards.forEach { card ->
                                val isSelected = card.id == selectedCardId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCardId = card.id },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(card.primaryColorHex))
                                        )
                                    },
                                    label = { Text(card.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalAmountText,
                            onValueChange = {
                                totalAmountText = it
                                if (!monthlyManuallyEdited) {
                                    val tot = it.toDoubleOrNull() ?: 0.0
                                    val m = totalMonthsText.toIntOrNull() ?: 1
                                    if (tot > 0 && m > 0) {
                                        monthlyPaymentText = String.format(Locale.US, "%.2f", tot / m)
                                    }
                                }
                            },
                            label = { Text("Monto total ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_msi_total")
                        )

                        OutlinedTextField(
                            value = monthlyPaymentText,
                            onValueChange = {
                                monthlyPaymentText = it
                                monthlyManuallyEdited = true
                            },
                            label = { Text("Mensualidad ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_msi_monthly")
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalMonthsText,
                            onValueChange = {
                                totalMonthsText = it
                                if (!monthlyManuallyEdited) {
                                    val m = it.toIntOrNull() ?: 1
                                    val tot = totalAmountText.toDoubleOrNull() ?: 0.0
                                    if (tot > 0 && m > 0) {
                                        monthlyPaymentText = String.format(Locale.US, "%.2f", tot / m)
                                    }
                                }
                            },
                            label = { Text("Plazo (meses)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_msi_months")
                        )

                        OutlinedTextField(
                            value = currentInstText,
                            onValueChange = { currentInstText = it },
                            label = { Text("Cuota actual (ej. 12)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_msi_installment")
                        )
                    }
                }

                item {
                    val m = totalMonthsText.toIntOrNull() ?: 1
                    val c = currentInstText.toIntOrNull() ?: 1
                    val mon = monthlyPaymentText.toDoubleOrNull() ?: 0.0
                    val remaining = (m - c).coerceAtLeast(0)
                    val remainingBal = mon * remaining
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Cuota $c de $m | Restan $remaining meses (${currencyFormat.format(remainingBal)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = beneficiaryText,
                        onValueChange = { beneficiaryText = it },
                        label = { Text("Beneficiario (ej. Personal, Familiar)") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_msi_beneficiary")
                    )
                }

                item {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notas / Detalles (Opcional)") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (conceptText.isNotBlank() && (totalAmount > 0.0 || monthlyPayment > 0.0)) {
                        val finalMonthly = if (monthlyPayment > 0.0) monthlyPayment else (totalAmount / totalMonths.coerceAtLeast(1))
                        onSave(
                            conceptText.trim(),
                            totalAmount,
                            finalMonthly,
                            selectedCardId,
                            categoryText,
                            beneficiaryText.trim().ifBlank { "Personal" },
                            totalMonths,
                            currentInst,
                            notesText.trim()
                        )
                    }
                },
                enabled = conceptText.isNotBlank() && (totalAmount > 0.0 || monthlyPayment > 0.0),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_msi_edit")
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
