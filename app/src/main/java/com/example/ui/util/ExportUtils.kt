package com.example.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Exporta el historial de gastos y pagos a un archivo CSV y lo comparte con otras apps (correo,
 * Drive, WhatsApp, etc.) mediante un FileProvider. Útil para llevar cuentas o compartir con un
 * contador, sin depender de la sincronización con la nube.
 */
object ExportUtils {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private fun csvEscape(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    fun buildCsv(expenses: List<Expense>, payments: List<Payment>, cards: List<CreditCard>): String {
        val cardMap = cards.associateBy { it.id }
        val sb = StringBuilder()
        sb.append("Tipo,Fecha,Tarjeta,Concepto,Categoría/Origen,Beneficiario,Monto,Corte,MSI\n")

        expenses.sortedByDescending { it.dateMillis }.forEach { exp ->
            val cardName = cardMap[exp.cardId]?.name ?: "TDC"
            val msiInfo = if (exp.isMsi) "Cuota ${exp.msiCurrentInstallment} de ${exp.msiTotalMonths}" else ""
            sb.append(
                listOf(
                    "Gasto",
                    dateFormat.format(exp.dateMillis),
                    cardName,
                    exp.concept,
                    exp.category,
                    exp.beneficiary,
                    String.format(Locale.US, "%.2f", exp.amount),
                    exp.targetStatementMonth,
                    msiInfo
                ).joinToString(",") { csvEscape(it) }
            )
            sb.append("\n")
        }

        payments.sortedByDescending { it.dateMillis }.forEach { pay ->
            val cardName = cardMap[pay.cardId]?.name ?: "TDC"
            sb.append(
                listOf(
                    "Abono",
                    dateFormat.format(pay.dateMillis),
                    cardName,
                    pay.concept,
                    "",
                    pay.sourcePayer,
                    String.format(Locale.US, "%.2f", pay.amount),
                    pay.targetStatementMonth,
                    ""
                ).joinToString(",") { csvEscape(it) }
            )
            sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Genera el CSV, lo guarda en la carpeta de caché de la app y devuelve un Intent para
     * compartirlo. El archivo se sobrescribe en cada exportación (no se acumulan copias viejas).
     */
    fun shareExpensesAndPaymentsCsv(
        context: Context,
        expenses: List<Expense>,
        payments: List<Payment>,
        cards: List<CreditCard>
    ): Intent {
        val csvContent = buildCsv(expenses, payments, cards)
        val exportsDir = File(context.cacheDir, "csv_exports").apply { mkdirs() }
        val file = File(exportsDir, "control_tdc_historial.csv")
        FileOutputStream(file).use { it.write(csvContent.toByteArray()) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
