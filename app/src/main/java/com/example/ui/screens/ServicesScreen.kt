package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntry
import com.example.data.model.ServiceType
import com.example.domain.CreditCardCalculator
import com.example.ui.util.rememberPrivacyCurrencyFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Pantalla del módulo de Servicios (agua, luz, gas). Es un control de consumos totalmente
 * independiente de las tarjetas de crédito: no genera cargos ni afecta ningún estado de cuenta.
 */
@Composable
fun ServicesScreen(
    entries: List<ServiceEntry>,
    onAddEntry: (serviceType: String, dateMillis: Long, amount: Double, consumption: Double, notes: String) -> Unit,
    onUpdateEntry: (entry: ServiceEntry, dateMillis: Long, amount: Double, consumption: Double, notes: String) -> Unit,
    onDeleteEntry: (ServiceEntry) -> Unit
) {
    val currencyFormat = rememberPrivacyCurrencyFormat()
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<ServiceEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<ServiceEntry?>(null) }

    val totalsByType = remember(entries) {
        ServiceType.ALL.associateWith { type -> entries.filter { it.serviceType == type }.sumOf { it.amount } }
    }
    val filteredEntries = remember(entries, selectedTypeFilter) {
        if (selectedTypeFilter == null) entries else entries.filter { it.serviceType == selectedTypeFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("services_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Servicios",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Control de agua, luz y gas — no afecta tus tarjetas",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_new_service_entry")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Tarjetas resumen por servicio: también funcionan como filtro (toca de nuevo para quitarlo)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ServiceType.ALL.forEach { type ->
                    val isSelected = selectedTypeFilter == type
                    val (icon, color) = serviceVisuals(type)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTypeFilter = if (isSelected) null else type },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ServiceType.displayName(type), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = currencyFormat.format(totalsByType[type] ?: 0.0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }

        if (filteredEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay registros de servicios. Toca \"Nuevo\" para agregar el primero.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredEntries, key = { it.id }) { entry ->
                ServiceEntryRow(
                    entry = entry,
                    currencyFormat = currencyFormat,
                    onEdit = { entryToEdit = entry },
                    onDelete = { entryToDelete = entry }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }

    if (showAddDialog) {
        AddEditServiceEntryDialog(
            entry = null,
            onDismiss = { showAddDialog = false },
            onSave = { type, dateMillis, amount, consumption, notes ->
                onAddEntry(type, dateMillis, amount, consumption, notes)
                showAddDialog = false
            }
        )
    }

    entryToEdit?.let { entry ->
        AddEditServiceEntryDialog(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onSave = { _, dateMillis, amount, consumption, notes ->
                onUpdateEntry(entry, dateMillis, amount, consumption, notes)
                entryToEdit = null
            }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("¿Eliminar este registro?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará el registro de ${ServiceType.displayName(entry.serviceType)} de forma permanente.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun serviceVisuals(type: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> = when (type) {
    ServiceType.AGUA -> Icons.Default.WaterDrop to Color(0xFF0288D1)
    ServiceType.LUZ -> Icons.Default.Bolt to Color(0xFFF9A825)
    else -> Icons.Default.LocalFireDepartment to Color(0xFFE64A19)
}

@Composable
private fun ServiceEntryRow(
    entry: ServiceEntry,
    currencyFormat: java.text.NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = serviceVisuals(entry.serviceType)
    val dateLabel = remember(entry.dateMillis, entry.serviceType) {
        if (entry.serviceType == ServiceType.GAS) {
            CreditCardCalculator.shortDateFormat.format(Date(entry.dateMillis))
        } else {
            SimpleDateFormat("MMMM yyyy", Locale("es", "MX")).format(Date(entry.dateMillis))
                .replaceFirstChar { it.uppercase() }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("service_entry_${entry.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${ServiceType.displayName(entry.serviceType)} • $dateLabel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (ServiceType.hasConsumption(entry.serviceType)) {
                    val consumptionLabel = if (entry.consumption % 1.0 == 0.0) {
                        entry.consumption.toLong().toString()
                    } else {
                        String.format(Locale.US, "%.2f", entry.consumption)
                    }
                    Text(
                        text = "$consumptionLabel ${ServiceType.consumptionUnit(entry.serviceType)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = currencyFormat.format(entry.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddEditServiceEntryDialog(
    entry: ServiceEntry?,
    onDismiss: () -> Unit,
    onSave: (serviceType: String, dateMillis: Long, amount: Double, consumption: Double, notes: String) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(entry?.serviceType ?: ServiceType.AGUA) }
    var selectedDateMillis by remember { mutableStateOf(entry?.dateMillis ?: System.currentTimeMillis()) }
    var amountText by remember { mutableStateOf(if (entry != null) entry.amount.toString() else "") }
    var consumptionText by remember { mutableStateOf(if (entry != null && entry.consumption > 0) entry.consumption.toString() else "") }
    var notesText by remember { mutableStateOf(entry?.notes ?: "") }

    val dateLabel = remember(selectedDateMillis, selectedType) {
        if (selectedType == ServiceType.GAS) {
            CreditCardCalculator.shortDateFormat.format(Date(selectedDateMillis))
        } else {
            SimpleDateFormat("MMMM yyyy", Locale("es", "MX")).format(Date(selectedDateMillis))
                .replaceFirstChar { it.uppercase() }
        }
    }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val consumptionValue = consumptionText.toDoubleOrNull() ?: 0.0
    val canSave = amountValue > 0.0 && (!ServiceType.hasConsumption(selectedType) || consumptionValue > 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Nuevo Registro de Servicio" else "Editar Registro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (entry == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ServiceType.ALL.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(ServiceType.displayName(type)) },
                                modifier = Modifier.testTag("service_type_$type")
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val chosen = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }
                                    selectedDateMillis = chosen.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (selectedType == ServiceType.GAS) "Fecha de carga" else "Mes al que corresponde",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(dateLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (ServiceType.hasConsumption(selectedType)) {
                    OutlinedTextField(
                        value = consumptionText,
                        onValueChange = { consumptionText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Consumo (${ServiceType.consumptionUnit(selectedType)})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("service_consumption_input")
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (selectedType == ServiceType.GAS) "Monto cargado ($)" else "Monto a pagar ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("service_amount_input")
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notas (opcional)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        onSave(selectedType, selectedDateMillis, amountValue, if (ServiceType.hasConsumption(selectedType)) consumptionValue else 0.0, notesText)
                    }
                },
                enabled = canSave,
                modifier = Modifier.testTag("save_service_entry_button")
            ) { Text("Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
