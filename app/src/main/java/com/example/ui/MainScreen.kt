package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.components.AppLockScreen
import com.example.ui.util.AppHaptics
import com.example.ui.util.LocalPrivacyMode
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.QuickAddExpenseSheet
import com.example.ui.components.QuickAddPaymentSheet
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.CardsManagementScreen
import com.example.ui.screens.FuelScreen
import com.example.ui.screens.MsiTrackerScreen
import com.example.ui.screens.RecommendationScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.StatementsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.NaturalBackgroundLight
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalPrimaryGreen
import com.example.ui.theme.NaturalSecondaryContainer
import com.example.ui.theme.NaturalSurfaceVariantLight
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary
import com.example.ui.viewmodel.CreditCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CreditCardViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddPaymentSheet by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Scroll listener para reducir el tamaño del botón flotante al desplazarse
    var isFabExpanded by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -12f) {
                    isFabExpanded = false // Reduce tamaño a icono al desplazarse hacia abajo
                } else if (available.y > 12f) {
                    isFabExpanded = true // Restaura texto y tamaño completo al subir
                }
                return Offset.Zero
            }
        }
    }

    // Comportamiento del botón Atrás: Solo activo cuando no hay hojas modales abiertas
    BackHandler(enabled = !showAddExpenseSheet && !showAddPaymentSheet) {
        when {
            selectedTab != 0 -> selectedTab = 0 // Primero ir al Recomendador
            else -> showExitConfirmDialog = true // Confirmar salida
        }
    }

    val cards by viewModel.allCards.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val msiSummaries by viewModel.msiSummaries.collectAsStateWithLifecycle()
    val cashFlowProjections by viewModel.cashFlowReleases.collectAsStateWithLifecycle()
    val statementSummary by viewModel.statementSummary.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedStatementMonth.collectAsStateWithLifecycle()
    val selectedCardId by viewModel.selectedCardId.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val quickConcepts by viewModel.quickConcepts.collectAsStateWithLifecycle()
    val peopleList by viewModel.peopleList.collectAsStateWithLifecycle()
    val paymentConcepts by viewModel.paymentConcepts.collectAsStateWithLifecycle()
    val payersList by viewModel.payersList.collectAsStateWithLifecycle()
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val subscriptionTrackings by viewModel.subscriptionTrackings.collectAsStateWithLifecycle()
    val trackingYearMonth by viewModel.trackingYearMonth.collectAsStateWithLifecycle()
    val fuelEntries by viewModel.allFuelEntries.collectAsStateWithLifecycle()
    val serviceEntries by viewModel.allServiceEntries.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val firebaseUser by viewModel.currentFirebaseUser.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()
    val isHapticsEnabled by viewModel.isHapticEnabled.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalPrivacyMode provides isPrivacyMode) {
        if (isAppLocked) {
            AppLockScreen(
                onUnlockAttempt = { pin -> viewModel.unlockApp(pin) },
                onBiometricUnlock = { viewModel.unlockAppWithBiometrics() },
                hapticsEnabled = isHapticsEnabled
            )
        } else {
            Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Natural Tones Header
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "BIENVENIDO • CONTROL TDC"
                                1 -> "ETAPAS Y LIQUIDACIÓN"
                                2 -> "DIVISIÓN Y CUOTAS"
                                3 -> "COMBUSTIBLE Y VIAJES"
                                4 -> "PERIODOS DE FACTURACIÓN"
                                5 -> "BILLETERA DE TARJETAS"
                                6 -> "PERFIL DE TITULAR"
                                7 -> "AGUA, LUZ Y GAS"
                                else -> "CONTROL TDC"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (selectedTab) {
                                0 -> "Recomendador"
                                1 -> "MSI & Flujo"
                                2 -> "Suscripciones"
                                3 -> "Gasolina"
                                4 -> "Estados de Cuenta"
                                5 -> "Mis Tarjetas"
                                6 -> "Mi Cuenta"
                                7 -> "Servicios"
                                else -> "Control TDC"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de Modo Privacidad (Ocultar cifras)
                        Surface(
                            shape = CircleShape,
                            color = if (isPrivacyMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    AppHaptics.light(haptic, isHapticsEnabled)
                                    viewModel.togglePrivacyMode()
                                }
                                .testTag("btn_privacy_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPrivacyMode) "Desactivar modo privacidad" else "Activar modo privacidad",
                                    tint = if (isPrivacyMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Botón de Servicios (agua, luz, gas) — reemplaza el antiguo botón de
                        // "Notificaciones" que no tenía ninguna acción asociada.
                        Surface(
                            shape = CircleShape,
                            color = if (selectedTab == 7) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { selectedTab = 7 }
                                .testTag("btn_top_services")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Servicios: agua, luz y gas",
                                    tint = if (selectedTab == 7) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // User avatar / Cuenta
                        Surface(
                            shape = CircleShape,
                            color = if (selectedTab == 6) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                            border = if (selectedTab == 6) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { selectedTab = 6 }
                                .testTag("btn_top_account_profile")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userProfile.initials,
                                    color = if (selectedTab == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Subtle divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            selectedTab = 0
                        },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Recomendador") },
                        label = { Text("Recomendar", fontSize = 9.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_recommendations")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            selectedTab = 1
                        },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "MSI & Flujo") },
                        label = { Text("MSI", fontSize = 9.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_msi")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            selectedTab = 2
                        },
                        icon = { Icon(Icons.Default.Subscriptions, contentDescription = "Suscripciones") },
                        label = { Text("Suscrip.", fontSize = 9.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_subscriptions")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            selectedTab = 3
                        },
                        icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Gasolina") },
                        label = { Text("Gasolina", fontSize = 9.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_fuel")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            selectedTab = 4
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Periodos") },
                        label = { Text("Cuentas", fontSize = 9.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_statements")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 5,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            selectedTab = 5
                        },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Tarjetas") },
                        label = { Text("Tarjetas", fontSize = 9.sp, fontWeight = if (selectedTab == 5) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_cards")
                    )
                }
            }
        },
        floatingActionButton = {
            // El botón "Registrar gasto" se muestra flotante en Recomendador o Cuentas
            if (selectedTab == 0 || selectedTab == 4) {
                ExtendedFloatingActionButton(
                    expanded = isFabExpanded,
                    onClick = { showAddExpenseSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Registrar Gasto") },
                    text = { Text("Registrar Gasto", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("main_fab_add_expense")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(220)) { width -> width / 4 } + fadeIn(animationSpec = tween(220)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { width -> -width / 4 } + fadeOut(animationSpec = tween(220)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(220)) { width -> -width / 4 } + fadeIn(animationSpec = tween(220)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { width -> width / 4 } + fadeOut(animationSpec = tween(220)))
                    }
                },
                label = "main_screen_tab_transition"
            ) { currentTab ->
                when (currentTab) {
                    0 -> RecommendationScreen(
                    recommendations = recommendations,
                    statementSummary = statementSummary,
                    onOpenAddExpense = { showAddExpenseSheet = true },
                    onOpenAddPayment = { showAddPaymentSheet = true },
                    onSelectCardForStatement = { cardId ->
                        viewModel.setSelectedCard(cardId)
                        selectedTab = 4
                    },
                    onAddCard = { selectedTab = 5 }
                )

                1 -> MsiTrackerScreen(
                    msiList = msiSummaries,
                    cashFlowProjections = cashFlowProjections,
                    cards = cards,
                    allExpenses = allExpenses,
                    allPayments = allPayments,
                    onOpenAddExpense = { showAddExpenseSheet = true },
                    onAdvanceInstallment = { exp -> viewModel.advanceMsiInstallment(exp) },
                    onUpdateMsiExpense = { exp, concept, totalAmount, monthlyPayment, cardId, category, beneficiary, msiTotalMonths, msiCurrentInstallment, notes ->
                        viewModel.updateMsiExpense(
                            expense = exp,
                            newConcept = concept,
                            newMonthlyAmount = monthlyPayment,
                            newTotalPurchaseAmount = totalAmount,
                            newCardId = cardId,
                            newBeneficiary = beneficiary,
                            newCategory = category,
                            newTotalMonths = msiTotalMonths,
                            newCurrentInstallment = msiCurrentInstallment,
                            newNotes = notes
                        )
                    },
                    onDeleteMsiExpense = { exp ->
                        viewModel.deleteExpense(exp)
                    }
                )

                2 -> SubscriptionsScreen(
                    subscriptions = subscriptions,
                    trackings = subscriptionTrackings,
                    cards = cards,
                    selectedYearMonth = trackingYearMonth,
                    userProfileName = userProfile.fullName,
                    onSelectYearMonth = { viewModel.setTrackingYearMonth(it) },
                    onAddSubscription = { name, cardId, billingDay, totalAmount, category, participants, periodicity ->
                        viewModel.addSubscription(name, cardId, billingDay, totalAmount, category, participants, periodicity)
                    },
                    onUpdateSubscription = { sub, newName, newCardId, newBillingDay, newTotal, newCategory, participants, affectCurrentMonth, periodicity ->
                        viewModel.updateSubscription(sub, newName, newCardId, newBillingDay, newTotal, newCategory, participants, affectCurrentMonth, periodicity)
                    },
                    onToggleTrackingPaid = { tracking ->
                        viewModel.toggleTrackingPaid(tracking)
                    },
                    onMarkAllPaid = { subId ->
                        viewModel.markAllParticipantsPaid(subId)
                    },
                    onToggleActive = { sub ->
                        viewModel.toggleSubscriptionActive(sub)
                    },
                    onDeleteSubscription = { sub, keepCurrentMonth ->
                        viewModel.deleteSubscription(sub, keepCurrentMonth)
                    }
                )

                3 -> FuelScreen(
                    fuelEntries = fuelEntries,
                    cards = cards,
                    onAddFuelEntry = { cardId, km, fuelType, price, liters, isDivided, dividedWith, dividedCount, notes, dateMillis ->
                        viewModel.addFuelEntry(
                            cardId = cardId,
                            kmDriven = km,
                            fuelType = fuelType,
                            pricePerLiter = price,
                            litersLoaded = liters,
                            isDivided = isDivided,
                            dividedWith = dividedWith,
                            dividedCount = dividedCount,
                            notes = notes,
                            dateMillis = dateMillis
                        )
                    },
                    onDeleteFuelEntry = { entry ->
                        viewModel.deleteFuelEntry(entry)
                    },
                    onUpdateFuelEntry = { entry, cardId, km, fuelType, price, liters, isDivided, dividedWith, dividedCount, notes, dateMillis ->
                        viewModel.updateFuelEntry(
                            entry = entry,
                            newCardId = cardId,
                            newKmDriven = km,
                            newFuelType = fuelType,
                            newPricePerLiter = price,
                            newLitersLoaded = liters,
                            newIsDivided = isDivided,
                            newDividedWith = dividedWith,
                            newDividedCount = dividedCount,
                            newNotes = notes,
                            newDateMillis = dateMillis
                        )
                    }
                )

                4 -> StatementsScreen(
                    cards = cards,
                    selectedMonth = selectedMonth,
                    availableMonths = viewModel.availableMonths,
                    selectedCardId = selectedCardId,
                    statementSummary = statementSummary,
                    expenses = allExpenses,
                    payments = allPayments,
                    onSelectMonth = { viewModel.setSelectedMonth(it) },
                    onSelectCard = { viewModel.setSelectedCard(it) },
                    onOpenAddExpense = { showAddExpenseSheet = true },
                    onOpenAddPayment = { showAddPaymentSheet = true },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onDeletePayment = { viewModel.deletePayment(it) }
                )

                5 -> CardsManagementScreen(
                    cards = cards,
                    expenses = allExpenses,
                    payments = allPayments,
                    onAddCard = { name, bank, cutoff, due, limit, pColor, sColor, lastDigits, net, isDep, grace, cardholder ->
                        viewModel.addCard(name, bank, cutoff, due, limit, pColor, sColor, lastDigits, net, isDep, grace, cardholder)
                    },
                    onUpdateCardDates = { card, newName, newCutoff, newDue, newLimit, newGrace, isDep, cardholder, pColor, sColor, newNet, newBank, newRate ->
                        viewModel.updateCardDates(
                            card = card,
                            newName = newName,
                            newCutoffDay = newCutoff,
                            newPaymentDueDay = newDue,
                            newLimit = newLimit,
                            newGraceDays = newGrace,
                            isDepartmental = isDep,
                            cardholderName = cardholder,
                            primaryColorHex = pColor,
                            secondaryColorHex = sColor,
                            newNetwork = newNet,
                            newBank = newBank,
                            newAnnualInterestRatePercent = newRate
                        )
                    },
                    onDeleteCard = { viewModel.deleteCard(it) }
                )

                6 -> AccountScreen(
                    userProfile = userProfile,
                    cards = cards,
                    subscriptions = subscriptions,
                    fuelEntries = fuelEntries,
                    syncState = syncState,
                    firebaseUser = firebaseUser,
                    isPrivacyMode = isPrivacyMode,
                    onTogglePrivacyMode = { viewModel.togglePrivacyMode() },
                    isHapticsEnabled = isHapticsEnabled,
                    onToggleHaptics = { viewModel.setHapticsEnabled(it) },
                    themeMode = themeMode,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    isAppLockEnabled = isAppLockEnabled,
                    onSetAppLock = { enabled, pin -> viewModel.setAppLock(enabled, pin) },
                    onSaveProfile = { fullName, email, shortName ->
                        viewModel.updateUserProfile(fullName, email, shortName)
                    },
                    onSyncToCloud = {
                        viewModel.syncDataToCloud()
                    },
                    onRestoreFromCloud = {
                        viewModel.restoreDataFromCloud()
                    },
                    onSignOutCloud = {
                        viewModel.signOutFromCloud()
                    },
                    onLaunchGoogleSignIn = {
                        showGoogleSignInDialog = true
                    },
                    onClose = { selectedTab = 0 }
                )

                7 -> ServicesScreen(
                    entries = serviceEntries,
                    onAddEntry = { serviceType, dateMillis, amount, consumption, notes ->
                        viewModel.addServiceEntry(
                            serviceType = serviceType,
                            dateMillis = dateMillis,
                            amount = amount,
                            consumption = consumption,
                            notes = notes
                        )
                    },
                    onUpdateEntry = { entry, dateMillis, amount, consumption, notes ->
                        viewModel.updateServiceEntry(
                            entry = entry,
                            newDateMillis = dateMillis,
                            newAmount = amount,
                            newConsumption = consumption,
                            newNotes = notes
                        )
                    },
                    onDeleteEntry = { viewModel.deleteServiceEntry(it) }
                )
            }
            }
        }
    }

    // Modal Sheet for adding expense
    if (showAddExpenseSheet) {
        QuickAddExpenseSheet(
            cards = cards,
            defaultCardId = if (selectedTab == 4) selectedCardId else null,
            currentMonth = selectedMonth,
            lockToCurrentMonth = (selectedTab == 4),
            availableMonths = viewModel.availableMonths,
            initialConcepts = quickConcepts,
            initialPeople = peopleList,
            onConceptsListChanged = { viewModel.updateQuickConcepts(it) },
            onPeopleListChanged = { viewModel.updatePeopleList(it) },
            onDismiss = { showAddExpenseSheet = false },
            onSave = { cardId, concept, amount, dateMillis, beneficiary, category, isMsi, totalM, curInst, sMonth, totalPurchase ->
                viewModel.addExpense(
                    cardId = cardId,
                    concept = concept,
                    amount = amount,
                    dateMillis = dateMillis,
                    beneficiary = beneficiary,
                    category = category,
                    isMsi = isMsi,
                    msiTotalMonths = totalM,
                    msiCurrentInstallment = curInst,
                    notes = "",
                    targetStatementMonth = sMonth,
                    msiTotalPurchaseAmount = totalPurchase
                )
            }
        )
    }

    // Modal Sheet for adding payment
    if (showAddPaymentSheet) {
        QuickAddPaymentSheet(
            cards = cards,
            defaultCardId = selectedCardId,
            currentMonth = selectedMonth,
            availableMonths = viewModel.availableMonths,
            initialConcepts = paymentConcepts,
            initialPayers = payersList,
            onConceptsListChanged = { viewModel.updatePaymentConcepts(it) },
            onPayersListChanged = { viewModel.updatePayersList(it) },
            onDismiss = { showAddPaymentSheet = false },
            onSave = { cardId, concept, amount, payer, sMonth ->
                viewModel.addPayment(
                    cardId = cardId,
                    concept = concept,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    sourcePayer = payer,
                    targetStatementMonth = sMonth,
                    notes = ""
                )
            }
        )
    }

    // Diálogo de confirmación para salir de la aplicación
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "¿Salir de la aplicación?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Te encuentras en la pantalla de Recomendador principal. ¿Confirmas que deseas cerrar y salir de la aplicación?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_exit_btn")
                ) {
                    Text("Salir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_exit_btn")
                ) {
                    Text("Permanecer", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("exit_confirmation_dialog")
        )
    }

    // Modal / Diálogo de inicio de sesión con Google & Firebase
    if (showGoogleSignInDialog) {
        var clientIdInput by remember { mutableStateOf("") }
        var isSigningIn by remember { mutableStateOf(false) }
        var signInError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSigningIn) showGoogleSignInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Iniciar Sesión con Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Conecta tu cuenta de Google para sincronizar tus tarjetas y finanzas entre todos tus dispositivos Android sin costo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!viewModel.googleAuthManager.isFirebaseReady) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Paso previo requerido:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Coloca tu archivo 'google-services.json' en la raíz del módulo 'app/'. Revisa la guía en el chat para el paso a paso.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = clientIdInput,
                            onValueChange = { clientIdInput = it; signInError = null },
                            label = { Text("Web Client ID de Firebase (Opcional)") },
                            placeholder = { Text("xxxxxx.apps.googleusercontent.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    signInError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!viewModel.googleAuthManager.isFirebaseReady) {
                            signInError = "Aún falta agregar el archivo google-services.json al proyecto."
                            return@Button
                        }
                        isSigningIn = true
                        signInError = null
                        coroutineScope.launch {
                            val res = viewModel.googleAuthManager.signInWithGoogle(clientIdInput)
                            isSigningIn = false
                            res.fold(
                                onSuccess = { user ->
                                    viewModel.onFirebaseUserAuthenticated(user)
                                    showGoogleSignInDialog = false
                                },
                                onFailure = { ex ->
                                    signInError = ex.localizedMessage ?: "Error al iniciar sesión con Google."
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSigningIn) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Continuar con Google", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoogleSignInDialog = false }
                ) {
                    Text("Cerrar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
        }
    }
}
