package com.example.domain

import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class TrafficLightStatus(
    val title: String,
    val subtitle: String,
    val colorHex: Long,
    val containerColorHex: Long
) {
    GREEN(
        title = "Óptima hoy",
        subtitle = "Máximo financiamiento sin intereses",
        colorHex = 0xFF2E6C38, // Emerald Green
        containerColorHex = 0xFFD8ECD5
    ),
    YELLOW(
        title = "Moderada",
        subtitle = "Financiamiento intermedio",
        colorHex = 0xFF856404, // Warm Amber
        containerColorHex = 0xFFFFF3CD
    ),
    RED(
        title = "No recomendada hoy",
        subtitle = "Corte próximo; pagarás casi de inmediato",
        colorHex = 0xFFB3261E, // Coral Red
        containerColorHex = 0xFFF9DEDC
    )
}

data class CardRecommendation(
    val card: CreditCard,
    val daysOfFinancing: Int,
    val nextCutoffDate: Date,
    val paymentDueDate: Date,
    val daysUntilCutoff: Int,
    val isBestOption: Boolean,
    val recommendationReason: String,
    val trafficLight: TrafficLightStatus = TrafficLightStatus.GREEN,
    val isPaymentShiftedByHoliday: Boolean = false
)

data class CardCreditBalance(
    val creditLimit: Double,
    val totalOccupiedCredit: Double,
    val availableCredit: Double,
    val usedRatio: Float,
    val regularDebt: Double,
    val totalMsiPendingBalance: Double,
    val currentCycleMsiCharge: Double,
    val totalPayments: Double
)

data class MsiAutoTimeline(
    val firstChargeMonth: String,       // e.g. "Octubre 2025"
    val firstChargeYear: Int,
    val firstChargeMonthIndex: Int,     // 0-11
    val currentInstallment: Int,        // 1..totalMonths (cuota correspondiente al corte actual)
    val remainingInstallments: Int,     // cuotas futuras por liquidar
    val isCompleted: Boolean,
    val targetStatementMonth: String,   // e.g. "Septiembre 2026"
    val explanation: String
)

data class MinimumPaymentSimulationResult(
    val balance: Double,
    val annualInterestRate: Double,
    val firstMonthMinimumPayment: Double,
    val monthsToPayOff: Int,
    val totalInterestPaid: Double,
    val totalIvaPaid: Double,
    val totalAmountPaidWithMinimums: Double,
    val savingsPayingInFull: Double,
    val monthlyAmortizationSample: List<AmortizationMonth>
)

data class AmortizationMonth(
    val month: Int,
    val startBalance: Double,
    val interest: Double,
    val iva: Double,
    val minimumPayment: Double,
    val principalPaid: Double,
    val endBalance: Double
)

object MexicanBankingCalendar {

    /**
     * Días inhábiles oficiales del sector financiero en México conforme a la CNBV y Banxico.
     */
    fun isMexicanBankingHoliday(cal: Calendar): Boolean {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return true
        }

        val month = cal.get(Calendar.MONTH) // 0-indexed
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
        val year = cal.get(Calendar.YEAR)

        // 1 de enero: Año Nuevo
        if (month == Calendar.JANUARY && dayOfMonth == 1) return true

        // Primer lunes de febrero (Día de la Constitución)
        if (month == Calendar.FEBRUARY && dayOfWeek == Calendar.MONDAY && dayOfWeekInMonth == 1) return true

        // Tercer lunes de marzo (Natalicio de Benito Juárez)
        if (month == Calendar.MARCH && dayOfWeek == Calendar.MONDAY && dayOfWeekInMonth == 3) return true

        // Jueves y Viernes Santo
        val holyDates = getHolyThursdayAndGoodFriday(year)
        if (holyDates.any { it.first == month && it.second == dayOfMonth }) return true

        // 1 de mayo: Día del Trabajo
        if (month == Calendar.MAY && dayOfMonth == 1) return true

        // 16 de septiembre: Día de la Independencia
        if (month == Calendar.SEPTEMBER && dayOfMonth == 16) return true

        // 1 de octubre: Transmisión del Poder Ejecutivo Federal (2024, 2030...)
        if (month == Calendar.OCTOBER && dayOfMonth == 1 && ((year - 2024) % 6 == 0)) return true

        // 2 de noviembre: Día de Muertos
        if (month == Calendar.NOVEMBER && dayOfMonth == 2) return true

        // Tercer lunes de noviembre (Revolución Mexicana)
        if (month == Calendar.NOVEMBER && dayOfWeek == Calendar.MONDAY && dayOfWeekInMonth == 3) return true

        // 12 de diciembre: Día del Empleado Bancario
        if (month == Calendar.DECEMBER && dayOfMonth == 12) return true

        // 25 de diciembre: Navidad
        if (month == Calendar.DECEMBER && dayOfMonth == 25) return true

