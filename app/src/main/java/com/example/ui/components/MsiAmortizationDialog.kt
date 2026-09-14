package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.domain.CreditCardCalculator
import com.example.domain.MsiSummary
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

data class AmortizationRow(
    val installmentNumber: Int,
    val monthYearLabel: String,
    val paymentAmount: Double,
    val remainingBalanceAfter: Double,
    val status: InstallmentStatus
)

enum class InstallmentStatus {
    PAID,
    CURRENT,
    OVERDUE, // El corte ya pasó (se cargó a la tarjeta) pero el estado de cuenta no se ha liquidado
    PENDING
}

@Composable
fun MsiAmortizationDialog(
    msiSummary: MsiSummary,
    allExpenses: List<Expense> = emptyList(),
    allPayments: List<Payment> = emptyList(),
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }
    val exp = msiSummary.expense
    val totalMonths = exp.msiTotalMonths.coerceAtLeast(1)
    val currentInst = exp.msiCurrentInstallment.coerceIn(0, totalMonths)
    val totalPurchase = if (msiSummary.totalPurchaseAmount > 0) {
        msiSummary.totalPurchaseAmount
    } else {
        msiSummary.monthlyPayment * totalMonths
    }

    // Calcular las filas de la tabla de amortización
    val rows = remember(msiSummary, allExpenses, allPayments) {
        val baseCal = CreditCardCalculator.getMsiBaseCalendar(exp)
        (1..totalMonths).map { instNum ->
            val rowCal = baseCal.clone() as Calendar
            rowCal.add(Calendar.MONTH, instNum - currentInst)
            val monthLabel = CreditCardCalculator.monthFormat.format(rowCal.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val remaining = (totalPurchase - (msiSummary.monthlyPayment * instNum)).coerceAtLeast(0.0)
            val status = when {
                instNum > currentInst -> InstallmentStatus.PENDING
                instNum == currentInst -> {
                    val settled = CreditCardCalculator.isStatementPeriodSettled(
                        cardId = exp.cardId,
                        year = rowCal.get(Calendar.YEAR),
                        monthName = monthLabel,
                        expenses = allExpenses,
                        payments = allPayments
                    )
                    if (settled) {
                        InstallmentStatus.PAID
                    } else {
                        val card = msiSummary.card
                        val isOverdue = if (card != null) {
                            val dueDate = CreditCardCalculator.calculatePaymentDueDateForCutoff(
                                card = card,
                                cutoffYear = rowCal.get(Calendar.YEAR),
                                cutoffMonth = rowCal.get(Calendar.MONTH)
                            )
                            Calendar.getInstance().time.after(dueDate)
                        } else {
                            false
                        }
                        if (isOverdue) InstallmentStatus.OVERDUE else InstallmentStatus.CURRENT
                    }
                }
                else -> {
                    // El corte de esta cuota ya pasó (por eso avanzó "currentInst"), pero eso no
                    // significa que el usuario ya haya liquidado ese estado de cuenta. Solo se marca
                    // "Pagado" si los pagos registrados para ese corte cubren los cargos.
                    val hasChargesRecorded = allExpenses.any {
                        it.cardId == exp.cardId &&
                            CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == rowCal.get(Calendar.YEAR) &&
                            CreditCardCalculator.normalizeMonth(it.targetStatementMonth) == monthLabel
                    }
                    if (hasChargesRecorded) {
                        val settled = CreditCardCalculator.isStatementPeriodSettled(
                            cardId = exp.cardId,
                            year = rowCal.get(Calendar.YEAR),
                            monthName = monthLabel,
                            expenses = allExpenses,
                            payments = allPayments
                        )
                        if (settled) InstallmentStatus.PAID else InstallmentStatus.OVERDUE
                    } else {
                        // Mensualidades anteriores a los registros del usuario en la app (histórico liquidado)
                        InstallmentStatus.PAID
                    }
                }
            }

            AmortizationRow(
                installmentNumber = instNum,
                monthYearLabel = monthLabel,
                paymentAmount = msiSummary.monthlyPayment,
                remainingBalanceAfter = remaining,
                status = status
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("msi_amortization_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TABLA DE AMORTIZACIÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = exp.concept,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Tarjeta", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${msiSummary.cardName} (${msiSummary.card?.bank ?: "TDC"})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monto Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = currencyFormat.format(totalPurchase),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Mensualidad", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${currencyFormat.format(msiSummary.monthlyPayment)} / mes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Plazo & Avance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Mes $currentInst de $totalMonths",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Barra de progreso y banner de estado actual
                        LinearProgressIndicator(
                            progress = { if (totalMonths > 0) currentInst.toFloat() / totalMonths else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Indicador visual de la posición actual en la deuda
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentInst >= totalMonths) {
                                    "Plan 100% liquidado. No hay saldo pendiente."
                                } else {
                                    "Posición actual: Cuota $currentInst de $totalMonths (Restan ${currencyFormat.format(msiSummary.remainingBalance)})"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table Header
                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "N°",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = "Periodo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = "Cuota",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = "Saldo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = "Estado",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                // Table Rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                        )
                ) {
                    itemsIndexed(rows) { index, row ->
                        val isCurrent = row.status == InstallmentStatus.CURRENT
                        val isPaid = row.status == InstallmentStatus.PAID

                        val rowBackground = when {
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            index % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBackground)
                                .then(
                                    if (isCurrent) Modifier.border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Installment number
                            Text(
                                text = "${row.installmentNumber}",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(30.dp)
                            )

                            // Month label
                            Text(
                                text = row.monthYearLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1.3f)
                            )

                            // Payment amount
                            Text(
                                text = currencyFormat.format(row.paymentAmount),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1.1f)
                            )

                            // Remaining balance after payment
                            Text(
                                text = currencyFormat.format(row.remainingBalanceAfter),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.1f)
                            )

                            // Status pill / badge
                            Box(
                                modifier = Modifier.weight(1.2f),
                                contentAlignment = Alignment.Center
                            ) {
                                when (row.status) {
                                    InstallmentStatus.PAID -> {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFD8ECD5)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2E6C38),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Pagado",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E6C38)
                                                )
                                            }
                                        }
                                    }

                                    InstallmentStatus.CURRENT -> {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "ACTUAL",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    InstallmentStatus.OVERDUE -> {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.errorContainer
                                        ) {
                                            Text(
                                                text = "No liquidado",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    InstallmentStatus.PENDING -> {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "Pendiente",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (index < rows.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onEdit != null) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Editar MSI", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
