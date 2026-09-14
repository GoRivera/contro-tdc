package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CreditCard
import com.example.domain.CreditCardCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    cards: List<CreditCard>,
    initialCardId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (
        cardId: Long,
        concept: String,
        amount: Double,
        beneficiary: String,
        category: String,
        isMsi: Boolean,
        msiMonths: Int,
        notes: String
    ) -> Unit
) {
    var selectedCardId by remember { mutableLongStateOf(initialCardId ?: cards.firstOrNull()?.id ?: 1L) }
    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var beneficiary by remember { mutableStateOf("Personal") }
    var category by remember { mutableStateOf("Despensa") }
    var isMsi by remember { mutableStateOf(false) }
    var msiMonthsText by remember { mutableStateOf("12") }
    var notes by remember { mutableStateOf("") }

    val beneficiaries = listOf("Personal", "Memé", "Ale", "Poncho", "Familiar", "Otro")
    val categories = listOf("Despensa", "Servicios", "Tecnología", "Gasolina", "Restaurantes", "Salud", "Hogar", "Otros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Gasto o Compra", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto Total ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Card Selector
                Text("Tarjeta utilizada:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cards.forEach { card ->
                        FilterChip(
                            selected = selectedCardId == card.id,
                            onClick = { selectedCardId = card.id },
                            label = { Text(card.bank) }
                        )
                    }
                }

                // Beneficiary
                Text("Beneficiario:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    beneficiaries.take(3).forEach { ben ->
                        FilterChip(
                            selected = beneficiary == ben,
                            onClick = { beneficiary = ben },
                            label = { Text(ben) }
                        )
                    }
                }

                // Category
                Text("Categoría:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // MSI Checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMsi, onCheckedChange = { isMsi = it })
                    Text("¿Compra a Meses Sin Intereses (MSI)?")
                }

                if (isMsi) {
                    OutlinedTextField(
                        value = msiMonthsText,
                        onValueChange = { msiMonthsText = it },
                        label = { Text("Plazo en Meses (ej. 3, 6, 12, 18, 24)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val months = msiMonthsText.toIntOrNull() ?: 12
                    if (concept.isNotBlank() && amount > 0) {
                        onConfirm(selectedCardId, concept, amount, beneficiary, category, isMsi, months, notes)
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddPaymentDialog(
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (cardId: Long, concept: String, amount: Double, sourcePayer: String, notes: String) -> Unit
) {
    var selectedCardId by remember { mutableLongStateOf(cards.firstOrNull()?.id ?: 1L) }
    var concept by remember { mutableStateOf("Pago para no generar intereses") }
    var amountText by remember { mutableStateOf("") }
    var sourcePayer by remember { mutableStateOf("Personal") }
    var notes by remember { mutableStateOf("Transferencia SPEI") }

    val payers = listOf("Personal", "Memé", "Ale", "Poncho", "Banco")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abonar / Pagar Tarjeta", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto del Pago ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tarjeta a abonar:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cards.forEach { card ->
                        FilterChip(
                            selected = selectedCardId == card.id,
                            onClick = { selectedCardId = card.id },
                            label = { Text(card.bank) }
                        )
                    }
                }

                Text("Quién paga:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    payers.take(3).forEach { p ->
                        FilterChip(
                            selected = sourcePayer == p,
                            onClick = { sourcePayer = p },
                            label = { Text(p) }
                        )
                    }
                }

                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(selectedCardId, concept, amount, sourcePayer, notes)
                        onDismiss()
                    }
                }
            ) {
                Text("Registrar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        bank: String,
        cutoffDay: Int,
        paymentDueDay: Int,
        creditLimit: Double,
        primaryColorHex: String,
        secondaryColorHex: String,
        last4Digits: String,
        isDepartmental: Boolean,
        interestRate: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var cutoffDayText by remember { mutableStateOf("15") }
    var paymentDueDayText by remember { mutableStateOf("5") }
    var creditLimitText by remember { mutableStateOf("30000") }
    var last4Digits by remember { mutableStateOf("1234") }
    var isDepartmental by remember { mutableStateOf(false) }
    var interestRateText by remember { mutableStateOf("55.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarjeta", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Nombre de Tarjeta (ej. Oro, LikeU, Azul)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = { Text("Banco / Emisor (ej. BBVA, Santander, Nu)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cutoffDayText,
                        onValueChange = { cutoffDayText = it },
                        label = { Text("Día Corte") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paymentDueDayText,
                        onValueChange = { paymentDueDayText = it },
                        label = { Text("Día Límite") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = creditLimitText,
                    onValueChange = { creditLimitText = it },
                    label = { Text("Línea de Crédito ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = last4Digits,
                    onValueChange = { last4Digits = it },
                    label = { Text("Últimos 4 dígitos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDepartmental, onCheckedChange = { isDepartmental = it })
                    Text("Tarjeta departamental (ej. Liverpool)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cutoff = cutoffDayText.toIntOrNull() ?: 15
                    val due = paymentDueDayText.toIntOrNull() ?: 5
                    val limit = creditLimitText.toDoubleOrNull() ?: 30000.0
                    val rate = interestRateText.toDoubleOrNull() ?: 55.0
                    if (name.isNotBlank() && bank.isNotBlank()) {
                        onConfirm(
                            name, bank, cutoff, due, limit,
                            "#1E293B", "#0F172A", last4Digits, isDepartmental, rate
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SimulatorDialog(
    initialBalance: Double,
    annualRate: Double,
    onDismiss: () -> Unit
) {
    var balanceText by remember { mutableStateOf(initialBalance.toInt().toString()) }
    var rateText by remember { mutableStateOf(annualRate.toString()) }

    val balance = balanceText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 55.0
    val simulation = CreditCardCalculator.simulateMinimumPaymentPayoff(balance, rate)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "La Trampa del Pago Mínimo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFFEF4444)
                    )
                }

                Text(
                    text = "Al pagar únicamente el mínimo, la mayor parte se va al 16% de IVA e intereses bancarios.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Saldo Deudor ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Results Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tiempo para liquidar:", fontSize = 13.sp)
                            Text(
                                text = "${simulation.monthsToPayOff} meses (~${simulation.monthsToPayOff / 12} años)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Intereses pagados:", fontSize = 13.sp)
                            Text(
                                text = CreditCardCalculator.formatCurrency(simulation.totalInterestPaid),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IVA (16% sobre interés):", fontSize = 13.sp)
                            Text(
                                text = CreditCardCalculator.formatCurrency(simulation.totalIvaPaid),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total pagado:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = CreditCardCalculator.formatCurrency(simulation.totalAmountPaidWithMinimums),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ahorro pagando el total:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = CreditCardCalculator.formatCurrency(simulation.savingsPayingInFull),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