        return false
    }

    private fun getHolyThursdayAndGoodFriday(year: Int): List<Pair<Int, Int>> {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val easterMonth = (h + l - 7 * m + 114) / 31 - 1
        val easterDay = ((h + l - 7 * m + 114) % 31) + 1

        val easterCal = Calendar.getInstance().apply {
            set(year, easterMonth, easterDay, 12, 0, 0)
        }

        val goodFridayCal = (easterCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -2) }
        val holyThursdayCal = (easterCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -3) }

        return listOf(
            Pair(holyThursdayCal.get(Calendar.MONTH), holyThursdayCal.get(Calendar.DAY_OF_MONTH)),
            Pair(goodFridayCal.get(Calendar.MONTH), goodFridayCal.get(Calendar.DAY_OF_MONTH))
        )
    }

    /**
     * Aplica la regla del artículo 23 de la Ley para la Transparencia y Ordenamiento de los Servicios Financieros:
     * Si la fecha límite de pago es inhábil bancario, el pago se efectúa válidamente el siguiente día hábil bancario.
     */
    fun getEffectivePaymentDueDate(rawDueDate: Date): Pair<Date, Boolean> {
        val cal = Calendar.getInstance().apply { time = rawDueDate }
        var shifted = false
        while (isMexicanBankingHoliday(cal)) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            shifted = true
        }
        return Pair(cal.time, shifted)
    }
}

data class MsiSummary(
    val expense: Expense,
    val cardName: String,
    val card: CreditCard? = null,
    val monthlyPayment: Double,
    val totalPaidSoFar: Double,
    val remainingBalance: Double,
    val installmentsRemaining: Int,
    val progressPercent: Float,
    val completionDateString: String,
    val willFinishInMonths: Int,
    val totalPurchaseAmount: Double = 0.0
)

data class CashFlowRelease(
    val monthYearLabel: String,
    val monthlyAmountFreed: Double,
    val finishingItems: List<String>
)

data class StatementSummary(
    val statementMonth: String, // "2026-08"
    val cardId: Long?,          // null for consolidated
    val cardName: String,
    val cutoffDateString: String,
    val paymentDueDateString: String,
    val totalCharges: Double,
    val totalPayments: Double,
    val remainingBalance: Double,
    val isFullyPaid: Boolean,
    val beneficiaryBreakdown: Map<String, Double>
)

object CreditCardCalculator {

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
    val dayMonthFormat = SimpleDateFormat("d 'de' MMMM", Locale("es", "MX"))
    val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Calculates the next cut-off date and payment due date for a card relative to a target date.
     * Incorporates Mexican banking non-working days (Banxico/CNBV rule): shifts to next business day if due date is a weekend/holiday.
     */
    fun calculateCycleDates(card: CreditCard, referenceDate: Date = Date()): Pair<Date, Date> {
        val (cutoff, effectiveDue, _) = calculateCycleDatesDetailed(card, referenceDate)
        return Pair(cutoff, effectiveDue)
    }

    /**
     * Returns cutoff date, effective payment due date (shifted by bank holiday if needed), and whether it was shifted.
     */
    fun calculateCycleDatesDetailed(card: CreditCard, referenceDate: Date = Date()): Triple<Date, Date, Boolean> {
        val cal = Calendar.getInstance()
        cal.time = referenceDate

        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val cutoffCal = Calendar.getInstance()
        cutoffCal.set(Calendar.HOUR_OF_DAY, 23)
        cutoffCal.set(Calendar.MINUTE, 59)
        cutoffCal.set(Calendar.SECOND, 59)
        cutoffCal.set(Calendar.MILLISECOND, 999)

        if (currentDay <= card.cutoffDay) {
            // Cutoff is this month (properly handles leap years and 28/29/30/31 days)
            val maxDayThisMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualCutoffDay = card.cutoffDay.coerceAtMost(maxDayThisMonth)
            cutoffCal.set(currentYear, currentMonth, actualCutoffDay)
        } else {
            // Cutoff is next month
            cal.add(Calendar.MONTH, 1)
            val nextMonth = cal.get(Calendar.MONTH)
            val nextYear = cal.get(Calendar.YEAR)
            val maxDayNextMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualCutoffDay = card.cutoffDay.coerceAtMost(maxDayNextMonth)
            cutoffCal.set(nextYear, nextMonth, actualCutoffDay)
        }

        val cutoffDate = cutoffCal.time

        // Payment due date: typically ~20 days after cutoff
        val paymentCal = Calendar.getInstance()
        paymentCal.time = cutoffDate
        paymentCal.set(Calendar.HOUR_OF_DAY, 23)
        paymentCal.set(Calendar.MINUTE, 59)
        paymentCal.set(Calendar.SECOND, 59)

        if (card.paymentDueDay > card.cutoffDay) {
            // Payment due day is in the same calendar month as cut-off
            val maxDay = paymentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualDueDay = card.paymentDueDay.coerceAtMost(maxDay)
            paymentCal.set(Calendar.DAY_OF_MONTH, actualDueDay)
        } else {
            // Payment due day is in the following month after cut-off
            paymentCal.add(Calendar.MONTH, 1)
            val maxDay = paymentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualDueDay = card.paymentDueDay.coerceAtMost(maxDay)
            paymentCal.set(Calendar.DAY_OF_MONTH, actualDueDay)
        }

        val (effectiveDueDate, wasShifted) = MexicanBankingCalendar.getEffectivePaymentDueDate(paymentCal.time)
        return Triple(cutoffDate, effectiveDueDate, wasShifted)
    }

