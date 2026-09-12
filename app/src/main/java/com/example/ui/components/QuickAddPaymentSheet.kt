package com.example.ui.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddPaymentSheet(
    cards: List<CreditCard>,
    defaultCardId: Long? = null,
    currentMonth: String = "Agosto",
    availableMonths: List<String> = listOf("Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre", "Enero", "Febrero"),
    onDismiss: () -> Unit,
    onSave: (
        cardId: Long,
        concept: String,
        amount: Double,
        sourcePayer: String,
        statementMonth: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            // Requisito 3: No permitir que al desplazar hacia abajo se descarte el registro de abono
            sheetValue != SheetValue.Hidden
        }
    )

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isKeyboardVisible = WindowInsets.isImeVisible

    var concept by remember { mutableStateOf("Pago TDC") }
    var amountText by remember { mutableStateOf("") }
    var selectedCardId by remember {
        mutableStateOf(defaultCardId ?: cards.firstOrNull()?.id ?: 0L)
    }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedPayer by remember { mutableStateOf("Personal") }

    // Conceptos sugeridos de abonos editables (posiciones, editar, eliminar, agregar)
    var quickConcepts by remember {
        mutableStateOf(listOf("Pago TDC", "Bonificación", "Devolución / Reembolso", "Abono Terceros", "Abono Familiar"))
    }
    var showManageConceptsDialog by remember { mutableStateOf(false) }

    // Personas que abonan editables (editar, eliminar, agregar, reordenar)
    var payersList by remember {
        mutableStateOf(listOf("Personal", "Familiar", "Pareja", "Banco", "Empresa"))
    }
    var showManagePayersDialog by remember { mutableStateOf(false) }

    // Diálogo de advertencia de descarte
    var showDiscardPaymentDialog by remember { mutableStateOf(false) }
    val hasUnsavedPayment = amountText.isNotBlank()
    val handleDismissAttempt = {
        if (hasUnsavedPayment) {
            showDiscardPaymentDialog = true
        } else {
            onDismiss()
        }
    }

    // Requisito 1: Al dar en el botón atrás con el teclado abierto, solo ocultar el teclado sin descartar la edición
    BackHandler(enabled = true) {
        if (isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            handleDismissAttempt()
        }
    }

    ModalBottomSheet(
        onDismissRequest = handleDismissAttempt,
        sheetState = sheetState,
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_add_payment_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Registrar Abono / Pago a Tarjeta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Añade pagos para liquidar o bonificaciones bancarias",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = handleDismissAttempt) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar registro de abono",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto del Abono / Pago") },
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
                    .testTag("payment_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Concept Input
            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                label = { Text("Concepto del Abono") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_concept_input")
            )

            // Header para conceptos sugeridos con botón de administración
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

            // Quick Chips
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

            // Card Selector
            Text(
                text = "Tarjeta Destino",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
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
                        onClick = { selectedCardId = card.id },
                        leadingIcon = {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(card.primaryColorHex),
                                modifier = Modifier.size(14.dp)
                            ) {}
                        },
                        label = { Text(card.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Statement Month
            Text(
                text = "Periodo a abonar",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableMonths.forEach { m ->
                    FilterChip(
                        selected = m == selectedMonth,
                        onClick = { selectedMonth = m },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(m) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Source Payer con botón de administración de personas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Quién realiza el abono?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                IconButton(
                    onClick = { showManagePayersDialog = true },
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
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                payersList.forEach { p ->
                    FilterChip(
                        selected = p == selectedPayer,
                        onClick = { selectedPayer = p },
                        label = { Text(p) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val amountValue = amountText.toDoubleOrNull() ?: 0.0
            val canSave = concept.isNotBlank() && amountValue > 0.0 && selectedCardId != 0L

            Button(
                onClick = {
                    if (canSave) {
                        onSave(
                            selectedCardId,
                            concept,
                            amountValue,
                            selectedPayer,
                            selectedMonth
                        )
                        onDismiss()
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_payment_button")
            ) {
                Text(
                    text = "Registrar Abono",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    // Diálogo para administrar conceptos sugeridos de abono (editar, reordenar, eliminar, agregar)
    if (showManageConceptsDialog) {
        ManageItemsDialog(
            title = "Conceptos de Abono",
            subtitle = "Agrega, edita, elimina o reordena los conceptos sugeridos",
            items = quickConcepts,
            itemLabel = "Concepto de abono",
            onDismiss = { showManageConceptsDialog = false },
            onSaveList = { updatedList ->
                quickConcepts = updatedList
                if (!updatedList.contains(concept) && updatedList.isNotEmpty()) {
                    concept = updatedList.first()
                }
            }
        )
    }

    // Diálogo para administrar personas que realizan abonos (editar, reordenar, eliminar, agregar)
    if (showManagePayersDialog) {
        ManageItemsDialog(
            title = "Personas y Fuentes de Abono",
            subtitle = "Administra las personas disponibles para realizar pagos",
            items = payersList,
            itemLabel = "Persona / Fuente",
            onDismiss = { showManagePayersDialog = false },
            onSaveList = { updatedList ->
                payersList = updatedList
                if (!updatedList.contains(selectedPayer) && updatedList.isNotEmpty()) {
                    selectedPayer = updatedList.first()
                }
            }
        )
    }

    // Diálogo de confirmación antes de descartar abono
    if (showDiscardPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardPaymentDialog = false },
            title = { Text("¿Deseas descartar el abono?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Aún no has registrado este abono. Si sales ahora, los datos introducidos se perderán."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardPaymentDialog = false
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
                androidx.compose.material3.TextButton(
                    onClick = { showDiscardPaymentDialog = false }
                ) {
                    Text("Seguir editando", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}
