package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard

@Composable
fun AddSubscriptionDialog(
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, cardId: Long, billingDay: Int, monthlyAmount: Double, category: String, participants: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCardId by remember { mutableLongStateOf(cards.firstOrNull()?.id ?: 1L) }
    var billingDayText by remember { mutableStateOf("15") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Streaming") }
    var participants by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Suscripción", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Disney+, Spotify)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto Mensual ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = billingDayText,
                    onValueChange = { billingDayText = it },
                    label = { Text("Día de Cobro (1-31)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Participantes / División de costos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val day = billingDayText.toIntOrNull() ?: 15
                    if (name.isNotBlank() && amount > 0) {
                        onConfirm(name, selectedCardId, day, amount, category, participants)
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
fun AddFuelDialog(
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (cardId: Long, kmDriven: Double, pricePerLiter: Double, litersLoaded: Double, fuelType: String, isDivided: Boolean, dividedWith: String) -> Unit
) {
    var selectedCardId by remember { mutableLongStateOf(cards.firstOrNull()?.id ?: 1L) }
    var kmText by remember { mutableStateOf("450") }
    var priceText by remember { mutableStateOf("25.50") }
    var litersText by remember { mutableStateOf("40") }
    var fuelType by remember { mutableStateOf("Premium (Roja)") }
    var isDivided by remember { mutableStateOf(false) }
    var dividedWith by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registro de Gasolina", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it },
                    label = { Text("Kilómetros recorridos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = litersText,
                    onValueChange = { litersText = it },
                    label = { Text("Litros cargados") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio por litro ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDivided, onCheckedChange = { isDivided = it })
                    Text("¿Gasto dividido con alguien más?")
                }
                if (isDivided) {
                    OutlinedTextField(
                        value = dividedWith,
                        onValueChange = { dividedWith = it },
                        label = { Text("¿Con quién? (ej. Ale)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmText.toDoubleOrNull() ?: 0.0
                    val liters = litersText.toDoubleOrNull() ?: 0.0
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (km > 0 && liters > 0) {
                        onConfirm(selectedCardId, km, price, liters, fuelType, isDivided, dividedWith)
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar Carga")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (serviceType: String, amount: Double, consumption: Double, notes: String) -> Unit
) {
    var serviceType by remember { mutableStateOf("CFE (Luz)") }
    var amountText by remember { mutableStateOf("") }
    var consumptionText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val services = listOf("CFE (Luz)", "Agua y Drenaje", "Gas Natural", "Internet / Teléfono")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Recibo de Servicio", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tipo de Servicio:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    services.take(2).forEach { s ->
                        FilterChip(
                            selected = serviceType == s,
                            onClick = { serviceType = s },
                            label = { Text(s) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto a Pagar ($ MXN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = consumptionText,
                    onValueChange = { consumptionText = it },
                    label = { Text("Lectura / Consumo (kWh / m³)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Detalle o periodo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val consumption = consumptionText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(serviceType, amount, consumption, notes)
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