    /**
     * Calcula la línea de crédito ocupada y disponible real de una tarjeta.
     * Considera:
     * 1) Los gastos regulares no-MSI menos los pagos realizados.
     * 2) El saldo TOTAL pendiente de todos los planes MSI activos (no solo la cuota del mes en curso,
     *    sino el principal retenido en la tarjeta: cuotas restantes más la cuota del periodo).
     */
    fun calculateCardCreditBalance(
        card: CreditCard,
        expenses: List<Expense>,
        payments: List<Payment>
    ): CardCreditBalance {
        val cardExpenses = expenses.filter { it.cardId == card.id }
        val cardPayments = payments.filter { it.cardId == card.id }.sumOf { it.amount }

        val regularExpenses = cardExpenses.filter { !it.isMsi }.sumOf { it.amount }

        var totalMsiPendingDebt = 0.0
        var currentCycleMsiCharge = 0.0

        cardExpenses.filter { it.isMsi }.forEach { msiExp ->
            val totalMonths = msiExp.msiTotalMonths.coerceAtLeast(1)
            val currentInstallment = msiExp.msiCurrentInstallment.coerceIn(1, totalMonths)
            val monthlyAmount = if (msiExp.amount > 0) msiExp.amount else {
                if (msiExp.msiTotalPurchaseAmount > 0) msiExp.msiTotalPurchaseAmount / totalMonths else 0.0
            }
            val totalCost = if (msiExp.msiTotalPurchaseAmount > 0) {
                msiExp.msiTotalPurchaseAmount
            } else {
                monthlyAmount * totalMonths
            }

            // Las cuotas futuras pendientes (totalMonths - currentInstallment)
            val remainingFutureInstallments = (totalMonths - currentInstallment).coerceAtLeast(0)
            // Principal retenido en la tarjeta: cuotas futuras pendientes + la cuota del corte actual
            val msiDebt = (monthlyAmount * remainingFutureInstallments + monthlyAmount).coerceAtMost(totalCost)

            totalMsiPendingDebt += msiDebt
            currentCycleMsiCharge += monthlyAmount
        }

        // El saldo total ocupado que retiene la línea de crédito
        val totalOccupiedCredit = (regularExpenses + totalMsiPendingDebt - cardPayments).coerceAtLeast(0.0)
        val availableCredit = (card.creditLimit - totalOccupiedCredit).coerceAtLeast(0.0)
        val usedRatio = if (card.creditLimit > 0) (totalOccupiedCredit / card.creditLimit).toFloat().coerceIn(0f, 1f) else 0f

        return CardCreditBalance(
            creditLimit = card.creditLimit,
            totalOccupiedCredit = totalOccupiedCredit,
            availableCredit = availableCredit,
            usedRatio = usedRatio,
            regularDebt = (regularExpenses - cardPayments).coerceAtLeast(0.0),
            totalMsiPendingBalance = totalMsiPendingDebt,
            currentCycleMsiCharge = currentCycleMsiCharge,
            totalPayments = cardPayments
        )
    }

