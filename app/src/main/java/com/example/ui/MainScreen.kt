package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.auth.FirebaseInitializer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.screens.SearchScreen
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
fun MainScreen(viewModel: CreditCardViewModel, initialAction: String? = null) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddPaymentSheet by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Acceso directo de la app ("mantener presionado" el ícono): abre directo el registro
    // correspondiente en cuanto se lanza la actividad desde ese shortcut.
    LaunchedEffect(initialAction) {
        when (initialAction) {
            "add_expense" -> showAddExpenseSheet = true
            "add_payment" -> showAddPaymentSheet = true
        }
    }

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
    val isDark = isSystemInDarkTheme()
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                        // Botón de Búsqueda global
                        Surface(
                            shape = CircleShape,
                            color = if (selectedTab == 9) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { selectedTab = 9 }
                                .testTag("btn_top_search")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = if (selectedTab == 9) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Botón de Estado / Acceso a Nube Firebase
                        val currentFbUser = firebaseUser
                        Surface(
                            shape = CircleShape,
                            color = if (currentFbUser != null) {
                                if (isDark) androidx.compose.ui.graphics.Color(0xFF1E392A) else androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = if (currentFbUser != null) {
                                androidx.compose.foundation.BorderStroke(1.dp, if (isDark) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color(0xFF2E6C38))
                            } else null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    AppHaptics.light(haptic, isHapticsEnabled)
                                    selectedTab = 6
                                }
                                .testTag("btn_top_cloud_sync")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (currentFbUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = if (currentFbUser != null) "Nube conectada: ${currentFbUser.email}" else "Conectar Firebase",
                                    tint = if (currentFbUser != null) {
                                        if (isDark) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color(0xFF2E6C38)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
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
                    // "Más": agrupa Suscripciones y Gasolina en un menú desplegable en vez de
                    // ocupar dos espacios fijos en la barra de navegación (antes eran 6 pestañas
                    // siempre visibles; con esto quedan 5, más cómodo en pantallas de celular).
                    // No cambia a qué pestaña navega cada una (siguen siendo selectedTab 2 y 3).
                    NavigationBarItem(
                        selected = selectedTab in listOf(2, 3, 7),
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            showMoreMenu = true
                        },
                        icon = {
                            Box {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Más")
                                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Suscripciones") },
                                        leadingIcon = { Icon(Icons.Default.Subscriptions, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            selectedTab = 2
                                        },
                                        modifier = Modifier.testTag("nav_subscriptions")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Gasolina") },
                                        leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            selectedTab = 3
                                        },
                                        modifier = Modifier.testTag("nav_fuel")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Servicios (Luz, Agua...)") },
                                        leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            selectedTab = 7
                                        },
                                        modifier = Modifier.testTag("nav_services")
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    "Cuenta y Nube Firebase",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    if (firebaseUser != null) "Conectado (${firebaseUser?.email})" else "Sincronización multi-dispositivo",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (firebaseUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            selectedTab = 6
                                        },
                                        modifier = Modifier.testTag("nav_cloud_account")
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                "Más",
                                fontSize = 9.sp,
                                fontWeight = if (selectedTab in listOf(2, 3, 7)) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_more")
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
            // Menú de acciones rápidas flotante en Recomendador o Cuentas: antes solo abría
            // "Registrar Gasto"; ahora se expande para elegir entre Gasto, Abono, Suscripción,
            // Gasolina o Servicio en un solo lugar, en vez de tener que navegar primero a cada
            // pestaña para encontrar su propio botón de agregar.
            if (selectedTab == 0 || selectedTab == 4) {
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(visible = showFabMenu) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            QuickActionFab(
                                icon = Icons.Default.Bolt,
                                label = "Servicio",
                                onClick = { showFabMenu = false; selectedTab = 7 }
                            )
                            QuickActionFab(
                                icon = Icons.Default.LocalGasStation,
                                label = "Gasolina",
                                onClick = { showFabMenu = false; selectedTab = 3 }
                            )
                            QuickActionFab(
                                icon = Icons.Default.Subscriptions,
                                label = "Suscripción",
                                onClick = { showFabMenu = false; selectedTab = 2 }
                            )
                            QuickActionFab(
                                icon = Icons.Default.Paid,
                                label = "Abono",
                                onClick = { showFabMenu = false; showAddPaymentSheet = true }
                            )
                            QuickActionFab(
                                icon = Icons.Default.Add,
                                label = "Gasto",
                                onClick = { showFabMenu = false; showAddExpenseSheet = true }
                            )
                        }
                    }

                    ExtendedFloatingActionButton(
                        expanded = isFabExpanded,
                        onClick = {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            showFabMenu = !showFabMenu
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        icon = {
                            Icon(
                                imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (showFabMenu) "Cerrar menú" else "Registrar"
                            )
                        },
                        text = { Text(if (showFabMenu) "Cerrar" else "Registrar", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("main_fab_add_expense")
                    )
                }
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
                    onUndoAdvanceInstallment = { exp -> viewModel.undoAdvanceMsiInstallment(exp) },
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
                    onUpdateCardDates = { card, newName, newCutoff, newDue, newLimit, newGrace, isDep, cardholder, pColor, sColor, newNet, newBank, newRate, newDigits ->
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
                            newAnnualInterestRatePercent = newRate,
                            newLast4Digits = newDigits
                        )
                    },
                    onDeleteCard = { viewModel.deleteCard(it) }
                )

                6 -> AccountScreen(
                    userProfile = userProfile,
                    cards = cards,
                    subscriptions = subscriptions,
                    fuelEntries = fuelEntries,
                    expenses = allExpenses,
                    payments = allPayments,
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

                9 -> SearchScreen(
                    expenses = allExpenses,
                    payments = allPayments,
                    cards = cards,
                    onClose = { selectedTab = 0 }
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

    // Modal / Diálogo integral de inicio de sesión con Firebase (Google, Correo y Acceso Rápido)
    if (showGoogleSignInDialog) {
        var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Google, 1: Correo, 2: Rápido
        var clientIdInput by remember { mutableStateOf("") }
        var isSigningIn by remember { mutableStateOf(false) }
        var signInError by remember { mutableStateOf<String?>(null) }
        var signInSuccessMessage by remember { mutableStateOf<String?>(null) }

        // Campos de correo y contraseña
        var isRegisterMode by remember { mutableStateOf(false) }
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var nameInput by remember { mutableStateOf("") }

        // Mostrar datos técnicos
        var showTechnicalDetails by remember { mutableStateOf(false) }
        val clipboardManager = LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { if (!isSigningIn) showGoogleSignInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Conexión con Firebase",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Conecta la aplicación a la nube de Firebase para respaldar y sincronizar tus tarjetas y finanzas.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Pestañas de método de acceso
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0; signInError = null },
                            text = { Text("Google", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1; signInError = null },
                            text = { Text("Correo", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    when (selectedAuthTab) {
                        // --- Pestaña 0: Google Sign-In ---
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Inicia sesión con tu cuenta de Google mediante el gestor de credenciales de Android.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = clientIdInput,
                                    onValueChange = { clientIdInput = it; signInError = null },
                                    label = { Text("Web Client ID (Opcional)") },
                                    placeholder = { Text("Usa el configurado por defecto") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        isSigningIn = true
                                        signInError = null
                                        signInSuccessMessage = null
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
                                    enabled = !isSigningIn,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSigningIn) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else {
                                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text("Continuar con Google", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // --- Pestaña 1: Correo y Contraseña ---
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isRegisterMode) "Crear nueva cuenta" else "Iniciar sesión con correo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(
                                        onClick = { isRegisterMode = !isRegisterMode; signInError = null },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = if (isRegisterMode) "¿Ya tienes cuenta? Entrar" else "¿Nuevo? Regístrate",
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isRegisterMode) {
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it; signInError = null },
                                        label = { Text("Nombre o Apodo") },
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it; signInError = null },
                                    label = { Text("Correo Electrónico") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it; signInError = null },
                                    label = { Text("Contraseña (mín. 6 letras)") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        isSigningIn = true
                                        signInError = null
                                        signInSuccessMessage = null
                                        coroutineScope.launch {
                                            val res = if (isRegisterMode) {
                                                viewModel.googleAuthManager.signUpWithEmailAndPassword(
                                                    email = emailInput,
                                                    password = passwordInput,
                                                    displayName = nameInput
                                                )
                                            } else {
                                                viewModel.googleAuthManager.signInWithEmailAndPassword(
                                                    email = emailInput,
                                                    password = passwordInput
                                                )
                                            }
                                            isSigningIn = false
                                            res.fold(
                                                onSuccess = { user ->
                                                    viewModel.onFirebaseUserAuthenticated(user)
                                                    showGoogleSignInDialog = false
                                                },
                                                onFailure = { ex ->
                                                    signInError = ex.localizedMessage ?: "Error al autenticar con Firebase."
                                                }
                                            )
                                        }
                                    },
                                    enabled = !isSigningIn,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSigningIn) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (isRegisterMode) "Registrar Cuenta" else "Iniciar Sesión",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!isRegisterMode) {
                                    TextButton(
                                        onClick = {
                                            if (emailInput.isBlank()) {
                                                signInError = "Ingresa tu correo en el campo de arriba para enviarte el enlace."
                                                return@TextButton
                                            }
                                            coroutineScope.launch {
                                                val res = viewModel.googleAuthManager.sendPasswordResetEmail(emailInput)
                                                res.fold(
                                                    onSuccess = {
                                                        signInSuccessMessage = "Correo de recuperación enviado a $emailInput."
                                                        signInError = null
                                                    },
                                                    onFailure = { ex ->
                                                        signInError = ex.localizedMessage ?: "Error al enviar recuperación."
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("¿Olvidaste tu contraseña? Restablecer", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Mensajes de error o éxito
                    signInError?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    signInSuccessMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF2E6C38).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = androidx.compose.ui.graphics.Color(0xFF2E6C38),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Accordeón / Información de configuración de Firebase
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTechnicalDetails = !showTechnicalDetails },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Datos de Firebase (control-tdc)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = if (showTechnicalDetails) "Ocultar" else "Ver SHA-1",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (showTechnicalDetails) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Proyecto: ${FirebaseInitializer.PROJECT_ID}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Paquete: ${FirebaseInitializer.PACKAGE_NAME}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("SHA-1 Debug:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(FirebaseInitializer.DEBUG_SHA1, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(FirebaseInitializer.DEBUG_SHA1))
                                        signInSuccessMessage = "¡SHA-1 copiado al portapapeles!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar SHA-1 para Firebase Console", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
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

/**
 * Mini-FAB con etiqueta a la izquierda, usado en el menú de acciones rápidas del FAB principal.
 */
@Composable
private fun QuickActionFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.testTag("fab_quick_${label.lowercase()}")
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}
