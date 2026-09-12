package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.domain.CreditCardCalculator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddExpenseSheet(
    cards: List<CreditCard>,
    defaultCardId: Long? = null,
    currentMonth: String = "Agosto",
    lockToCurrentMonth: Boolean = false,
    availableMonths: List<String> = listOf("Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre", "Enero", "Febrero"),
    onDismiss: () -> Unit,
    onSave: (
        cardId: Long,
        concept: String,
        amount: Double,
        dateMillis: Long,
        beneficiary: String,
        category: String,
        isMsi: Boolean,
        msiTotalMonths: Int,
        msiCurrentInstallment: Int,
        statementMonth: String,
        msiTotalPurchaseAmount: Double
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            // Requisito 3: No permitir que al desplazar hacia abajo se descarte el registro de gasto
            sheetValue != SheetValue.Hidden
        }
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isKeyboardVisible = WindowInsets.isImeVisible

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val displayDateFormat = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale("es", "MX"))

    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCardId by remember(defaultCardId) {
        mutableStateOf(defaultCardId ?: cards.firstOrNull()?.id ?: 0L)
    }

    // 1. FECHA DEL GASTO (REQUERIDA)
    var expenseDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Diálogo de advertencia de descarte / navegación
    var showDiscardWarningDialog by remember { mutableStateOf(false) }
    val hasUnsavedChanges = concept.isNotBlank() || amountText.isNotBlank()
    val handleDismissAttempt = {
        if (hasUnsavedChanges) {
            showDiscardWarningDialog = true
        } else {
            onDismiss()
        }
    }

    // Requisito 1: Al dar en el botón atrás para ocultar el teclado, solo ocultar el teclado sin descartar la edición
    BackHandler(enabled = true) {
        if (isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            handleDismissAttempt()
        }
    }

    // Periodo / Estado de cuenta: Determinado automáticamente con opción a editar (o ligado a la cuenta/periodo revisado)
    var userOverriddenMonth by remember(lockToCurrentMonth, currentMonth) {
        mutableStateOf<String?>(if (lockToCurrentMonth) CreditCardCalculator.normalizeMonth(currentMonth) else null)
    }

    // Beneficiario individual cuando NO se divide
    var selectedBeneficiary by remember { mutableStateOf("Personal") }
    var selectedCategory by remember { mutableStateOf("Despensa") }

    // 2. MESES SIN INTERESES (MSI)
    var isMsi by remember { mutableStateOf(false) }
    var msiTotalMonths by remember { mutableIntStateOf(12) }
    var isExistingMsiWithAdvance by remember { mutableStateOf(false) }
    var msiCurrentInstallmentText by remember { mutableStateOf("1") }
    var autoEstimateOriginalDate by remember { mutableStateOf(true) }

    val effectiveCurrentInstallment = remember(isExistingMsiWithAdvance, msiCurrentInstallmentText, msiTotalMonths) {
        if (!isExistingMsiWithAdvance) {
            1
        } else {
            (msiCurrentInstallmentText.toIntOrNull() ?: 1).coerceIn(1, msiTotalMonths)
        }
    }

    // 1. REQUISITO USUARIO: CONCEPTOS SUGERIDOS EDITABLES (agregar, editar, eliminar, reordenar)
    var quickConcepts by remember {
        mutableStateOf(listOf("Gasolina", "Despensa Walmart", "Amazon", "TotalPlay", "CFE", "Mercado Pago", "Aurrerá", "Restaurante"))
    }
    var showManageConceptsDialog by remember { mutableStateOf(false) }

    // 3. REQUISITO USUARIO: LISTA DE PERSONAS EDITABLE (agregar, editar, eliminar, reordenar)
    var allPeople by remember {
        mutableStateOf(listOf("Personal", "Familiar", "Pareja", "Hijos", "Trabajo", "Amigo"))
    }
    var showManagePeopleDialog by remember { mutableStateOf(false) }

    // 2. REQUISITO USUARIO: DIVISOR DE GASTOS PARA 2 O MÁS PERSONAS CON PARTICIPACIÓN PERSONAL OPCIONAL
    var isSplitExpense by remember { mutableStateOf(false) }
    var splitIncludeMe by remember { mutableStateOf(true) } // Es opcional que yo participe ("Personal")
    var selectedSplitPartners by remember { mutableStateOf(emptyList<String>()) } // Otras personas seleccionadas
    var splitMode by remember { mutableIntStateOf(0) } // 0: Equitativo / Porcentaje, 1: Monto Fijo ($)

    // Porcentajes y montos por persona: mapa de Persona -> Valor
    var customPercentages by remember { mutableStateOf(mapOf<String, Float>()) }
    var customFixedAmounts by remember { mutableStateOf(mapOf<String, String>()) }

    // Corrección del bug de "cursor que salta": mientras el usuario teclea un porcentaje, se conserva
    // el texto exacto que escribió (incluso vacío momentáneamente) en vez de mostrar siempre un valor
    // recalculado desde customPercentages, que se reescribía en cada tecla y reposicionaba el cursor.
    var customPercentageTexts by remember { mutableStateOf(mapOf<String, String>()) }

    val msiOptions = listOf(3, 6, 9, 12, 15, 18, 24)
    val selectedCard = cards.firstOrNull { it.id == selectedCardId } ?: cards.firstOrNull()

    // Determinación automática del mes en que se va a pagar según corte y fecha de pago de la tarjeta
    val autoCalculatedMonth = if (selectedCard != null) {
        CreditCardCalculator.calculatePaymentMonthName(selectedCard, expenseDateMillis)
    } else {
        CreditCardCalculator.normalizeMonth(currentMonth)
    }

    // El periodo activo muestra solo el nombre del mes
    val activePaymentMonth = userOverriddenMonth ?: autoCalculatedMonth

    // Cálculo automático de mensualidades MSI sobre el monto total capturado
    val capturedTotalAmount = CreditCardCalculator.parseLocalizedDouble(amountText) ?: 0.0
    val calculatedMonthlyInstallment = if (isMsi && msiTotalMonths > 0) {
        capturedTotalAmount / msiTotalMonths
    } else {
        capturedTotalAmount
    }

    // Cálculo automático de la línea de tiempo de MSI conforme a fecha de compra, corte de tarjeta y mes activo
    val autoMsiTimeline = remember(expenseDateMillis, selectedCard, msiTotalMonths, activePaymentMonth, isMsi) {
        if (selectedCard != null && isMsi) {
            CreditCardCalculator.calculateMsiAutoTimeline(
                purchaseDateMillis = expenseDateMillis,
                cardCutoffDay = selectedCard.cutoffDay,
                totalMonths = msiTotalMonths,
                targetStatementMonthName = activePaymentMonth
            )
        } else null
    }

    // Si la fecha seleccionada corresponde a una compra de meses anteriores, autocompletar la cuota y avance
    LaunchedEffect(autoMsiTimeline?.currentInstallment) {
        if (autoMsiTimeline != null && autoMsiTimeline.currentInstallment > 1 && !isExistingMsiWithAdvance) {
            isExistingMsiWithAdvance = true
            msiCurrentInstallmentText = autoMsiTimeline.currentInstallment.toString()
        }
    }

    // Participantes activos en la división
    val activeParticipants: List<String> = remember(splitIncludeMe, selectedSplitPartners) {
        val list = mutableListOf<String>()
        if (splitIncludeMe) list.add("Personal")
        list.addAll(selectedSplitPartners.filter { it != "Personal" })
        list.distinct()
    }

    // Cálculo de cuotas de cada participante
    val participantShares: List<Pair<String, Double>> = remember(
        isSplitExpense,
        capturedTotalAmount,
        activeParticipants,
        splitMode,
        customPercentages,
        customFixedAmounts
    ) {
        if (!isSplitExpense || activeParticipants.isEmpty() || capturedTotalAmount <= 0.0) {
            emptyList()
        } else {
            val count = activeParticipants.size
            if (splitMode == 0) {
                // Modo Porcentual o Equitativo
                val hasCustomPercentages = customPercentages.keys.containsAll(activeParticipants)
                if (!hasCustomPercentages) {
                    // Equitativo por defecto
                    val equalPct = 100.0 / count
                    activeParticipants.map { person ->
                        val share = (capturedTotalAmount * equalPct) / 100.0
                        person to share
                    }
                } else {
                    activeParticipants.map { person ->
                        val pct = customPercentages[person] ?: (100f / count)
                        val share = (capturedTotalAmount * pct.toDouble()) / 100.0
                        person to share
                    }
                }
            } else {
                // Modo Monto Fijo ($)
                activeParticipants.map { person ->
                    val textVal = customFixedAmounts[person] ?: ""
                    val fixed = CreditCardCalculator.parseLocalizedDouble(textVal) ?: (capturedTotalAmount / count)
                    person to fixed
                }
            }
        }
    }

    val totalAssignedAmount = participantShares.sumOf { it.second }

    // Requisito 2: Función de actualización para porcentajes cerrados (sin decimales) y auto-ajuste de complemento
    val updatePersonPercentage: (String, Float) -> Unit = { targetPerson, rawPct ->
        val roundedPct = Math.round(rawPct).coerceIn(0, 100).toFloat()
        val updated = customPercentages.toMutableMap()
        activeParticipants.forEach { p ->
            if (!updated.containsKey(p)) {
                updated[p] = Math.round(100f / activeParticipants.size).toFloat()
            }
        }
        updated[targetPerson] = roundedPct

        val others = activeParticipants.filter { it != targetPerson }
        if (others.size == 1) {
            // Exactamente 2 personas: la otra persona toma el complemento exacto (100 - X)
            updated[others[0]] = (100f - roundedPct).coerceIn(0f, 100f)
        } else if (others.isNotEmpty()) {
            // Más de 2 personas: distribuir el resto proporcionalmente con valores enteros cerrados
            val remainingPct = (100f - roundedPct).coerceAtLeast(0f)
            val sumOthers = others.sumOf { (updated[it] ?: 0f).toDouble() }.toFloat()
            if (sumOthers > 0.001f) {
                val factor = remainingPct / sumOthers
                var allocatedSoFar = 0f
                others.forEachIndexed { index, o ->
                    if (index == others.lastIndex) {
                        updated[o] = (remainingPct - allocatedSoFar).coerceIn(0f, 100f)
                    } else {
                        val share = Math.round((updated[o] ?: 0f) * factor).toFloat().coerceIn(0f, remainingPct)
                        updated[o] = share
                        allocatedSoFar += share
                    }
                }
            } else {
                val equalShare = Math.round(remainingPct / others.size).toFloat()
                var allocatedSoFar = 0f
                others.forEachIndexed { index, o ->
                    if (index == others.lastIndex) {
                        updated[o] = (remainingPct - allocatedSoFar).coerceIn(0f, 100f)
                    } else {
                        updated[o] = equalShare
                        allocatedSoFar += equalShare
                    }
                }
            }
        }
        customPercentages = updated
    }

    ModalBottomSheet(
        onDismissRequest = handleDismissAttempt,
        sheetState = sheetState,
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_add_expense_sheet")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // TÍTULO DE LA SECCIÓN SIEMPRE VISIBLE (PINNED HEADER CON BOTÓN CERRAR)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Registrar Nuevo Gasto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ingresa la fecha para calcular automáticamente el periodo de corte y pago",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = handleDismissAttempt) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar registro",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )

            // CONTENIDO CON SCROLL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // REQUISITO: FECHA DEL GASTO (REQUERIDA)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fecha del Gasto",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Requerido",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = displayDateFormat.format(Date(expenseDateMillis)).replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Define a qué estado de cuenta pertenece la compra",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("open_date_picker_button")
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cambiar", fontSize = 12.sp)
                        }
                    }

                    // Acceso rápido a fechas frecuentes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val todayMillis = System.currentTimeMillis()
                        val yesterdayMillis = todayMillis - (24 * 60 * 60 * 1000L)

                        FilterChip(
                            selected = isSameDay(expenseDateMillis, todayMillis),
                            onClick = {
                                expenseDateMillis = todayMillis
                                userOverriddenMonth = null
                            },
                            label = { Text("Hoy", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = isSameDay(expenseDateMillis, yesterdayMillis),
                            onClick = {
                                expenseDateMillis = yesterdayMillis
                                userOverriddenMonth = null
                            },
                            label = { Text("Ayer", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Tarjeta
                Text(
                    text = "Tarjeta Utilizada",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cards.forEach { card ->
                        val isSelected = card.id == selectedCardId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCardId = card.id
                                userOverriddenMonth = null
                            },
                            leadingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(card.primaryColorHex),
                                    modifier = Modifier.size(14.dp)
                                ) {}
                            },
                            label = { Text("${card.name} (corte ${card.cutoffDay})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // PERIODO A PAGAR CALCULADO AUTOMÁTICAMENTE CON OPCIÓN A EDITAR
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Periodo / Estado de Cuenta a Liquidar:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activePaymentMonth,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = if (userOverriddenMonth == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (userOverriddenMonth == null) "AUTOMÁTICO" else "EDITADO",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (userOverriddenMonth != null) {
                                TextButton(
                                    onClick = { userOverriddenMonth = null },
                                    modifier = Modifier.testTag("reset_auto_month_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revertir Auto", fontSize = 11.sp)
                                }
                            }
                        }

                        if (selectedCard != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val cardCutoff = selectedCard.cutoffDay
                            val cardDue = selectedCard.paymentDueDay
                            val cal = Calendar.getInstance().apply { timeInMillis = expenseDateMillis }
                            val expenseDay = cal.get(Calendar.DAY_OF_MONTH)

                            Text(
                                text = if (expenseDay <= cardCutoff) {
                                    "La compra fue el día $expenseDay (antes del corte del día $cardCutoff), por lo que se liquida en el corte de $activePaymentMonth (límite día $cardDue)."
                                } else {
                                    "La compra fue el día $expenseDay (después del corte del día $cardCutoff), ganando financiamiento hasta $activePaymentMonth (límite día $cardDue)."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Cambiar manualmente periodo:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableMonths.forEach { monthName ->
                                val isSelected = activePaymentMonth.equals(monthName, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { userOverriddenMonth = monthName },
                                    label = { Text(monthName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("month_select_$monthName")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Concepto
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto o Establecimiento *") },
                    placeholder = { Text("Ej. Gasolina, Despensa Walmart...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_concept_input")
                )

                // 1. REQUISITO: Header con botón para administrar conceptos sugeridos (agregar, editar, eliminar, mover)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Conceptos sugeridos:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { showManageConceptsDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Administrar conceptos",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Quick Concepts Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickConcepts.forEach { qc ->
                        FilterChip(
                            selected = concept.equals(qc, ignoreCase = true),
                            onClick = { concept = qc },
                            label = { Text(qc, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // MONTO TOTAL CAPTURADO (SIEMPRE EL MONTO TOTAL)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = {
                        Text(if (isMsi) "Monto Total de la Compra a MSI *" else "Monto Total del Gasto *")
                    },
                    placeholder = { Text(if (isMsi) "Ej. 12000.00" else "Ej. 1350.00") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // MESES SIN INTERESES Y CÁLCULO AUTOMÁTICO DE MENSUALIDADES
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMsi) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Meses Sin Intereses (MSI)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Captura el monto total y la app calcula la mensualidad",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isMsi,
                                onCheckedChange = { isMsi = it },
                                modifier = Modifier.testTag("msi_toggle")
                            )
                        }

                        if (isMsi) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Plazo en Meses:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                msiOptions.forEach { months ->
                                    FilterChip(
                                        selected = msiTotalMonths == months,
                                        onClick = { msiTotalMonths = months },
                                        label = { Text("$months meses", fontWeight = if (msiTotalMonths == months) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // TARJETA DE CÁLCULO AUTOMÁTICO DE MENSUALIDAD
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("msi_calculation_display")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Mensualidad fija calculada:",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Text(
                                            text = "${currencyFormat.format(calculatedMonthlyInstallment)}/mes",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (capturedTotalAmount > 0) {
                                            "Monto total ${currencyFormat.format(capturedTotalAmount)} diferido a $msiTotalMonths meses fijos sin intereses."
                                        } else {
                                            "Ingresa el monto total arriba para calcular las mensualidades automáticamente."
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Tarjeta de Cálculo Automático de MSI basada en fecha de compra y corte
                            if (autoMsiTimeline != null && selectedCard != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Cálculo Automático de Inicio y Avance",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = autoMsiTimeline.explanation,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Primer cargo en tarjeta:",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = autoMsiTimeline.firstChargeMonth,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Saldo pendiente en MSI:",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val remainingTotal = calculatedMonthlyInstallment * autoMsiTimeline.remainingInstallments
                                            Text(
                                                text = "${autoMsiTimeline.remainingInstallments} meses (${currencyFormat.format(remainingTotal)})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // SELECTOR: COMPRA NUEVA VS COMPRA EN CURSO CON AVANCE
                            Text(
                                text = "Estado inicial del plan:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !isExistingMsiWithAdvance,
                                    onClick = {
                                        isExistingMsiWithAdvance = false
                                        msiCurrentInstallmentText = "1"
                                    },
                                    label = { Text("Nueva (Mes 1 de $msiTotalMonths)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = isExistingMsiWithAdvance,
                                    onClick = {
                                        isExistingMsiWithAdvance = true
                                        if (msiCurrentInstallmentText == "1") {
                                            msiCurrentInstallmentText = "2"
                                        }
                                    },
                                    label = { Text("Con avance previo") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (isExistingMsiWithAdvance) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "¿Qué mensualidad se cobra en este corte ($activePaymentMonth)?",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Controles Stepper y Entrada numérica libre
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val current = effectiveCurrentInstallment
                                                    if (current > 1) {
                                                        msiCurrentInstallmentText = (current - 1).toString()
                                                    }
                                                },
                                                enabled = effectiveCurrentInstallment > 1
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Disminuir cuota")
                                            }

                                            OutlinedTextField(
                                                // Corrección: antes se reescribía el texto con el valor ya acotado
                                                // (coerceIn) en cada tecla, lo que podía "recortar" silenciosamente
                                                // lo que el usuario acababa de escribir. Ahora se conserva el texto
                                                // tal cual se tecleó; el acotado a [1, msiTotalMonths] ya lo hace
                                                // "effectiveCurrentInstallment" solo para los cálculos.
                                                value = msiCurrentInstallmentText,
                                                onValueChange = { input ->
                                                    msiCurrentInstallmentText = input.filter { it.isDigit() }.take(3)
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier
                                                    .width(72.dp)
                                                    .onFocusChanged { focusState ->
                                                        if (!focusState.isFocused) {
                                                            msiCurrentInstallmentText = effectiveCurrentInstallment.toString()
                                                        }
                                                    },
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            )

                                            IconButton(
                                                onClick = {
                                                    val current = effectiveCurrentInstallment
                                                    if (current < msiTotalMonths) {
                                                        msiCurrentInstallmentText = (current + 1).toString()
                                                    }
                                                },
                                                enabled = effectiveCurrentInstallment < msiTotalMonths
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Aumentar cuota")
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "de $msiTotalMonths meses",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Chips rápidos
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val presets = listOfNotNull(
                                                if (msiTotalMonths >= 6) 3 else null,
                                                if (msiTotalMonths >= 12) 6 else null,
                                                if (msiTotalMonths >= 18) 12 else null,
                                                msiTotalMonths / 2,
                                                (msiTotalMonths - 1).coerceAtLeast(1)
                                            ).distinct().filter { it in 1..msiTotalMonths }

                                            presets.forEach { preset ->
                                                FilterChip(
                                                    selected = effectiveCurrentInstallment == preset,
                                                    onClick = { msiCurrentInstallmentText = preset.toString() },
                                                    label = {
                                                        val labelText = when (preset) {
                                                            msiTotalMonths / 2 -> "A la mitad ($preset de $msiTotalMonths)"
                                                            msiTotalMonths - 1 -> "Casi liquidada ($preset de $msiTotalMonths)"
                                                            else -> "Cuota $preset"
                                                        }
                                                        Text(labelText, fontSize = 11.sp)
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Checkbox fecha original
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { autoEstimateOriginalDate = !autoEstimateOriginalDate },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = autoEstimateOriginalDate,
                                                onCheckedChange = { autoEstimateOriginalDate = it }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Calcular fecha de compra original automáticamente",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = if (effectiveCurrentInstallment > 1) {
                                                        "Se registrará hace ${effectiveCurrentInstallment - 1} meses en el historial"
                                                    } else {
                                                        "Comienza en el mes actual"
                                                    },
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Desglose del avance
                                        val monthsPaid = (effectiveCurrentInstallment - 1).coerceAtLeast(0)
                                        val amountPaidSoFar = calculatedMonthlyInstallment * monthsPaid
                                        val monthsRemaining = (msiTotalMonths - effectiveCurrentInstallment).coerceAtLeast(0)
                                        val remainingDebt = calculatedMonthlyInstallment * monthsRemaining

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("• Meses ya cubiertos antes de este corte:", fontSize = 11.sp)
                                                Text(
                                                    "$monthsPaid meses (${currencyFormat.format(amountPaidSoFar)})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("• Cargo en este corte ($activePaymentMonth):", fontSize = 11.sp)
                                                Text(
                                                    "Cuota $effectiveCurrentInstallment de $msiTotalMonths (${currencyFormat.format(calculatedMonthlyInstallment)})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("• Meses restantes por pagar:", fontSize = 11.sp)
                                                Text(
                                                    "$monthsRemaining meses (${currencyFormat.format(remainingDebt)})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. REQUISITO: DIVISOR DE GASTOS PARA 2 O MÁS PERSONAS (PARTICIPACIÓN PROPIA OPCIONAL)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isSplitExpense) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("split_expense_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSplitExpense) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dividir Gasto (2 o más personas)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Reparte entre múltiples personas con tu participación opcional",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isSplitExpense,
                                onCheckedChange = { isSplitExpense = it },
                                modifier = Modifier.testTag("split_expense_toggle")
                            )
                        }

                        if (isSplitExpense) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Participación del usuario (opcional)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "¿Incluir mi parte? (Personal)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (splitIncludeMe) "Tú asumes una porción del gasto" else "El gasto es completamente para otras personas",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = splitIncludeMe,
                                    onCheckedChange = { splitIncludeMe = it },
                                    modifier = Modifier.testTag("split_include_me_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Selección de personas con botón de administración (editar, agregar, eliminar)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Personas que dividen el gasto:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { showManagePeopleDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Administrar personas",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Chips de selección múltiple de personas (excluyendo "Personal" ya controlado por switch)
                            val selectablePartners = allPeople.filter { it != "Personal" }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectablePartners.forEach { person ->
                                    val isSelected = selectedSplitPartners.contains(person)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSplitPartners = if (isSelected) {
                                                selectedSplitPartners.filter { it != person }
                                            } else {
                                                selectedSplitPartners + person
                                            }
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        label = { Text(person) }
                                    )
                                }
                            }

                            // Validar mínimo de 2 participantes requeridos
                            if (activeParticipants.size < 2) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ Selecciona al menos 2 personas para dividir (o activa 'Incluir mi parte' + 1 persona).",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Modalidad de división: Porcentual o Monto fijo
                            Text(
                                text = "Modalidad de división:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = splitMode == 0,
                                    onClick = { splitMode = 0 },
                                    label = { Text("Porcentualmente (%)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = splitMode == 1,
                                    onClick = { splitMode = 1 },
                                    label = { Text("Monto Fijo ($)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Configuración y desglose por cada persona
                            if (activeParticipants.isNotEmpty()) {
                                Text(
                                    text = "Distribución entre participantes (${activeParticipants.size}):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                activeParticipants.forEachIndexed { idx, person ->
                                    val sharePair = participantShares.firstOrNull { it.first == person }
                                    val assignedAmount = sharePair?.second ?: 0.0
                                    val monthlyAssigned = if (isMsi && msiTotalMonths > 0) assignedAmount / msiTotalMonths else assignedAmount

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = null,
                                                            modifier = Modifier.padding(4.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = person,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = currencyFormat.format(assignedAmount),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    if (isMsi) {
                                                        Text(
                                                            text = "${currencyFormat.format(monthlyAssigned)}/mes ($msiTotalMonths MSI)",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            if (splitMode == 0) {
                                                // MODO PORCENTAJE AUTO-AJUSTABLE (Suma 100% con porcentajes cerrados)
                                                val currentPct = customPercentages[person] ?: Math.round(100f / activeParticipants.size).toFloat()
                                                
                                                // Slider con pasos discretos (múltiplos de 5%) + Casilla para escribir porcentaje manual
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Slider(
                                                        value = currentPct,
                                                        onValueChange = { newPct ->
                                                            customPercentageTexts = customPercentageTexts - person
                                                            updatePersonPercentage(person, newPct)
                                                        },
                                                        valueRange = 0f..100f,
                                                        steps = 19, // Múltiplos exactos de 5%: 0%, 5%, 10%, ..., 95%, 100%
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    OutlinedTextField(
                                                        // Mientras el usuario teclea, se muestra exactamente lo que escribió
                                                        // (incluso vacío) en vez de un valor recalculado en cada tecla.
                                                        value = customPercentageTexts[person] ?: currentPct.toInt().toString(),
                                                        onValueChange = { input ->
                                                            val digits = input.filter { it.isDigit() }.take(3)
                                                            customPercentageTexts = customPercentageTexts + (person to digits)
                                                            if (digits.isNotEmpty()) {
                                                                val num = digits.toIntOrNull()?.coerceIn(0, 100) ?: 0
                                                                updatePersonPercentage(person, num.toFloat())
                                                            }
                                                        },
                                                        suffix = { Text("%", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier
                                                            .width(80.dp)
                                                            .onFocusChanged { focusState ->
                                                                // Al perder el foco, si quedó vacío o inválido, se vuelve a
                                                                // mostrar el valor real en vez de dejar el campo en blanco.
                                                                if (!focusState.isFocused) {
                                                                    customPercentageTexts = customPercentageTexts - person
                                                                }
                                                            },
                                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                                                        )
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Porcentajes predefinidos de selección rápida (0%, 25%, 30%, 50%, 70%, 75%, 100%)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Atajos:",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    val predefinedPercentages = listOf(0, 25, 30, 50, 70, 75, 100)
                                                    predefinedPercentages.forEach { preset ->
                                                        val isSelected = currentPct.toInt() == preset
                                                        Surface(
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                            modifier = Modifier.clickable {
                                                                customPercentageTexts = customPercentageTexts - person
                                                                updatePersonPercentage(person, preset.toFloat())
                                                            }
                                                        ) {
                                                            Text(
                                                                text = "$preset%",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                // MODO MONTO FIJO ($) AUTO-AJUSTABLE (Suma Total)
                                                OutlinedTextField(
                                                    value = customFixedAmounts[person] ?: (if (capturedTotalAmount > 0) String.format(Locale.US, "%.2f", capturedTotalAmount / activeParticipants.size) else ""),
                                                    onValueChange = { newVal ->
                                                        val updated = customFixedAmounts.toMutableMap()
                                                        updated[person] = newVal

                                                        val enteredVal = newVal.toDoubleOrNull()
                                                        if (enteredVal != null && capturedTotalAmount > 0) {
                                                            val others = activeParticipants.filter { it != person }
                                                            if (others.size == 1) {
                                                                val complement = (capturedTotalAmount - enteredVal).coerceAtLeast(0.0)
                                                                updated[others[0]] = String.format(Locale.US, "%.2f", complement)
                                                            }
                                                        }
                                                        customFixedAmounts = updated
                                                    },
                                                    label = { Text("Monto asignado a $person") },
                                                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Resumen de cuadre
                                Surface(
                                    color = if (Math.abs(totalAssignedAmount - capturedTotalAmount) < 0.1) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Total repartido: ${currencyFormat.format(totalAssignedAmount)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Monto total: ${currencyFormat.format(capturedTotalAmount)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Si NO se divide, mostrar selección simple de beneficiario
                if (!isSplitExpense) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Para quién es este gasto?",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showManagePeopleDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Administrar personas",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allPeople.forEach { ben ->
                            val isSelected = ben == selectedBeneficiary
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBeneficiary = ben },
                                label = { Text(ben) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val isSplitBalanced = !isSplitExpense || Math.abs(capturedTotalAmount - totalAssignedAmount) < 0.05

                if (isSplitExpense && !isSplitBalanced) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val diff = Math.abs(capturedTotalAmount - totalAssignedAmount)
                            Text(
                                text = "La suma asignada ($${String.format(Locale.US, "%.2f", totalAssignedAmount)}) no coincide con el total ($${String.format(Locale.US, "%.2f", capturedTotalAmount)}). Diferencia: $${String.format(Locale.US, "%.2f", diff)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Botón Guardar Gasto
                val canSave = concept.isNotBlank() && capturedTotalAmount > 0.0 && selectedCardId != 0L &&
                        (!isSplitExpense || (activeParticipants.size >= 2 && isSplitBalanced))

                Button(
                    onClick = {
                        if (canSave) {
                            val effectiveExpenseDateMillis = if (isMsi && isExistingMsiWithAdvance && autoEstimateOriginalDate) {
                                val cal = Calendar.getInstance()
                                CreditCardCalculator.calculateOriginalPurchaseDate(
                                    statementMonthName = activePaymentMonth,
                                    statementYear = cal.get(Calendar.YEAR),
                                    currentInstallment = effectiveCurrentInstallment
                                )
                            } else {
                                expenseDateMillis
                            }

                            if (isSplitExpense) {
                                // Guardar registro individual para cada persona que participó en la división
                                participantShares.forEach { (person, shareTotal) ->
                                    val monthlyShare = if (isMsi && msiTotalMonths > 0) shareTotal / msiTotalMonths else shareTotal
                                    onSave(
                                        selectedCardId,
                                        "$concept (Parte $person)",
                                        monthlyShare,
                                        effectiveExpenseDateMillis,
                                        person,
                                        selectedCategory,
                                        isMsi,
                                        msiTotalMonths,
                                        effectiveCurrentInstallment,
                                        activePaymentMonth,
                                        shareTotal
                                    )
                                }
                            } else {
                                // Para MSI: la mensualidad calculada es el cargo mensual al estado de cuenta
                                val finalMonthlyCharge = if (isMsi) calculatedMonthlyInstallment else capturedTotalAmount

                                onSave(
                                    selectedCardId,
                                    concept,
                                    finalMonthlyCharge,
                                    effectiveExpenseDateMillis,
                                    selectedBeneficiary,
                                    selectedCategory,
                                    isMsi,
                                    msiTotalMonths,
                                    effectiveCurrentInstallment,
                                    activePaymentMonth,
                                    capturedTotalAmount
                                )
                            }
                            onDismiss()
                        }
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_expense_button")
                ) {
                    Text(
                        text = if (isSplitExpense) {
                            "Guardar Gasto Dividido (${activeParticipants.size} personas)"
                        } else if (isMsi) {
                            "Guardar Gasto (${currencyFormat.format(calculatedMonthlyInstallment)}/mes)"
                        } else {
                            "Guardar Gasto (${currencyFormat.format(capturedTotalAmount)})"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Modal DatePickerDialog para elegir la fecha requerida
    if (showDatePickerDialog) {
        // Obtenemos el inicio del día local en UTC para sincronizar el DatePicker
        val localCal = Calendar.getInstance().apply {
            timeInMillis = expenseDateMillis
        }
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH))
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = utcCal.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedUtcMillis ->
                            // Convertir la fecha UTC del DatePicker a la hora local para evitar el desfase de 1 día antes
                            val pickedUtcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selectedUtcMillis
                            }
                            val updatedLocalCal = Calendar.getInstance().apply {
                                set(
                                    pickedUtcCal.get(Calendar.YEAR),
                                    pickedUtcCal.get(Calendar.MONTH),
                                    pickedUtcCal.get(Calendar.DAY_OF_MONTH),
                                    12, 0, 0 // Mediodía para evitar cualquier transición de medianoche
                                )
                            }
                            expenseDateMillis = updatedLocalCal.timeInMillis
                            userOverriddenMonth = null // Recalcula automáticamente el periodo al cambiar fecha
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Diálogo de confirmación antes de descartar cambios en el registro
    if (showDiscardWarningDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardWarningDialog = false },
            title = { Text("¿Deseas descartar el registro?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Aún no has guardado este gasto. Si sales ahora, los datos introducidos se perderán."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardWarningDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardWarningDialog = false
                        // Continuar editando
                    }
                ) {
                    Text("Seguir editando", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // 1. DIÁLOGO PARA ADMINISTRAR CONCEPTOS SUGERIDOS DE GASTO (editar, reordenar, eliminar, agregar)
    if (showManageConceptsDialog) {
        ManageItemsDialog(
            title = "Conceptos de Gasto Sugeridos",
            subtitle = "Agrega, edita, elimina o reordena los conceptos sugeridos",
            items = quickConcepts,
            itemLabel = "Concepto de gasto",
            onDismiss = { showManageConceptsDialog = false },
            onSaveList = { updatedList ->
                quickConcepts = updatedList
                if (!updatedList.contains(concept) && updatedList.isNotEmpty() && concept.isBlank()) {
                    concept = updatedList.first()
                }
            }
        )
    }

    // 3. DIÁLOGO PARA ADMINISTRAR PERSONAS / BENEFICIARIOS (editar, reordenar, eliminar, agregar)
    if (showManagePeopleDialog) {
        ManageItemsDialog(
            title = "Personas y Beneficiarios",
            subtitle = "Administra las personas disponibles para asignar o dividir gastos",
            items = allPeople,
            itemLabel = "Persona",
            onDismiss = { showManagePeopleDialog = false },
            onSaveList = { updatedList ->
                allPeople = updatedList
                if (!updatedList.contains(selectedBeneficiary) && updatedList.isNotEmpty()) {
                    selectedBeneficiary = updatedList.first()
                }
                // Filtrar partners seleccionados que hayan sido eliminados
                selectedSplitPartners = selectedSplitPartners.filter { updatedList.contains(it) }
            }
        )
    }
}

private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