    /**
     * Calcula automáticamente en qué mes fue el primer cargo en la tarjeta de un MSI
     * (según la fecha de compra y el día de corte) y determina las cuotas transcurridas y restantes
     * al corte de referencia o actual.
     */
    fun calculateMsiAutoTimeline(
        purchaseDateMillis: Long,
        cardCutoffDay: Int,
        totalMonths: Int,
        targetStatementMonthName: String? = null,
        targetStatementYear: Int? = null
    ): MsiAutoTimeline {
        val total = totalMonths.coerceAtLeast(1)
        val purchaseCal = Calendar.getInstance().apply { timeInMillis = purchaseDateMillis }
        val purchaseDay = purchaseCal.get(Calendar.DAY_OF_MONTH)
        val purchaseMonth = purchaseCal.get(Calendar.MONTH)
        val purchaseYear = purchaseCal.get(Calendar.YEAR)

        // Regla bancaria mexicana:
        // Si la compra fue en o antes del día de corte (<= cutoffDay), entra en el corte de ese mismo mes.
        // Si la compra fue después del día de corte (> cutoffDay), entra en el corte del mes siguiente.
        val firstChargeCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, purchaseYear)
            set(Calendar.MONTH, purchaseMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (purchaseDay > cardCutoffDay) {
            firstChargeCal.add(Calendar.MONTH, 1)
        }

        // Corrección: monthFormat ya tiene el patrón "MMMM yyyy" (incluye el año), así que antes
        // se concatenaba el año una segunda vez (p. ej. "Octubre 2025 2025").
        val firstChargeFull = monthFormat.format(firstChargeCal.time).replaceFirstChar { it.uppercase() }

        // Determinar el mes de corte de referencia
        val refCal = Calendar.getInstance()
        if (!targetStatementMonthName.isNullOrBlank()) {
            val norm = normalizeMonth(targetStatementMonthName).lowercase()
            val mIdx = when {
                norm.startsWith("ene") -> 0
                norm.startsWith("feb") -> 1
                norm.startsWith("mar") -> 2
                norm.startsWith("abr") -> 3
                norm.startsWith("may") -> 4
                norm.startsWith("jun") -> 5
                norm.startsWith("jul") -> 6
                norm.startsWith("ago") -> 7
                norm.startsWith("sep") -> 8
                norm.startsWith("oct") -> 9
                norm.startsWith("nov") -> 10
                norm.startsWith("dic") -> 11
                else -> refCal.get(Calendar.MONTH)
            }
            val parsedY = if (!targetStatementMonthName.isNullOrBlank()) {
                Regex("\\b(20\\d\\d)\\b").find(targetStatementMonthName)?.value?.toIntOrNull()
            } else null
            val y = targetStatementYear ?: parsedY ?: refCal.get(Calendar.YEAR)
            refCal.set(Calendar.YEAR, y)
            refCal.set(Calendar.MONTH, mIdx)
            refCal.set(Calendar.DAY_OF_MONTH, 1)
        } else {
            val todayDay = refCal.get(Calendar.DAY_OF_MONTH)
            refCal.set(Calendar.DAY_OF_MONTH, 1)
            if (todayDay > cardCutoffDay) {
                refCal.add(Calendar.MONTH, 1)
            }
        }

        val monthsDiff = (refCal.get(Calendar.YEAR) - firstChargeCal.get(Calendar.YEAR)) * 12 +
                (refCal.get(Calendar.MONTH) - firstChargeCal.get(Calendar.MONTH))

        val currentInst = if (monthsDiff < 0) {
            1
        } else {
            monthsDiff + 1
        }

        val clampedCurrentInst = currentInst.coerceIn(1, total)
        val remaining = (total - currentInst).coerceAtLeast(0)
        val completed = currentInst > total

        val refFull = monthFormat.format(refCal.time).replaceFirstChar { it.uppercase() }

        val explanation = when {
            completed -> "El plan de $total MSI inició en $firstChargeFull y ya cubrió sus $total mensualidades."
            monthsDiff == 0 -> "Compra del ciclo: 1er cargo en $firstChargeFull (cuota 1 de $total, restan $remaining meses)."
            else -> "Compra anterior: 1er cargo en $firstChargeFull (corte día $cardCutoffDay). Para $refFull corresponde a la cuota $clampedCurrentInst de $total (restan $remaining meses)."
        }

        return MsiAutoTimeline(
            firstChargeMonth = firstChargeFull,
            firstChargeYear = firstChargeCal.get(Calendar.YEAR),
            firstChargeMonthIndex = firstChargeCal.get(Calendar.MONTH),
            currentInstallment = clampedCurrentInst,
            remainingInstallments = remaining,
            isCompleted = completed,
            targetStatementMonth = refFull,
            explanation = explanation
        )
    }

    /**
     * Calcula el mes de corte ("targetStatementMonth") que corresponde a una cuota específica de un
     * MSI, a partir de la fecha real de compra y el día de corte de la tarjeta.
     *
     * Se usa al editar un MSI para mantener siempre sincronizados fecha de compra, cuota actual y
     * mes de corte: antes, editar la cuota actual o el plazo no recalculaba el mes de corte
     * guardado, por lo que la tabla de amortización podía terminar mostrando cuotas en meses
     * anteriores a la fecha de compra real (algo imposible).
     */
    fun calculateStatementMonthForInstallment(purchaseDateMillis: Long, cardCutoffDay: Int, installmentNumber: Int): String {
        val purchaseCal = Calendar.getInstance().apply { timeInMillis = purchaseDateMillis }
        val purchaseDay = purchaseCal.get(Calendar.DAY_OF_MONTH)

        val installmentCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, purchaseCal.get(Calendar.YEAR))
            set(Calendar.MONTH, purchaseCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (purchaseDay > cardCutoffDay) {
            installmentCal.add(Calendar.MONTH, 1)
        }
        installmentCal.add(Calendar.MONTH, (installmentNumber - 1).coerceAtLeast(0))

        return monthFormat.format(installmentCal.time).replaceFirstChar { it.uppercase() }
    }

