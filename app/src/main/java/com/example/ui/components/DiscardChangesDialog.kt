package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Diálogo de confirmación al intentar cerrar un formulario de registro (gasto, abono, etc.) sin
 * guardar. Antes vivía casi duplicado, palabra por palabra, en QuickAddExpenseSheet y
 * QuickAddPaymentSheet — solo cambiaba el sustantivo ("gasto" vs "abono").
 */
@Composable
fun DiscardChangesDialog(
    itemLabel: String,
    onDismissRequest: () -> Unit,
    onConfirmDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("¿Deseas descartar el $itemLabel?", fontWeight = FontWeight.Bold) },
        text = {
            Text("Aún no has guardado este $itemLabel. Si sales ahora, los datos introducidos se perderán.")
        },
        confirmButton = {
            Button(
                onClick = onConfirmDiscard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Descartar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Seguir editando", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
