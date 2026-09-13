package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subscriptions
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import com.example.ui.util.rememberPrivacyCurrencyFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionsScreen(
    subscriptions: List<Subscription>,
    trackings: List<SubscriptionPaymentTracking>,
    cards: List<CreditCard>,
    selectedYearMonth: String,
    userProfileName: String = "Usuario",
    onSelectYearMonth: (String) -> Unit,
    onAddSubscription: (
        name: String,
        cardId: Long,
        billingDayOfMonth: Int,
        totalMonthlyAmount: Double,
        category: String,
        participants: List<Pair<String, Double>>,
        periodicity: String
    ) -> Unit,
    onUpdateSubscription: (
        subscription: Subscription,
        newName: String,
        newCardId: Long,
        newBillingDay: Int,
        newTotalMonthlyAmount: Double,
        newCategory: String,
        participants: List<Pair<String, Double>>,
        affectCurrentMonth: Boolean,
        periodicity: String
    ) -> Unit,
    onToggleTrackingPaid: (SubscriptionPaymentTracking) -> Unit,
    onMarkAllPaid: (subscriptionId: Long) -> Unit,
    onToggleActive: (Subscription) -> Unit,
    onDeleteSubscription: ((subscription: Subscription, keepCurrentMonthCharge: Boolean) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val currencyFormat = rememberPrivacyCurrencyFormat()
    val cardMap = remember(cards) { cards.associateBy { it.id } }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<Subscription?>(null) }
    var subscriptionToDelete by remember { mutableStateOf<Subscription?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedParticipantFilter by remember { mutableStateOf<String?>(null) }
    var showMonthPickerDialog by remember { mutableStateOf(false) }

    val availableYearMonths = remember {
        val list = mutableListOf<Pair<String, String>>()
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val monthNames = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        for (year in (currentYear - 1)..(currentYear + 2)) {
            for (month in 1..12) {
                val ym = String.format(Locale.US, "%04d-%02d", year, month)
                val label = "${monthNames[month - 1]} $year"
                list.add(ym to label)
            }
        }
        list
    }

    val activeSubscriptions = remember(subscriptions) { subscriptions.filter { it.isActive } }
    val totalMonthlyCost = remember(activeSubscriptions) { activeSubscriptions.sumOf { it.monthlyEquivalentAmount } }

    val trackingsBySubId = remember(trackings) { trackings.groupBy { it.subscriptionId } }

    // Summary calculations
    val totalPaid = remember(trackings) {
        trackings.filter { it.isPaid }.sumOf { it.amountOwed }
    }
    val totalPending = remember(trackings) {
        trackings.filter { !it.isPaid }.sumOf { it.amountOwed }
    }

    val pendingByParticipant = remember(trackings) {
        trackings.filter { !it.isPaid }
            .groupBy { it.participantName }
            .mapValues { entry -> entry.value.sumOf { it.amountOwed } }
            .filter { it.value > 0 }
    }

    // Requisito 9: Filtrar por participante y calcular lo que debe cada uno a la fecha actual
    val allParticipantNames = remember(trackings, activeSubscriptions) {
        val fromTrackings = trackings.map { it.participantName.trim() }
        val fromSubs = activeSubscriptions.flatMap { sub ->
            sub.participantsSummary.split(",").mapNotNull { entry ->
                entry.substringBefore(":").trim().takeIf { it.isNotBlank() }
            }
        }
        (fromTrackings + fromSubs).distinct().filter { it.isNotBlank() }.sorted()
    }

    val participantStatusMap = remember(trackings, allParticipantNames) {
        allParticipantNames.associateWith { name ->
            val pTrackings = trackings.filter { it.participantName.equals(name, ignoreCase = true) }
            val pending = pTrackings.filter { !it.isPaid }.sumOf { it.amountOwed }
            val paid = pTrackings.filter { it.isPaid }.sumOf { it.amountOwed }
            Pair(pending, paid)
        }
    }

    val filteredSubscriptions = remember(activeSubscriptions, selectedCategoryFilter, selectedParticipantFilter, trackingsBySubId) {
        activeSubscriptions.filter { sub ->
            val matchesCategory = selectedCategoryFilter == null || sub.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesParticipant = if (selectedParticipantFilter == null) {
                true
            } else {
                val subTracks = trackingsBySubId[sub.id] ?: emptyList()
                if (subTracks.isNotEmpty()) {
                    subTracks.any { it.participantName.equals(selectedParticipantFilter, ignoreCase = true) }
                } else {
                    sub.participantsSummary.contains(selectedParticipantFilter!!, ignoreCase = true)
                }
            }
            matchesCategory && matchesParticipant
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("subscriptions_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado Principal
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Suscripciones",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Control recurrente y participantes compartidos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_new_subscription")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nueva", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de mes / periodo para el tracking.
                // Corrección: antes se mostraban 48 chips (4 años x 12 meses) siempre visibles en
                // pantalla, ocupando mucho espacio horizontal en celular. Ahora es un selector
                // compacto "‹ Mes Año ›" con navegación de un mes a la vez, más un botón de
                // calendario para saltar directamente a cualquier mes (sin perder esa función).
                val currentYmIndex = remember(availableYearMonths, selectedYearMonth) {
                    availableYearMonths.indexOfFirst { it.first == selectedYearMonth }
                }
                val currentYmLabel = if (currentYmIndex >= 0) availableYearMonths[currentYmIndex].second else selectedYearMonth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentYmIndex > 0) onSelectYearMonth(availableYearMonths[currentYmIndex - 1].first)
                        },
                        enabled = currentYmIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showMonthPickerDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(currentYmLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    IconButton(
                        onClick = {
                            if (currentYmIndex in 0 until availableYearMonths.lastIndex) onSelectYearMonth(availableYearMonths[currentYmIndex + 1].first)
                        },
                        enabled = currentYmIndex in 0 until availableYearMonths.lastIndex
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                    }
                }
            }
        }

        // Resumen Financiero de Suscripciones del Mes
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscriptions_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL MENSUAL FIJO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(totalMonthlyCost),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Subscriptions, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeSubscriptions.size} activas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicadores de recaudación manual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recaudado / Pagado", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(totalPaid),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(if (isDark) Color(0xFFFFB74D) else Color(0xFFD97706), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Por cobrar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(totalPending),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPending > 0) {
                                    if (isDark) Color(0xFFFFB74D) else Color(0xFFD97706)
                                } else {
                                    if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                }
                            )
                        }
                    }

                    if (totalMonthlyCost > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = if (totalPaid + totalPending > 0) (totalPaid / (totalPaid + totalPending)).toFloat().coerceIn(0f, 1f) else 1f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }

                    // Tiras de personas pendientes de pago
                    if (pendingByParticipant.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0xFF26190B) else Color(0xFFFFF3E0),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF7A4B10) else Color(0xFFFFB74D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = "👤 FALTAN POR PAGAR ESTE MES:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFFB74D) else Color(0xFF9A3412)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pendingByParticipant.forEach { (pName, owed) ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isDark) Color(0xFF3D260F) else Color(0xFFFFE0B2),
                                            modifier = Modifier.clickable { selectedParticipantFilter = pName }
                                        ) {
                                            Text(
                                                text = "$pName: ${currencyFormat.format(owed)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color(0xFFFFE0B2) else Color(0xFF5D2400),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Categorías filter
        item {
            val categories = listOf("Streaming", "Música", "Almacenamiento", "Software", "Servicios")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("Todas (${activeSubscriptions.size})", fontSize = 11.sp) }
                )
                categories.forEach { cat ->
                    val count = activeSubscriptions.count { it.category.equals(cat, ignoreCase = true) }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedCategoryFilter.equals(cat, ignoreCase = true),
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter.equals(cat, ignoreCase = true)) null else cat },
                            label = { Text("$cat ($count)", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Requisito 9: Filtrar por participante para saber quién debe cuánto a la fecha actual
        if (allParticipantNames.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Corrección: se quitó el botón "Ver todos" de aquí — era redundante con el chip
                    // "Todos (N)" que aparece justo debajo y hace exactamente lo mismo.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filtrar por Participante (Deudas a la fecha):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedParticipantFilter == null,
                            onClick = { selectedParticipantFilter = null },
                            label = { Text("Todos (${allParticipantNames.size})", fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_participant_all")
                        )
                        allParticipantNames.forEach { pName ->
                            val (pending, _) = participantStatusMap[pName] ?: Pair(0.0, 0.0)
                            val isSelected = selectedParticipantFilter.equals(pName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedParticipantFilter = if (isSelected) null else pName
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (pending > 0) Icons.Default.Person else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (pending > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (pending > 0) "$pName (Debe ${currencyFormat.format(pending)})"
                                               else "$pName (Al día ✓)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("filter_participant_$pName")
                            )
                        }
                    }
                }
            }
        }

        // Banner informativo del participante filtrado (Requisito 9)
        if (selectedParticipantFilter != null) {
            item {
                val pFilterName = selectedParticipantFilter!!
                val (debt, paid) = participantStatusMap[pFilterName] ?: Pair(0.0, 0.0)
                val bannerBg = if (isDark) {
                    if (debt > 0) Color(0xFF2E1906) else Color(0xFF0D2814)
                } else {
                    if (debt > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                }
                val bannerBorder = if (isDark) {
                    if (debt > 0) Color(0xFFFFB74D).copy(alpha = 0.5f) else Color(0xFF81C784).copy(alpha = 0.5f)
                } else {
                    if (debt > 0) Color(0xFFFFB74D) else Color(0xFFA5D6A7)
                }
                val bannerAccent = if (isDark) {
                    if (debt > 0) Color(0xFFFFB74D) else Color(0xFF81C784)
                } else {
                    if (debt > 0) Color(0xFFBF360C) else Color(0xFF2E7D32)
                }
                val bannerText = if (isDark) {
                    if (debt > 0) Color(0xFFFFE0B2) else Color(0xFFA5D6A7)
                } else {
                    if (debt > 0) Color(0xFFD84315) else Color(0xFF1B5E20)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = bannerBg,
                    border = BorderStroke(1.dp, bannerBorder),
                    modifier = Modifier.fillMaxWidth().testTag("participant_detail_filter_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = bannerAccent,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ESTADO DE CUENTA: ${pFilterName.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = bannerAccent
                                )
                                Text(
                                    text = if (debt > 0) "Debe a la fecha: ${currencyFormat.format(debt)} en $selectedYearMonth"
                                           else "Al corriente en $selectedYearMonth (Total pagado: ${currencyFormat.format(paid)})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = bannerText
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedParticipantFilter = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar filtro", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Lista de Suscripciones
        if (filteredSubscriptions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Subscriptions, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay suscripciones registradas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lleva el control recurrente de streaming, servicios y participantes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar Primera Suscripción")
                    }
                }
            }
        } else {
            items(filteredSubscriptions, key = { it.id }) { sub ->
                val card = cardMap[sub.cardId]
                val subTrackings = trackingsBySubId[sub.id] ?: emptyList()
                val subAllPaid = subTrackings.isNotEmpty() && subTrackings.all { it.isPaid }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .testTag("subscription_card_${sub.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Fila Superior: Nombre del servicio, Categoría y Costo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Subscriptions,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = sub.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = sub.category,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormat.format(sub.totalMonthlyAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val periodLabel = when (sub.periodicity.uppercase()) {
                                    "BIMESTRAL" -> "cada 2 meses"
                                    "TRIMESTRAL" -> "cada 3 meses"
                                    "SEMESTRAL" -> "cada 6 meses"
                                    "ANUAL" -> "cada año"
                                    else -> "al mes"
                                }
                                Text(
                                    text = periodLabel,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (sub.periodicity.uppercase() != "MENSUAL") {
                                    Text(
                                        text = "~${currencyFormat.format(sub.monthlyEquivalentAmount)}/mes",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fila de Detalles: Tarjeta Asignada y Día de Cargo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chip de tarjeta
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(card?.primaryColorHex ?: 0xFF003B70L), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${card?.name ?: "Tarjeta"} ••${card?.last4Digits ?: "0000"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Chip de fecha de cargo
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cargo día ${sub.billingDayOfMonth} c/mes",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            // Botón Editar
                            IconButton(
                                onClick = { editingSubscription = sub },
                                modifier = Modifier.size(32.dp).testTag("btn_edit_sub_${sub.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }

                            // Requisito 4: Botón Eliminar suscripción con confirmación
                            IconButton(
                                onClick = { subscriptionToDelete = sub },
                                modifier = Modifier.size(32.dp).testTag("btn_delete_sub_${sub.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar suscripción",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Requisito 5: Control manual para saber quién ya pagó y quién falta por pagar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PARTICIPANTES ($selectedYearMonth)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (subTrackings.isNotEmpty() && !subAllPaid) {
                                TextButton(
                                    onClick = { onMarkAllPaid(sub.id) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp).testTag("mark_all_paid_${sub.id}")
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Marcar todos pagados", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (subTrackings.isEmpty()) {
                            Text(
                                text = sub.participantsSummary.ifBlank { "Sin participantes configurados" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                subTrackings.forEach { tracking ->
                                    val isTargetParticipant = selectedParticipantFilter != null && tracking.participantName.equals(selectedParticipantFilter, ignoreCase = true)
                                    val pillBg = if (isDark) {
                                        if (tracking.isPaid) {
                                            if (isTargetParticipant) Color(0xFF1B3820) else Color(0xFF112615)
                                        } else {
                                            if (isTargetParticipant) Color(0xFF3E1F08) else Color(0xFF2A1504)
                                        }
                                    } else {
                                        if (isTargetParticipant) {
                                            if (tracking.isPaid) Color(0xFFDCEDC8) else Color(0xFFFFE0B2)
                                        } else {
                                            if (tracking.isPaid) Color(0xFFF1F8E9) else Color(0xFFFFF8E1)
                                        }
                                    }

                                    val pillBorder = if (isTargetParticipant) {
                                        BorderStroke(2.dp, if (isDark) {
                                            if (tracking.isPaid) Color(0xFF81C784) else Color(0xFFFFB74D)
                                        } else {
                                            if (tracking.isPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                                        })
                                    } else if (isDark) {
                                        BorderStroke(0.5.dp, if (tracking.isPaid) Color(0xFF2E7D32).copy(alpha = 0.4f) else Color(0xFFE65100).copy(alpha = 0.4f))
                                    } else null

                                    val iconTint = if (isDark) {
                                        if (tracking.isPaid) Color(0xFF81C784) else Color(0xFFFFB74D)
                                    } else {
                                        if (tracking.isPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    }

                                    val nameColor = if (isDark) {
                                        if (tracking.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFE0B2)
                                    } else {
                                        if (tracking.isPaid) Color(0xFF1B5E20) else Color(0xFF3E1C00)
                                    }

                                    val amountColor = if (isDark) {
                                        if (tracking.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFCC80)
                                    } else {
                                        if (tracking.isPaid) Color(0xFF1B5E20) else Color(0xFF7A3600)
                                    }

                                    val badgeContainerColor = if (isDark) {
                                        if (tracking.isPaid) Color(0xFF1E4624) else Color(0xFF5A2A06)
                                    } else {
                                        if (tracking.isPaid) Color(0xFFC8E6C9) else Color(0xFFFFE0B2)
                                    }

                                    val badgeTextColor = if (isDark) {
                                        if (tracking.isPaid) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                                    } else {
                                        if (tracking.isPaid) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = pillBg,
                                        border = pillBorder,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleTrackingPaid(tracking) }
                                            .testTag("tracking_pill_${tracking.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (tracking.isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = iconTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = tracking.participantName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = nameColor
                                                        )
                                                        if (isTargetParticipant) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = if (tracking.isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                            ) {
                                                                Text(
                                                                    text = "FILTRADO",
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (tracking.isPaid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (tracking.isPaid && tracking.paidDateMillis != null) {
                                                        Text(
                                                            text = "Pagado",
                                                            fontSize = 10.sp,
                                                            color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = currencyFormat.format(tracking.amountOwed),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = amountColor
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = badgeContainerColor
                                                ) {
                                                    Text(
                                                        text = if (tracking.isPaid) "PAGADO" else "PENDIENTE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = badgeTextColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para saltar directamente a cualquier mes (reemplaza los 48 chips que antes estaban
    // siempre visibles en pantalla; ahora solo aparecen cuando el usuario los pide).
    if (showMonthPickerDialog) {
        AlertDialog(
            onDismissRequest = { showMonthPickerDialog = false },
            title = { Text("Ir a un mes", fontWeight = FontWeight.Bold) },
            text = {
                val groupedByYear = remember(availableYearMonths) {
                    availableYearMonths.groupBy { it.first.substring(0, 4) }
                }
                LazyColumn(modifier = Modifier.height(360.dp)) {
                    groupedByYear.forEach { (year, months) ->
                        item {
                            Text(
                                text = year,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                months.forEach { (ym, label) ->
                                    val isSelected = ym == selectedYearMonth
                                    val monthOnly = label.substringBefore(" ")
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onSelectYearMonth(ym)
                                            showMonthPickerDialog = false
                                        },
                                        label = { Text(monthOnly, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthPickerDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Diálogo Registrar Nueva Suscripción
    if (showAddDialog) {
        AddSubscriptionDialog(
            cards = cards,
            defaultProfileName = userProfileName,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cardId, billingDay, totalAmount, category, participants, periodicity ->
                onAddSubscription(name, cardId, billingDay, totalAmount, category, participants, periodicity)
                showAddDialog = false
            }
        )
    }

    // Diálogo Editar Suscripción
    if (editingSubscription != null) {
        val subToEdit = editingSubscription!!
        val currentTrackings = trackingsBySubId[subToEdit.id] ?: emptyList()
        EditSubscriptionDialog(
            subscription = subToEdit,
            currentTrackings = currentTrackings,
            cards = cards,
            selectedYearMonth = selectedYearMonth,
            defaultProfileName = userProfileName,
            onDismiss = { editingSubscription = null },
            onConfirm = { newName, newCardId, newBillingDay, newTotalAmount, newCategory, participants, affectCurrentMonth, periodicity ->
                onUpdateSubscription(subToEdit, newName, newCardId, newBillingDay, newTotalAmount, newCategory, participants, affectCurrentMonth, periodicity)
                editingSubscription = null
            }
        )
    }

    // Requisito 4 & 5: Diálogo Confirmación para Eliminar Suscripción
    if (subscriptionToDelete != null) {
        val sub = subscriptionToDelete!!
        AlertDialog(
            onDismissRequest = { subscriptionToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("¿Eliminar suscripción?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Estás por dar de baja la suscripción \"${sub.name}\" (${currencyFormat.format(sub.totalMonthlyAmount)}/mes)."
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Los registros de meses anteriores permanecerán intactos en tu historial.\n\n¿Deseas conservar el cargo del mes en curso ($selectedYearMonth) o eliminarlo también?",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val target = subscriptionToDelete
                            subscriptionToDelete = null
                            if (target != null) onDeleteSubscription?.invoke(target, true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Conservar mes actual y eliminar futuros", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val target = subscriptionToDelete
                            subscriptionToDelete = null
                            if (target != null) onDeleteSubscription?.invoke(target, false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar mes actual y futuros", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { subscriptionToDelete = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Estado reactivo para cada participante de la suscripción.
 * Al usar MutableState en name y amountText, Compose detecta cada cambio de texto en tiempo real
 * evitando que el teclado se bloquee o el cursor salte.
 */
class SubscriptionParticipantState(
    val id: Long,
    initialName: String,
    initialAmount: String,
    val isPersonal: Boolean = false
) {
    var name by mutableStateOf(initialName)
    var amountText by mutableStateOf(initialAmount)
}

/**
 * Diálogo para registrar una nueva suscripción
 * - Sin sugerencias populares (Requisito 5)
 * - Botón dividir equitativo 100% funcional (Requisito 6)
 * - Nombres y montos editables con teclado y cursor funcionales (Requisito 7)
 * - Modificación manual de miembros y el resultante lo asume el perfil personal (Requisito 8)
 */
@Composable
private fun AddSubscriptionDialog(
    cards: List<CreditCard>,
    defaultProfileName: String = "Usuario",
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        cardId: Long,
        billingDayOfMonth: Int,
        totalMonthlyAmount: Double,
        category: String,
        participants: List<Pair<String, Double>>,
        periodicity: String
    ) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Streaming") }
    var selectedCardId by remember { mutableStateOf(cards.firstOrNull()?.id ?: 1L) }
    var billingDayText by remember { mutableStateOf("15") }
    // Corrección: antes venía precargado con "299" (un monto real, similar al de Netflix), lo que
    // permitía registrar sin querer un cargo real si el usuario tocaba "Registrar" sin revisar.
    var totalAmountText by remember { mutableStateOf("") }
    var periodicity by remember { mutableStateOf("MENSUAL") }

    val participantsList = remember {
        mutableStateListOf(
            SubscriptionParticipantState(1L, "$defaultProfileName (Tú)", "", isPersonal = true)
        )
    }

    // Requisito 6: Dividir equitativo
    fun splitEqually() {
        val total = totalAmountText.toDoubleOrNull() ?: 0.0
        val count = participantsList.size
        if (count > 0 && total > 0) {
            val base = Math.floor((total / count) * 100.0) / 100.0
            val sumOthers = base * (count - 1)
            val personalShare = total - sumOthers
            participantsList.forEach { item ->
                if (item.isPersonal) {
                    item.amountText = if (personalShare % 1.0 == 0.0) personalShare.toInt().toString() else String.format(Locale.US, "%.2f", personalShare)
                } else {
                    item.amountText = if (base % 1.0 == 0.0) base.toInt().toString() else String.format(Locale.US, "%.2f", base)
                }
            }
        }
    }

    // Requisito 8: Al modificar manualmente la aportación de un miembro, el resultante lo asume el perfil personal
    fun onMemberAmountChange(participant: SubscriptionParticipantState, newText: String) {
        val clean = newText.filter { it.isDigit() || it == '.' }
        participant.amountText = clean
        if (!participant.isPersonal) {
            val total = totalAmountText.toDoubleOrNull() ?: 0.0
            val sumOthers = participantsList.filter { !it.isPersonal }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val remainder = (total - sumOthers).coerceAtLeast(0.0)
            val personalItem = participantsList.firstOrNull { it.isPersonal }
            if (personalItem != null) {
                personalItem.amountText = if (remainder % 1.0 == 0.0) remainder.toInt().toString() else String.format(Locale.US, "%.2f", remainder)
            }
        }
    }

    fun onTotalAmountChange(newTotalText: String) {
        totalAmountText = newTotalText.filter { it.isDigit() || it == '.' }
        val total = totalAmountText.toDoubleOrNull() ?: 0.0
        val sumOthers = participantsList.filter { !it.isPersonal }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
        val remainder = (total - sumOthers).coerceAtLeast(0.0)
        val personalItem = participantsList.firstOrNull { it.isPersonal }
        if (personalItem != null) {
            personalItem.amountText = if (remainder % 1.0 == 0.0) remainder.toInt().toString() else String.format(Locale.US, "%.2f", remainder)
        }
    }

    fun removeParticipant(participant: SubscriptionParticipantState) {
        if (!participant.isPersonal && participantsList.size > 1) {
            participantsList.remove(participant)
            val total = totalAmountText.toDoubleOrNull() ?: 0.0
            val sumOthers = participantsList.filter { !it.isPersonal }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val remainder = (total - sumOthers).coerceAtLeast(0.0)
            val personalItem = participantsList.firstOrNull { it.isPersonal }
            if (personalItem != null) {
                personalItem.amountText = if (remainder % 1.0 == 0.0) remainder.toInt().toString() else String.format(Locale.US, "%.2f", remainder)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Subscriptions, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Suscripción", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Servicio") },
                    placeholder = { Text("Ej. Netflix Familiar") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sub_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { onTotalAmountChange(it) },
                        label = { Text("Costo Mensual ($)") },
                        placeholder = { Text("Ej. 299") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f).testTag("sub_cost_input")
                    )

                    OutlinedTextField(
                        value = billingDayText,
                        onValueChange = { billingDayText = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Día Cargo (1-31)") },
                        placeholder = { Text("15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("sub_billing_day_input")
                    )
                }

                // Periodicidad
                Text("Periodicidad de Cobro:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val pOptions = listOf(
                        "MENSUAL" to "Mensual",
                        "BIMESTRAL" to "Bimestral",
                        "TRIMESTRAL" to "Trimestral",
                        "SEMESTRAL" to "Semestral",
                        "ANUAL" to "Anual"
                    )
                    pOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = periodicity == key,
                            onClick = { periodicity = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Selector de Tarjeta
                Text("Cargar a Tarjeta:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        .background(Color(card.primaryColorHex), CircleShape)
                                )
                            },
                            label = { Text("${card.name} (••${card.last4Digits})", fontSize = 11.sp) }
                        )
                    }
                }

                // Sección Participantes y división
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Participantes y Cuotas:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Modifica aportaciones y tu perfil asume el restante",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = { splitEqually() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dividir equitativo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    participantsList.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (p.isPersonal) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (p.isPersonal) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (p.isPersonal) "👤 PERFIL PERSONAL (ASUME RESTANTE)" else "👥 PARTICIPANTE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (p.isPersonal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!p.isPersonal && participantsList.size > 1) {
                                        IconButton(
                                            onClick = { removeParticipant(p) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = p.name,
                                        onValueChange = { p.name = it },
                                        placeholder = { Text("Nombre") },
                                        label = { Text("Nombre", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                        singleLine = true,
                                        modifier = Modifier.weight(1.4f)
                                    )
                                    // Corrección: el monto de "Personal" se recalculaba y sobrescribía sin
                                    // aviso cada vez que se editaba cualquier otro participante o el total,
                                    // descartando lo que el usuario hubiera escrito ahí. Como por diseño
                                    // (Requisito 8) Personal siempre debe absorber el remanente, ahora ese
                                    // campo se muestra de solo lectura en vez de editable-pero-inestable.
                                    OutlinedTextField(
                                        value = p.amountText,
                                        onValueChange = { onMemberAmountChange(p, it) },
                                        placeholder = { Text("0") },
                                        label = { Text(if (p.isPersonal) "Cuota ($) · automático" else "Cuota ($)", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        readOnly = p.isPersonal,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Cuadre de cuotas
                    val totalCost = totalAmountText.toDoubleOrNull() ?: 0.0
                    val currentSum = participantsList.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                    val diff = totalCost - currentSum
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) {
                            if (Math.abs(diff) < 0.01) Color(0xFF0F2B14) else Color(0xFF2E1906)
                        } else {
                            if (Math.abs(diff) < 0.01) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        },
                        border = BorderStroke(1.dp, if (isDark) {
                            if (Math.abs(diff) < 0.01) Color(0xFF2E7D32).copy(alpha = 0.5f) else Color(0xFFFFB74D).copy(alpha = 0.4f)
                        } else {
                            if (Math.abs(diff) < 0.01) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                        }),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total: \$${totalAmountText.ifBlank { "0" }} | Suma: \$${String.format(Locale.US, "%.2f", currentSum)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) {
                                    if (Math.abs(diff) < 0.01) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                                } else {
                                    if (Math.abs(diff) < 0.01) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                }
                            )
                            Text(
                                text = if (Math.abs(diff) < 0.01) "Balanceado ✓" else "Diferencia: \$${String.format(Locale.US, "%.2f", diff)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) {
                                    if (Math.abs(diff) < 0.01) Color(0xFF81C784) else Color(0xFFFFB74D)
                                } else {
                                    if (Math.abs(diff) < 0.01) Color(0xFF2E7D32) else Color(0xFFE65100)
                                }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            participantsList.add(
                                SubscriptionParticipantState(
                                    id = System.currentTimeMillis(),
                                    initialName = "Participante ${participantsList.size + 1}",
                                    initialAmount = "0",
                                    isPersonal = false
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar Otro Participante", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            val totalNum = totalAmountText.toDoubleOrNull() ?: 0.0
            val dayNum = billingDayText.toIntOrNull() ?: 15
            // Corrección: la división de un gasto (QuickAddExpenseSheet) bloquea guardar si no cuadra,
            // pero aquí solo era una advertencia visual; ahora también es obligatorio que cuadre.
            val currentSum = participantsList.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val isBalanced = Math.abs(totalNum - currentSum) < 0.01
            val canSubmit = name.isNotBlank() && totalNum > 0 && isBalanced

            Button(
                onClick = {
                    if (canSubmit) {
                        val finalParticipants = participantsList.map {
                            it.name.trim().ifBlank { "Participante" } to (it.amountText.toDoubleOrNull() ?: 0.0)
                        }
                        onConfirm(name, selectedCardId, dayNum, totalNum, selectedCategory, finalParticipants, periodicity)
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.testTag("btn_confirm_add_subscription")
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para editar una suscripción existente
 * Regla de negocio del usuario:
 * "No podrán eliminarse esos cargos y solo podrán modificarse en caso de que la suscripción cambie de precio o de tarjeta,
 * y se afectarán los movimientos del mes en curso o posterior, no los anteriores."
 */
@Composable
private fun EditSubscriptionDialog(
    subscription: Subscription,
    currentTrackings: List<SubscriptionPaymentTracking>,
    cards: List<CreditCard>,
    selectedYearMonth: String,
    defaultProfileName: String = "Usuario",
    onDismiss: () -> Unit,
    onConfirm: (
        newName: String,
        newCardId: Long,
        newBillingDay: Int,
        newTotalAmount: Double,
        newCategory: String,
        participants: List<Pair<String, Double>>,
        affectCurrentMonth: Boolean,
        periodicity: String
    ) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var editName by remember { mutableStateOf(subscription.name) }
    var editCategory by remember { mutableStateOf(subscription.category) }
    var editCardId by remember { mutableStateOf(subscription.cardId) }
    var editBillingDay by remember { mutableStateOf(subscription.billingDayOfMonth.toString()) }
    var editPeriodicity by remember { mutableStateOf(subscription.periodicity) }
    var affectCurrentMonth by remember { mutableStateOf(true) }
    var editTotalAmount by remember {
        mutableStateOf(
            if (subscription.totalMonthlyAmount % 1.0 == 0.0)
                subscription.totalMonthlyAmount.toInt().toString()
            else
                String.format(Locale.US, "%.2f", subscription.totalMonthlyAmount)
        )
    }

    val editParticipants = remember(subscription, currentTrackings) {
        val list = mutableStateListOf<SubscriptionParticipantState>()
        if (currentTrackings.isNotEmpty()) {
            currentTrackings.forEachIndexed { index, track ->
                val isPersonal = index == 0 || track.participantName.contains(defaultProfileName, ignoreCase = true) || track.participantName.contains("Tú", ignoreCase = true) || track.participantName.contains("Personal", ignoreCase = true)
                list.add(
                    SubscriptionParticipantState(
                        id = track.id,
                        initialName = track.participantName,
                        initialAmount = if (track.amountOwed % 1.0 == 0.0) track.amountOwed.toInt().toString() else String.format(Locale.US, "%.2f", track.amountOwed),
                        isPersonal = isPersonal
                    )
                )
            }
            if (list.none { it.isPersonal } && list.isNotEmpty()) {
                val first = list[0]
                list[0] = SubscriptionParticipantState(first.id, first.name, first.amountText, isPersonal = true)
            }
        } else {
            list.add(
                SubscriptionParticipantState(
                    id = 1L,
                    initialName = "$defaultProfileName (Tú)",
                    initialAmount = if (subscription.totalMonthlyAmount % 1.0 == 0.0) subscription.totalMonthlyAmount.toInt().toString() else String.format(Locale.US, "%.2f", subscription.totalMonthlyAmount),
                    isPersonal = true
                )
            )
        }
        list
    }

    fun splitEqually() {
        val total = editTotalAmount.toDoubleOrNull() ?: 0.0
        val count = editParticipants.size
        if (count > 0 && total > 0) {
            val base = Math.floor((total / count) * 100.0) / 100.0
            val sumOthers = base * (count - 1)
            val personalShare = total - sumOthers
            editParticipants.forEach { item ->
                if (item.isPersonal) {
                    item.amountText = if (personalShare % 1.0 == 0.0) personalShare.toInt().toString() else String.format(Locale.US, "%.2f", personalShare)
                } else {
                    item.amountText = if (base % 1.0 == 0.0) base.toInt().toString() else String.format(Locale.US, "%.2f", base)
                }
            }
        }
    }

    fun onMemberAmountChange(participant: SubscriptionParticipantState, newText: String) {
        val clean = newText.filter { it.isDigit() || it == '.' }
        participant.amountText = clean
        if (!participant.isPersonal) {
            val total = editTotalAmount.toDoubleOrNull() ?: 0.0
            val sumOthers = editParticipants.filter { !it.isPersonal }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val remainder = (total - sumOthers).coerceAtLeast(0.0)
            val personalItem = editParticipants.firstOrNull { it.isPersonal }
            if (personalItem != null) {
                personalItem.amountText = if (remainder % 1.0 == 0.0) remainder.toInt().toString() else String.format(Locale.US, "%.2f", remainder)
            }
        }
    }

    fun onTotalAmountChange(newTotalText: String) {
        editTotalAmount = newTotalText.filter { it.isDigit() || it == '.' }
        val total = editTotalAmount.toDoubleOrNull() ?: 0.0
        val sumOthers = editParticipants.filter { !it.isPersonal }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
        val remainder = (total - sumOthers).coerceAtLeast(0.0)
        val personalItem = editParticipants.firstOrNull { it.isPersonal }
        if (personalItem != null) {
            personalItem.amountText = if (remainder % 1.0 == 0.0) remainder.toInt().toString() else String.format(Locale.US, "%.2f", remainder)
        }
    }

    fun removeParticipant(participant: SubscriptionParticipantState) {
        if (!participant.isPersonal && editParticipants.size > 1) {
            editParticipants.remove(participant)
            val total = editTotalAmount.toDoubleOrNull() ?: 0.0
            val sumOthers = editParticipants.filter { !it.isPersonal }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val remainder = (total - sumOthers).coerceAtLeast(0.0)
            val personalItem = editParticipants.firstOrNull { it.isPersonal }
            if (personalItem != null) {
                personalItem.amountText = if (remainder % 1.0 == 0.0) remainder.toInt().toString() else String.format(Locale.US, "%.2f", remainder)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modificar Suscripción", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Aviso de protección contable
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Los cambios de precio o tarjeta afectarán los movimientos del mes en curso o posteriores. Los meses anteriores se mantendrán intactos para proteger la contabilidad.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Opción para aplicar cambios al mes en curso o solo posteriores
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¿Aplicar al mes actual ($selectedYearMonth)?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (affectCurrentMonth) "Afectará el mes actual y meses futuros" else "Comenzará a partir del próximo mes",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = affectCurrentMonth,
                            onCheckedChange = { affectCurrentMonth = it }
                        )
                    }
                }

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nombre") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editTotalAmount,
                        onValueChange = { onTotalAmountChange(it) },
                        label = { Text("Costo Mensual ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = editBillingDay,
                        onValueChange = { editBillingDay = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Día Cargo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Selector de Periodicidad
                Text("Periodicidad de Cobro:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val pOptions = listOf(
                        "MENSUAL" to "Mensual",
                        "BIMESTRAL" to "Bimestral",
                        "TRIMESTRAL" to "Trimestral",
                        "SEMESTRAL" to "Semestral",
                        "ANUAL" to "Anual"
                    )
                    pOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = editPeriodicity == key,
                            onClick = { editPeriodicity = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Selector de Tarjeta
                Text("Tarjeta para el cargo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cards.forEach { card ->
                        val isSelected = card.id == editCardId
                        FilterChip(
                            selected = isSelected,
                            onClick = { editCardId = card.id },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(card.primaryColorHex), CircleShape)
                                )
                            },
                            label = { Text("${card.name} (••${card.last4Digits})", fontSize = 11.sp) }
                        )
                    }
                }

                // Participantes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Participantes y Cuotas:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Modifica aportaciones y tu perfil asume el restante",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = { splitEqually() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dividir equitativo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    editParticipants.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (p.isPersonal) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (p.isPersonal) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (p.isPersonal) "👤 PERFIL PERSONAL (ASUME RESTANTE)" else "👥 PARTICIPANTE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (p.isPersonal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!p.isPersonal && editParticipants.size > 1) {
                                        IconButton(
                                            onClick = { removeParticipant(p) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = p.name,
                                        onValueChange = { p.name = it },
                                        placeholder = { Text("Nombre") },
                                        label = { Text("Nombre", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                        singleLine = true,
                                        modifier = Modifier.weight(1.4f)
                                    )
                                    OutlinedTextField(
                                        value = p.amountText,
                                        onValueChange = { onMemberAmountChange(p, it) },
                                        placeholder = { Text("0") },
                                        label = { Text(if (p.isPersonal) "Cuota ($) · automático" else "Cuota ($)", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        readOnly = p.isPersonal,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Balance de cuotas
                    val totalCost = editTotalAmount.toDoubleOrNull() ?: 0.0
                    val currentSum = editParticipants.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                    val diff = totalCost - currentSum
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) {
                            if (Math.abs(diff) < 0.01) Color(0xFF0F2B14) else Color(0xFF2E1906)
                        } else {
                            if (Math.abs(diff) < 0.01) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        },
                        border = BorderStroke(1.dp, if (isDark) {
                            if (Math.abs(diff) < 0.01) Color(0xFF2E7D32).copy(alpha = 0.5f) else Color(0xFFFFB74D).copy(alpha = 0.4f)
                        } else {
                            if (Math.abs(diff) < 0.01) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                        }),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total: \$${editTotalAmount.ifBlank { "0" }} | Suma: \$${String.format(Locale.US, "%.2f", currentSum)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) {
                                    if (Math.abs(diff) < 0.01) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                                } else {
                                    if (Math.abs(diff) < 0.01) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                }
                            )
                            Text(
                                text = if (Math.abs(diff) < 0.01) "Balanceado ✓" else "Diferencia: \$${String.format(Locale.US, "%.2f", diff)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) {
                                    if (Math.abs(diff) < 0.01) Color(0xFF81C784) else Color(0xFFFFB74D)
                                } else {
                                    if (Math.abs(diff) < 0.01) Color(0xFF2E7D32) else Color(0xFFE65100)
                                }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            editParticipants.add(
                                SubscriptionParticipantState(
                                    id = System.currentTimeMillis(),
                                    initialName = "Participante ${editParticipants.size + 1}",
                                    initialAmount = "0",
                                    isPersonal = false
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar Participante", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            val totalNum = editTotalAmount.toDoubleOrNull() ?: 0.0
            val dayNum = editBillingDay.toIntOrNull() ?: 15
            val currentSum = editParticipants.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val isBalanced = Math.abs(totalNum - currentSum) < 0.01
            val canSubmit = editName.isNotBlank() && totalNum > 0 && isBalanced

            Button(
                onClick = {
                    if (canSubmit) {
                        val finalParticipants = editParticipants.map {
                            it.name.trim().ifBlank { "Participante" } to (it.amountText.toDoubleOrNull() ?: 0.0)
                        }
                        onConfirm(editName, editCardId, dayNum, totalNum, editCategory, finalParticipants, affectCurrentMonth, editPeriodicity)
                    }
                },
                enabled = canSubmit
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