    /**
     * Simula el costo real de liquidar un saldo cubriendo únicamente el Pago Mínimo reglamentario
     * (Cálculo según reglas de Banxico e IVA del 16% sobre intereses).
     */
    fun simulateMinimumPaymentPayoff(
        initialBalance: Double,
        annualInterestRatePercent: Double = 55.0,
        creditLimit: Double = 0.0,
        maxMonths: Int = 120
    ): MinimumPaymentSimulationResult {
        if (initialBalance <= 0.0) {
            return MinimumPaymentSimulationResult(
                balance = 0.0,
                annualInterestRate = annualInterestRatePercent,
                firstMonthMinimumPayment = 0.0,
                monthsToPayOff = 0,
                totalInterestPaid = 0.0,
                totalIvaPaid = 0.0,
                totalAmountPaidWithMinimums = 0.0,
                savingsPayingInFull = 0.0,
                monthlyAmortizationSample = emptyList()
            )
        }

        val monthlyRate = (annualInterestRatePercent / 100.0) / 12.0
        val ivaRate = 0.16

        var currentBalance = initialBalance
        var totalInterest = 0.0
        var totalIva = 0.0
        var totalPaid = 0.0
        var monthCount = 0
        var firstMonthMin = 0.0

        val sampleList = mutableListOf<AmortizationMonth>()

        while (currentBalance > 1.0 && monthCount < maxMonths) {
            monthCount++
            val startBal = currentBalance
            val interest = currentBalance * monthlyRate
            val iva = interest * ivaRate
            val chargesThisMonth = interest + iva

            val formula1 = (currentBalance * 0.015) + chargesThisMonth
            val formula2 = if (creditLimit > 0) creditLimit * 0.0125 else 200.0
            val minCalculated = maxOf(formula1, formula2, 200.0)

            val totalDue = currentBalance + chargesThisMonth
            val payment = minCalculated.coerceAtMost(totalDue)

            if (monthCount == 1) {
                firstMonthMin = payment
            }

            totalInterest += interest
            totalIva += iva
            totalPaid += payment

            val principalPaid = (payment - chargesThisMonth).coerceAtLeast(0.0)
            currentBalance = (currentBalance - principalPaid).coerceAtLeast(0.0)

            if (monthCount <= 6 || monthCount % 6 == 0 || currentBalance <= 1.0) {
                sampleList.add(
                    AmortizationMonth(
                        month = monthCount,
                        startBalance = startBal,
                        interest = interest,
                        iva = iva,
                        minimumPayment = payment,
                        principalPaid = principalPaid,
                        endBalance = currentBalance
                    )
                )
            }
        }

        if (currentBalance > 0.0) {
            totalPaid += currentBalance
        }

        val savings = (totalPaid - initialBalance).coerceAtLeast(0.0)

        return MinimumPaymentSimulationResult(
            balance = initialBalance,
            annualInterestRate = annualInterestRatePercent,
            firstMonthMinimumPayment = firstMonthMin,
            monthsToPayOff = monthCount,
            totalInterestPaid = totalInterest,
            totalIvaPaid = totalIva,
            totalAmountPaidWithMinimums = totalPaid,
            savingsPayingInFull = savings,
            monthlyAmortizationSample = sampleList
        )
    }

    fun calculatePaymentDueDayFromGrace(cutoffDay: Int, graceDays: Int): Int {
        val safeCutoff = cutoffDay.coerceIn(1, 31)
        val safeGrace = graceDays.coerceAtLeast(1)
        val total = safeCutoff + safeGrace
        return if (total <= 30) {
            total.coerceIn(1, 31)
        } else {
            val due = total - 30
            if (due <= 0) 1 else due.coerceIn(1, 31)
        }
    }

