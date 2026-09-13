package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.domain.CreditCardCalculator
import com.example.ui.util.LocalPrivacyMode
import com.example.ui.util.PrivacyFormat
import java.text.NumberFormat
import java.util.Locale

private sealed class SearchResultItem(val dateMillis: Long, val amount: Double) {
    class ExpenseResult(val expense: Expense) : SearchResultItem(expense.dateMillis, expense.amount)
    class PaymentResult(val payment: Payment) : SearchResultItem(payment.dateMillis, payment.amount)
}

/**
 * Buscador global de gastos y abonos por concepto, categoría, beneficiario/origen o monto. Antes
 * solo existían filtros por pantalla (por tarjeta, por mes); esto permite encontrar un registro
 * concreto sin recordar en qué mes o pantalla se capturó.
 */
@Composable
fun SearchScreen(
    expenses: List<Expense>,
    payments: List<Payment>,
    cards: List<CreditCard>,
    onClose: () -> Unit
) {
    val isPrivate = LocalPrivacyMode.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val cardMap = remember(cards) { cards.associateBy { it.id } }
    var query by remember { mutableStateOf("") }

    val results: List<SearchResultItem> = remember(query, expenses, payments) {
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            emptyList()
        } else {
            val expenseMatches = expenses.filter {
                it.concept.lowercase().contains(q) ||
                    it.beneficiary.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.notes.lowercase().contains(q) ||
                    it.amount.toString().contains(q)
            }.map { SearchResultItem.ExpenseResult(it) }

            val paymentMatches = payments.filter {
                it.concept.lowercase().contains(q) ||
                    it.sourcePayer.lowercase().contains(q) ||
                    it.notes.lowercase().contains(q) ||
                    it.amount.toString().contains(q)
            }.map { SearchResultItem.PaymentResult(it) }

            (expenseMatches + paymentMatches).sortedByDescending { it.dateMillis }
        }
    }

    Column(modifier = Modifier.fillMaxSize().testTag("search_screen")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Buscar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Concepto, categoría, persona o monto...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (query.isBlank()) {
                item {
                    Text(
                        text = "Escribe para buscar en todos tus gastos y abonos registrados.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                    )
                }
            } else if (results.isEmpty()) {
                item {
                    Text(
                        text = "Sin resultados para \"$query\".",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                    )
                }
            } else {
                items(results) { result ->
                    when (result) {
                        is SearchResultItem.ExpenseResult -> {
                            val exp = result.expense
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exp.concept, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = "${cardMap[exp.cardId]?.name ?: "TDC"} • ${exp.category} • ${exp.beneficiary} • ${CreditCardCalculator.formatDate(exp.dateMillis)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = PrivacyFormat.format(exp.amount, isPrivate, currencyFormat),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        is SearchResultItem.PaymentResult -> {
                            val pay = result.payment
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pay.concept, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = "${cardMap[pay.cardId]?.name ?: "TDC"} • ${pay.sourcePayer} • ${CreditCardCalculator.formatDate(pay.dateMillis)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = PrivacyFormat.format(pay.amount, isPrivate, currencyFormat),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
