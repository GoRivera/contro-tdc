package com.example.domain

import com.example.data.model.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object CreditCardCalculator {

    val SPANISH_MONTHS = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    private fun getEasterDate(year: Int): Pair<Int, Int> {
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
        val month = (h + l - 7 * m + 114) / 31 - 1 // 0-indexed: March=2, April=3
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return Pair(month, day)
    }

    fun isMexicanBankingHoliday(cal: Calendar): Boolean {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return true

        val month = cal.get(Calendar.MONTH) // 0-11
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)

        // 1 de Enero: Año Nuevo
        if (month == Calendar.JANUARY && day == 1) return true

        // Primer lunes de febrero (Constitución)
        if (month == Calendar.FEBRUARY && dayOfWeek == Calendar.MONDAY && day <= 7) return true

        // Tercer lunes de marzo (Benito Juárez)
        if (month == Calendar.MARCH && dayOfWeek == Calendar.MONDAY && day in 15..21) return true

        // Jueves Santo y Viernes Santo
        val (easterMonth, easterDay) = getEasterDate(year)
        val easterCal = Calendar.getInstance().apply {
            set(year, easterMonth, easterDay, 0, 0, 0)
        }
        val holyThursday = (easterCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -3) }
        val goodFriday = (easterCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -2) }

        if (month == holyThursday.get(Calendar.MONTH) && day == holyThursday.get(Calendar.DAY_OF_MONTH)) return true
        if (month == goodFriday.get(Calendar.MONTH) && day == goodFriday.get(Calendar.DAY_OF_MONTH)) return true

        // 1 de Mayo: Día del Trabajo
        if (month == Calendar.MAY && day == 1) return true

        // 16 de Septiembre: Día de la Independencia
        if (month == Calendar.SEPTEMBER && day == 16) return true

        // 1 de Octubre: Transmisión del Poder Ejecutivo Federal (cada 6 años desde 2024)
        if (month == Calendar.OCTOBER && day == 1 && (year - 2024) % 6 == 0) return true

        // 2 de Noviembre: Día de Muertos
        if (month == Calendar.NOVEMBER && day == 2) return true

        // Tercer lunes de Noviembre: Revolución Mexicana
        if (month == Calendar.NOVEMBER && dayOfWeek == Calendar.MONDAY && day in 15..21) return true

        // 12 de Diciembre: Día del Empleado Bancario
        if (month == Calendar.DECEMBER && day == 12) return true

        // 25 de Diciembre: Navidad
        if (month == Calendar.DECEMBER && day == 25) return true

        return false
    }

    fun getEffectivePaymentDueDate(rawDueDate: Calendar): Pair<Calendar, Boolean> {
        val current = rawDueDate.clone() as Calendar
        var shifted = false
        while (isMexicanBankingHoliday(current)) {
            current.add(Calendar.DAY_OF_MONTH, 1)
            shifted = true
        }
        return Pair(current, shifted)
    }

    data class CycleResult(
        val cutoffCal: Calendar,
        val paymentDueCal: Calendar,
        val wasShifted: Boolean
    )

    fun calculateCycleDatesDetailed(card: CreditCard, referenceDate: Calendar = Calendar.getInstance()): CycleResult {
        val currentDay = referenceDate.get(Calendar.DAY_OF_MONTH)
        val currentMonth = referenceDate.get(Calendar.MONTH)
        val currentYear = referenceDate.get(Calendar.YEAR)

        val cutoffCal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        if (currentDay <= card.cutoffDay) {
            val maxDaysInMonth = cutoffCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualCutoff = min(card.cutoffDay, maxDaysInMonth)
            cutoffCal.set(Calendar.DAY_OF_MONTH, actualCutoff)
        } else {
            cutoffCal.add(Calendar.MONTH, 1)
            val maxDaysInNextMonth = cutoffCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualCutoff = min(card.cutoffDay, maxDaysInNextMonth)
            cutoffCal.set(Calendar.DAY_OF_MONTH, actualCutoff)
        }

        val paymentCal = cutoffCal.clone() as Calendar
        if (card.paymentDueDay > card.cutoffDay) {
            val maxDays = paymentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            paymentCal.set(Calendar.DAY_OF_MONTH, min(card.paymentDueDay, maxDays))
        } else {
            paymentCal.add(Calendar.MONTH, 1)
            val maxDays = paymentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            paymentCal.set(Calendar.DAY_OF_MONTH, min(card.paymentDueDay, maxDays))
        }

        val (effectiveDue, shifted) = getEffectivePaymentDueDate(paymentCal)
        return CycleResult(cutoffCal, effectiveDue, shifted)
    }

    fun evaluateCardsForPurchase(cards: List<CreditCard>, purchaseDate: Calendar = Calendar.getInstance()): List<CardRecommendation> {
        val activeCards = cards.filter { it.isActive }
        if (activeCards.isEmpty()) return emptyList()

        val dateFormat = SimpleDateFormat("dd 'de' MMMM", Locale("es", "MX"))

        val rawRecommendations = activeCards.map { card ->
            val cycle = calculateCycleDatesDetailed(card, purchaseDate)

            val diffMillis = cycle.paymentDueCal.timeInMillis - purchaseDate.timeInMillis
            val daysOfFinancing = max(1, (diffMillis / (1000 * 60 * 60 * 24)).toInt())

            val diffCutoff = cycle.cutoffCal.timeInMillis - purchaseDate.timeInMillis
            val daysUntilCutoff = max(0, (diffCutoff / (1000 * 60 * 60 * 24)).toInt())

            val trafficLight = when {
                daysOfFinancing >= 38 -> TrafficLight.GREEN
                daysOfFinancing <= 22 -> TrafficLight.RED
                else -> TrafficLight.YELLOW
            }

            val reason = when {
                card.isDepartmental -> "Tarjeta departamental (${card.bank}) • $daysOfFinancing días de financiamiento"
                trafficLight == TrafficLight.GREEN -> "¡Excelente opción! Ganas $daysOfFinancing días de financiamiento libre de intereses"
                trafficLight == TrafficLight.YELLOW -> "Opción moderada ($daysOfFinancing días de financiamiento)"
                else -> "Poco financiamiento ($daysOfFinancing días hasta la fecha límite de pago)"
            }

            CardRecommendation(
                card = card,
                daysOfFinancing = daysOfFinancing,
                nextCutoffDateFormatted = dateFormat.format(cycle.cutoffCal.time),
                paymentDueDateFormatted = dateFormat.format(cycle.paymentDueCal.time),
                daysUntilCutoff = daysUntilCutoff,
                isBestOption = false,
                recommendationReason = reason,
                trafficLight = trafficLight,
                isPaymentShiftedByHoliday = cycle.wasShifted
            )
        }

        val bestBanking = rawRecommendations
            .filter { !it.card.isDepartmental }
            .maxByOrNull { it.daysOfFinancing }

        val bestId = bestBanking?.card?.id

        val sorted = rawRecommendations.sortedWith(
            compareBy<CardRecommendation> { it.card.isDepartmental }
                .thenByDescending { it.daysOfFinancing }
        )

        return sorted.map { rec ->
            val isBest = rec.card.id == bestId
            rec.copy(
                isBestOption = isBest,
                trafficLight = if (isBest) TrafficLight.GREEN else rec.trafficLight
            )
        }
    }

    fun calculateCardCreditBalance(
        card: CreditCard,
        expenses: List<Expense>,
        payments: List<Payment>
    ): CardCreditBalance {
        val cardExpenses = expenses.filter { it.cardId == card.id }
        val totalPayments = payments.filter { it.cardId == card.id }.sumOf { it.amount }

        val regularExpenses = cardExpenses.filter { !it.isMsi }.sumOf { it.amount }

        var totalMsiPendingDebt = 0.0
        var currentCycleMsiCharge = 0.0

        cardExpenses.filter { it.isMsi }.forEach { msi ->
            val totalMonths = max(1, msi.msiTotalMonths)
            val currentInst = min(max(1, msi.msiCurrentInstallment), totalMonths)
            val monthlyAmount = if (msi.amount > 0) msi.amount else {
                if (msi.msiTotalPurchaseAmount > 0) msi.msiTotalPurchaseAmount / totalMonths else 0.0
            }
            val totalCost = if (msi.msiTotalPurchaseAmount > 0) msi.msiTotalPurchaseAmount else monthlyAmount * totalMonths
            val remainingFuture = max(0, totalMonths - currentInst)
            val msiDebt = min(totalCost, (monthlyAmount * remainingFuture) + monthlyAmount)

            totalMsiPendingDebt += msiDebt
            currentCycleMsiCharge += monthlyAmount
        }

        val totalOccupiedCredit = max(0.0, (regularExpenses + totalMsiPendingDebt) - totalPayments)
        val availableCredit = max(0.0, card.creditLimit - totalOccupiedCredit)
        val usedRatio = if (card.creditLimit > 0) min(1.0, totalOccupiedCredit / card.creditLimit) else 0.0
        val occupancyPercentage = min(100, (usedRatio * 100).roundToInt())

        return CardCreditBalance(
            creditLimit = card.creditLimit,
            totalOccupiedCredit = totalOccupiedCredit,
            availableCredit = availableCredit,
            occupancyPercentage = occupancyPercentage,
            regularDebt = max(0.0, regularExpenses - totalPayments),
            totalMsiPendingBalance = totalMsiPendingDebt,
            currentCycleMsiCharge = currentCycleMsiCharge,
            totalPayments = totalPayments
        )
    }

    fun computeMsiSummary(expense: Expense, card: CreditCard?): MsiSummary {
        val totalMonths = max(1, expense.msiTotalMonths)
        val currentInstallment = max(1, expense.msiCurrentInstallment)
        val monthlyAmount = expense.amount
        val totalCost = if (expense.msiTotalPurchaseAmount > 0) expense.msiTotalPurchaseAmount else monthlyAmount * totalMonths

        val remainingInstallments = max(0, totalMonths - currentInstallment)
        val isCompleted = currentInstallment > totalMonths
        val isLastInstallmentPending = currentInstallment == totalMonths

        val totalPaid = if (isCompleted) totalCost else min(totalCost, monthlyAmount * currentInstallment)
        val remainingBalance = max(0.0, monthlyAmount * remainingInstallments)
        val progressPercent = if (isCompleted) 1.0f else min(0.99f, currentInstallment.toFloat() / totalMonths.toFloat())

        val completionCal = Calendar.getInstance().apply {
            add(Calendar.MONTH, remainingInstallments)
        }
        val completionMonthName = SPANISH_MONTHS[completionCal.get(Calendar.MONTH)]
        val completionDateString = "$completionMonthName ${completionCal.get(Calendar.YEAR)}"

        return MsiSummary(
            expense = expense,
            cardName = card?.name ?: "TDC",
            card = card,
            monthlyPayment = monthlyAmount,
            totalPaidSoFar = totalPaid,
            remainingBalance = remainingBalance,
            installmentsRemaining = remainingInstallments,
            progressPercent = progressPercent,
            completionDateString = completionDateString,
            totalPurchaseAmount = totalCost,
            isCompleted = isCompleted,
            isLastInstallmentPending = isLastInstallmentPending
        )
    }

    fun projectCashFlowRelease(msiSummaries: List<MsiSummary>): List<CashFlowRelease> {
        val activeItems = msiSummaries.filter { it.installmentsRemaining > 0 }
        if (activeItems.isEmpty()) return emptyList()

        val grouped = activeItems.groupBy { it.completionDateString }
        return grouped.map { (monthLabel, items) ->
            val freed = items.sumOf { it.monthlyPayment }
            val titles = items.map { "${it.expense.concept} (+${formatCurrency(it.monthlyPayment)}/mes)" }
            CashFlowRelease(
                monthYearLabel = monthLabel,
                monthlyAmountFreed = freed,
                finishingItems = titles
            )
        }
    }

    fun simulateMinimumPaymentPayoff(
        initialBalance: Double,
        annualInterestRatePercent: Double = 55.0,
        creditLimit: Double = 0.0,
        maxMonths: Int = 120
    ): MinimumPaymentSimulationResult {
        if (initialBalance <= 0) {
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

        val samples = mutableListOf<AmortizationMonth>()

        while (currentBalance > 1.0 && monthCount < maxMonths) {
            monthCount++
            val startBal = currentBalance
            val interest = currentBalance * monthlyRate
            val iva = interest * ivaRate
            val chargesThisMonth = interest + iva

            val formula1 = (currentBalance * 0.015) + chargesThisMonth
            val formula2 = if (creditLimit > 0) creditLimit * 0.0125 else 200.0
            val minCalculated = max(formula1, max(formula2, 200.0))

            val totalDue = currentBalance + chargesThisMonth
            val payment = min(minCalculated, totalDue)

            if (monthCount == 1) {
                firstMonthMin = payment
            }

            totalInterest += interest
            totalIva += iva
            totalPaid += payment

            val principalPaid = max(0.0, payment - chargesThisMonth)
            currentBalance = max(0.0, currentBalance - principalPaid)

            if (monthCount <= 6 || monthCount % 6 == 0 || currentBalance <= 1.0) {
                samples.add(
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

        if (currentBalance > 0) {
            totalPaid += currentBalance
        }

        val savings = max(0.0, totalPaid - initialBalance)

        return MinimumPaymentSimulationResult(
            balance = initialBalance,
            annualInterestRate = annualInterestRatePercent,
            firstMonthMinimumPayment = firstMonthMin,
            monthsToPayOff = monthCount,
            totalInterestPaid = totalInterest,
            totalIvaPaid = totalIva,
            totalAmountPaidWithMinimums = totalPaid,
            savingsPayingInFull = savings,
            monthlyAmortizationSample = samples
        )
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return format.format(amount)
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
        return sdf.format(Date(millis))
    }
}