    /**
     * Recommends which card to use on a given date to maximize financing days.
     * Requisito 10: Las tarjetas departamentales nunca serán sugeridas en el resumen de tarjetas óptimas.
     */
    fun evaluateCardsForPurchase(
        cards: List<CreditCard>,
        purchaseDate: Date = Date()
    ): List<CardRecommendation> {
        val activeCards = cards.filter { it.isActive }
        if (activeCards.isEmpty()) return emptyList()

        val rawRecommendations = activeCards.map { card ->
            val (cutoffDate, paymentDueDate, isShifted) = calculateCycleDatesDetailed(card, purchaseDate)

            val diffMillis = paymentDueDate.time - purchaseDate.time
            val daysOfFinancing = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt().coerceAtLeast(1)

            val diffCutoffMillis = cutoffDate.time - purchaseDate.time
            val daysUntilCutoff = TimeUnit.MILLISECONDS.toDays(diffCutoffMillis).toInt().coerceAtLeast(0)

            val trafficStatus = when {
                daysOfFinancing >= 38 -> TrafficLightStatus.GREEN
                daysOfFinancing <= 22 -> TrafficLightStatus.RED
                else -> TrafficLightStatus.YELLOW
            }

            val reason = when {
                card.isDepartmental -> "Tarjeta departamental (${card.bank}) • $daysOfFinancing días de financiamiento"
                trafficStatus == TrafficLightStatus.GREEN -> "¡Excelente opción! Ganas $daysOfFinancing días de financiamiento libre de intereses"
                trafficStatus == TrafficLightStatus.YELLOW -> "Opción moderada ($daysOfFinancing días de financiamiento)"
                else -> "Poco financiamiento ($daysOfFinancing días hasta la fecha límite de pago)"
            }

            CardRecommendation(
                card = card,
                daysOfFinancing = daysOfFinancing,
                nextCutoffDate = cutoffDate,
                paymentDueDate = paymentDueDate,
                daysUntilCutoff = daysUntilCutoff,
                isBestOption = false,
                recommendationReason = reason,
                trafficLight = trafficStatus,
                isPaymentShiftedByHoliday = isShifted
            )
        }

        // Requisito 2: Las tarjetas departamentales se muestran en la sección pero NUNCA como principal recomendación
        val bestBankingCardId = rawRecommendations
            .filter { !it.card.isDepartmental }
            .maxByOrNull { it.daysOfFinancing }
            ?.card?.id

        // Ordenamos las tarjetas: primero las bancarias por financiamiento, luego las departamentales
        val sortedList = rawRecommendations.sortedWith(
            compareBy<CardRecommendation> { it.card.isDepartmental }
                .thenByDescending { it.daysOfFinancing }
        )

        return sortedList.map { rec ->
            if (rec.card.id == bestBankingCardId) {
                rec.copy(isBestOption = true, trafficLight = TrafficLightStatus.GREEN)
            } else {
                rec.copy(isBestOption = false)
            }
        }
    }

    /**
     * Helper to get the reference statement calendar for an MSI expense.
     * Accurately parses the statement year and month from targetStatementMonth or falls back to dateMillis.
     */
    fun getMsiBaseCalendar(expense: Expense): Calendar {
        val cal = Calendar.getInstance()
        val raw = expense.targetStatementMonth.trim()

        // Extract year from targetStatementMonth if present (e.g. "Septiembre 2026" or "2026-09")
        val yearRegex = "\\b(20\\d\\d)\\b".toRegex()
        val yearMatch = yearRegex.find(raw)
        val extractedYear = yearMatch?.value?.toIntOrNull() ?: if (expense.dateMillis > 0) {
            val dateCal = Calendar.getInstance().apply { timeInMillis = expense.dateMillis }
            dateCal.get(Calendar.YEAR)
        } else {
            2026
        }

        if (raw.contains("-")) {
            val parts = raw.split("-")
            val year = parts[0].toIntOrNull() ?: extractedYear
            val month = (parts[1].toIntOrNull() ?: 8) - 1
            cal.set(year, month, 1, 12, 0, 0)
        } else {
            val norm = normalizeMonth(raw).lowercase()
            val monthIdx = when {
                norm.startsWith("ene") -> 0
                norm.startsWith("feb") -> 1
                norm.startsWith("mar") -> 2
                norm.startsWith("abr") -> 3
                norm.startsWith("may") -> 4
                norm.startsWith("jun") -> 5
                norm.startsWith("jul") -> 6
                norm.startsWith("ago") -> 7
                norm.startsWith("sep") -> 8
                norm.startsWith("oct") -> 9
                norm.startsWith("nov") -> 10
                norm.startsWith("dic") -> 11
                else -> 7 // Default Agosto
            }
            cal.set(extractedYear, monthIdx, 1, 12, 0, 0)
        }
        return cal
    }

    /**
     * Calculates the original purchase date for an MSI plan that already has previous installments.
     * For example, if registering an installment 12 of 24 in September 2026, the purchase happened 11 months earlier (October 2025).
     */
    fun calculateOriginalPurchaseDate(statementMonthName: String, statementYear: Int, currentInstallment: Int): Long {
        val cal = Calendar.getInstance()
        val norm = normalizeMonth(statementMonthName).lowercase()
        val monthIdx = when {
            norm.startsWith("ene") -> 0
            norm.startsWith("feb") -> 1
            norm.startsWith("mar") -> 2
            norm.startsWith("abr") -> 3
            norm.startsWith("may") -> 4
            norm.startsWith("jun") -> 5
            norm.startsWith("jul") -> 6
            norm.startsWith("ago") -> 7
            norm.startsWith("sep") -> 8
            norm.startsWith("oct") -> 9
            norm.startsWith("nov") -> 10
            norm.startsWith("dic") -> 11
            else -> 7
        }
        // Corrección: antes se fijaba el día 15, un valor arbitrario que no tenía relación con el
        // día de corte real de la tarjeta. Como calculateMsiAutoTimeline decide el mes del primer
        // cargo comparando "día de compra" contra "día de corte", el día 15 podía quedar por encima
        // del corte real de la tarjeta y desplazar el mes de primer cargo un mes de más al
        // recalcularlo — desincronizando el avance de cuotas mostrado. El día 1 siempre es menor o
        // igual a cualquier día de corte válido (1-31), así que nunca provoca ese desplazamiento.
        cal.set(statementYear, monthIdx, 1, 12, 0, 0)
        val monthsAgo = (currentInstallment - 1).coerceAtLeast(0)
        cal.add(Calendar.MONTH, -monthsAgo)
        return cal.timeInMillis
    }

