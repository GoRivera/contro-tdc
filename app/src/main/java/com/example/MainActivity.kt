package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.AppViewModel
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.ControlTDCTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlTDCTheme {
                val currentTab by viewModel.selectedTab.collectAsState()
                val cards by viewModel.cards.collectAsState()

                // Dialog states
                var showAddExpenseDialog by remember { mutableStateOf(false) }
                var selectedExpenseCardId by remember { mutableStateOf<Long?>(null) }

                var showAddPaymentDialog by remember { mutableStateOf(false) }
                var showAddCardDialog by remember { mutableStateOf(false) }

                var showSimulatorDialog by remember { mutableStateOf(false) }
                var simBalance by remember { mutableDoubleStateOf(10000.0) }
                var simRate by remember { mutableDoubleStateOf(55.0) }

                var showAddSubDialog by remember { mutableStateOf(false) }
                var showAddFuelDialog by remember { mutableStateOf(false) }
                var showAddServiceDialog by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
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
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Control TDC",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = "Finanzas Inteligentes México",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "G. RIVERA",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == "recommendation",
                                onClick = { viewModel.selectTab("recommendation") },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                                label = { Text("Recomienda") }
                            )
                            NavigationBarItem(
                                selected = currentTab == "cards",
                                onClick = { viewModel.selectTab("cards") },
                                icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                label = { Text("Tarjetas") }
                            )
                            NavigationBarItem(
                                selected = currentTab == "msi",
                                onClick = { viewModel.selectTab("msi") },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("MSI") }
                            )
                            NavigationBarItem(
                                selected = currentTab == "statements",
                                onClick = { viewModel.selectTab("statements") },
                                icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                                label = { Text("Estado") }
                            )
                            NavigationBarItem(
                                selected = currentTab == "more",
                                onClick = { viewModel.selectTab("more") },
                                icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
                                label = { Text("Fijos") }
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                selectedExpenseCardId = null
                                showAddExpenseDialog = true
                            },
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nuevo Gasto")
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            "recommendation" -> {
                                RecommendationScreen(
                                    viewModel = viewModel,
                                    onOpenAddExpense = { cardId ->
                                        selectedExpenseCardId = cardId
                                        showAddExpenseDialog = true
                                    }
                                )
                            }
                            "cards" -> {
                                CardsScreen(
                                    viewModel = viewModel,
                                    onOpenAddCard = { showAddCardDialog = true },
                                    onOpenSimulator = { balance, rate ->
                                        simBalance = balance
                                        simRate = rate
                                        showSimulatorDialog = true
                                    }
                                )
                            }
                            "msi" -> {
                                MsiScreen(viewModel = viewModel)
                            }
                            "statements" -> {
                                StatementsScreen(
                                    viewModel = viewModel,
                                    onOpenAddPayment = { showAddPaymentDialog = true }
                                )
                            }
                            "more" -> {
                                MoreScreen(
                                    viewModel = viewModel,
                                    onOpenAddSubscription = { showAddSubDialog = true },
                                    onOpenAddFuel = { showAddFuelDialog = true },
                                    onOpenAddService = { showAddServiceDialog = true }
                                )
                            }
                        }
                    }
                }

                // Dialogs
                if (showAddExpenseDialog) {
                    AddExpenseDialog(
                        cards = cards,
                        initialCardId = selectedExpenseCardId,
                        onDismiss = { showAddExpenseDialog = false },
                        onConfirm = { cardId, concept, amount, beneficiary, category, isMsi, msiMonths, notes ->
                            viewModel.addExpense(cardId, concept, amount, beneficiary, category, isMsi, msiMonths, notes)
                        }
                    )
                }

                if (showAddPaymentDialog) {
                    AddPaymentDialog(
                        cards = cards,
                        onDismiss = { showAddPaymentDialog = false },
                        onConfirm = { cardId, concept, amount, sourcePayer, notes ->
                            viewModel.addPayment(cardId, concept, amount, sourcePayer, notes)
                        }
                    )
                }

                if (showAddCardDialog) {
                    AddCardDialog(
                        onDismiss = { showAddCardDialog = false },
                        onConfirm = { name, bank, cutoff, due, limit, pColor, sColor, last4, isDept, rate ->
                            viewModel.addCard(name, bank, cutoff, due, limit, pColor, sColor, last4, isDept, rate)
                        }
                    )
                }

                if (showSimulatorDialog) {
                    SimulatorDialog(
                        initialBalance = simBalance,
                        annualRate = simRate,
                        onDismiss = { showSimulatorDialog = false }
                    )
                }

                if (showAddSubDialog) {
                    AddSubscriptionDialog(
                        cards = cards,
                        onDismiss = { showAddSubDialog = false },
                        onConfirm = { name, cardId, day, amount, cat, part ->
                            viewModel.addSubscription(name, cardId, day, amount, cat, part)
                        }
                    )
                }

                if (showAddFuelDialog) {
                    AddFuelDialog(
                        cards = cards,
                        onDismiss = { showAddFuelDialog = false },
                        onConfirm = { cardId, km, price, liters, type, div, withWho ->
                            viewModel.addFuelEntry(cardId, km, price, liters, type, div, withWho)
                        }
                    )
                }

                if (showAddServiceDialog) {
                    AddServiceDialog(
                        onDismiss = { showAddServiceDialog = false },
                        onConfirm = { type, amount, cons, notes ->
                            viewModel.addServiceEntry(type, amount, cons, notes, 1L)
                        }
                    )
                }
            }
        }
    }
}