    /**
     * Calculates the exact calendar date when an MSI plan finishes its last installment.
     */
    fun getMsiCompletionCalendar(expense: Expense): Calendar {
        val totalMonths = expense.msiTotalMonths.coerceAtLeast(1)
        val currentInstallment = expense.msiCurrentInstallment.coerceIn(1, totalMonths)
        val remainingInstallments = totalMonths - currentInstallment
        val cal = getMsiBaseCalendar(expense)
        cal.add(Calendar.MONTH, remainingInstallments)
        return cal
    }

    /**
     * Computes MSI item details and remaining timeline.
     */
    fun computeMsiSummary(expense: Expense, card: CreditCard?): MsiSummary {
        val totalMonths = expense.msiTotalMonths.coerceAtLeast(1)
        val currentInstallment = expense.msiCurrentInstallment.coerceIn(1, totalMonths)
        val remainingInstallments = totalMonths - currentInstallment
        val monthlyAmount = expense.amount

        val totalCost = if (expense.msiTotalPurchaseAmount > 0) {
            expense.msiTotalPurchaseAmount
        } else {
            monthlyAmount * totalMonths
        }

        val totalPaid = monthlyAmount * currentInstallment
        val remainingBalance = (monthlyAmount * remainingInstallments).coerceAtLeast(0.0)
        val progress = (currentInstallment.toFloat() / totalMonths.toFloat()).coerceIn(0f, 1f)

        // Requisito 2: Cálculo sincronizado de finalización
        val completionCal = getMsiCompletionCalendar(expense)
        val completionDateString = monthFormat.format(completionCal.time).replaceFirstChar { it.uppercase() }

        return MsiSummary(
            expense = expense,
            cardName = card?.name ?: "TDC",
            card = card,
            monthlyPayment = monthlyAmount,
            totalPaidSoFar = totalPaid,
            remainingBalance = remainingBalance,
            installmentsRemaining = remainingInstallments,
            progressPercent = progress,
            completionDateString = completionDateString,
            willFinishInMonths = remainingInstallments,
            totalPurchaseAmount = totalCost
        )
    }

    /**
     * Projects upcoming cash flow increases as MSI items finish.
     * Requisito 2: La liberación de flujo de efectivo coincide al 100% con la finalización de los MSI.
     */
    fun projectCashFlowRelease(msiSummaries: List<MsiSummary>): List<CashFlowRelease> {
        val activeItems = msiSummaries.filter { it.installmentsRemaining > 0 }
        if (activeItems.isEmpty()) return emptyList()

        // Agrupamos directamente por el mes exacto de terminación para sincronización perfecta
        val grouped = activeItems.groupBy { it.completionDateString }

        // Ordenamos cronológicamente por la fecha de finalización
        val sortedGroups = grouped.entries.sortedBy { (_, items) ->
            getMsiCompletionCalendar(items.first().expense).timeInMillis
        }

        return sortedGroups.map { (dateLabel, items) ->
            val amountFreed = items.sumOf { it.monthlyPayment }
            val itemNames = items.map { "${it.expense.concept} (+$${String.format(Locale.US, "%.2f", it.monthlyPayment)}/mes)" }
            CashFlowRelease(
                monthYearLabel = dateLabel,
                monthlyAmountFreed = amountFreed,
                finishingItems = itemNames
            )
        }
    }

    /**
     * Computes statement summary for a selected statement month (e.g. "2026-08") and card.
     */
    fun computeStatementSummary(
        statementMonth: String,
        selectedCard: CreditCard?,
        expenses: List<Expense>,
        payments: List<Payment>
    ): StatementSummary {
        val filteredExpenses = if (selectedCard != null) {
            expenses.filter { it.cardId == selectedCard.id }
        } else {
            expenses
        }

        val filteredPayments = if (selectedCard != null) {
            payments.filter { it.cardId == selectedCard.id }
        } else {
            payments
        }

        val totalCharges = filteredExpenses.sumOf { it.amount }
        val totalPayments = filteredPayments.sumOf { it.amount }
        val remaining = totalCharges - totalPayments

        // Beneficiary breakdown
        val beneficiaryMap = mutableMapOf<String, Double>()
        for (exp in filteredExpenses) {
            val ben = exp.beneficiary.ifBlank { "Personal" }
            beneficiaryMap[ben] = (beneficiaryMap[ben] ?: 0.0) + exp.amount
        }

        val cutoffStr = if (selectedCard != null) {
            "Día ${selectedCard.cutoffDay} del mes"
        } else {
            "Varios cortes según tarjeta"
        }

        val paymentDueStr = if (selectedCard != null) {
            "Día ${selectedCard.paymentDueDay} del mes"
        } else {
            "Vencimientos según tarjeta"
        }

        return StatementSummary(
            statementMonth = statementMonth,
            cardId = selectedCard?.id,
            cardName = selectedCard?.name ?: "Todas las Tarjetas (Consolidado)",
            cutoffDateString = cutoffStr,
            paymentDueDateString = paymentDueStr,
            totalCharges = totalCharges,
            totalPayments = totalPayments,
            remainingBalance = remaining,
            isFullyPaid = remaining <= 0.01,
            beneficiaryBreakdown = beneficiaryMap
        )
    }

    fun formatDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun formatFriendlyDate(date: Date): String {
        return dayMonthFormat.format(date)
    }

    /**
     * Calculates the name of the month in which the purchase will be paid,
     * based on the card's cutoff and payment due days.
     */
    fun calculatePaymentMonthName(card: CreditCard, expenseDateMillis: Long): String {
        val (_, paymentDueDate) = calculateCycleDates(card, Date(expenseDateMillis))
        val monthName = SimpleDateFormat("MMMM", Locale("es", "MX")).format(paymentDueDate)
        return monthName.replaceFirstChar { it.uppercase() }
    }

    /**
     * Calculates grace days based on cutoff day and payment due day.
     * Symmetric with calculatePaymentDueDayFromGrace so 1 day change in due adds exactly 1 grace day.
     */
    fun calculateGraceDaysFromDue(cutoffDay: Int, paymentDueDay: Int): Int {
        val safeCutoff = cutoffDay.coerceIn(1, 31)
        val safeDue = paymentDueDay.coerceIn(1, 31)
        return if (safeDue > safeCutoff) {
            safeDue - safeCutoff
        } else {
            (30 - safeCutoff) + safeDue
        }
    }

    /**
     * Normalizes a raw month representation (e.g. "2026-08", "Agosto 2025" or "agosto")
     * to the clean month name in Spanish ("Agosto").
     */
    fun normalizeMonth(raw: String): String {
        val trimmed = raw.trim()
        val monthRegex = Regex("(?i)(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)")
        val match = monthRegex.find(trimmed)
        if (match != null) {
            return match.value.lowercase().replaceFirstChar { it.uppercase() }
        }
        val dashRegex = Regex("\\b\\d{4}-(\\d{2})\\b")
        val dashMatch = dashRegex.find(trimmed)
        if (dashMatch != null) {
            return when (dashMatch.groupValues[1]) {
                "01" -> "Enero"
                "02" -> "Febrero"
                "03" -> "Marzo"
                "04" -> "Abril"
                "05" -> "Mayo"
                "06" -> "Junio"
                "07" -> "Julio"
                "08" -> "Agosto"
                "09" -> "Septiembre"
                "10" -> "Octubre"
                "11" -> "Noviembre"
                "12" -> "Diciembre"
                else -> trimmed
            }
        }
        return trimmed.replaceFirstChar { it.uppercase() }
    }

    /**
     * Extracts the year of a statement record from either targetStatementMonth or dateMillis.
     */
    fun extractYear(dateMillis: Long, targetStatementMonth: String): Int {
        val yearRegex = "\\b(20\\d\\d)\\b".toRegex()
        val match = yearRegex.find(targetStatementMonth)
        if (match != null) {
            return match.value.toInt()
        }
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        return cal.get(Calendar.YEAR)
    }

    /**
     * Extracts "yyyy-MM" from dateMillis.
     */
    fun extractYearMonth(dateMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    /**
     * Parsea montos ingresados por el usuario tolerando tanto punto (.) como coma (,) decimal,
     * símbolos de moneda ($), espacios y separadores de miles.
     */
    fun parseLocalizedDouble(input: String?): Double? {
        if (input == null) return null
        val clean = input.trim().replace("$", "").replace(" ", "")
        if (clean.isBlank()) return null
        return try {
            if (clean.contains(',') && clean.contains('.')) {
                if (clean.lastIndexOf(',') > clean.lastIndexOf('.')) {
                    // Formato 1.500,50
                    clean.replace(".", "").replace(',', '.').toDoubleOrNull()
                } else {
                    // Formato 1,500.50
                    clean.replace(",", "").toDoubleOrNull()
                }
            } else if (clean.contains(',')) {
                clean.replace(',', '.').toDoubleOrNull()
            } else {
                clean.toDoubleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
