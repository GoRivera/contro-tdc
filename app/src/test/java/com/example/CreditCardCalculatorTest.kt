package com.example

import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.domain.CreditCardCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CreditCardCalculatorTest {

    private val likeU = CreditCard(
        id = 1L,
        name = "Like U",
        bank = "Santander",
        cutoffDay = 12,
        paymentDueDay = 2,
        creditLimit = 65000.0,
        primaryColorHex = 0xFFEC0000,
        secondaryColorHex = 0xFF990000
    )

    private val bbva = CreditCard(
        id = 2L,
        name = "Oro BBVA",
        bank = "BBVA",
        cutoffDay = 19,
        paymentDueDay = 9,
        creditLimit = 70000.0,
        primaryColorHex = 0xFF0B2265,
        secondaryColorHex = 0xFF1464A5
    )

    @Test
    fun testFinancingDaysRecommendation() {
        val cal = Calendar.getInstance()
        // Reference date: July 13 (1 day AFTER Like U's cutoff on July 12)
        cal.set(2026, Calendar.JULY, 13, 10, 0, 0)
        val purchaseDate = cal.time

        val recs = CreditCardCalculator.evaluateCardsForPurchase(listOf(likeU, bbva), purchaseDate)
        assertTrue(recs.isNotEmpty())

        // Like U should have ~51 days of financing (cutoff Aug 12, payment due Sept 2)
        val likeURec = recs.first { it.card.id == likeU.id }
        assertTrue("Like U should have >= 45 financing days", likeURec.daysOfFinancing >= 45)
        assertTrue("Like U should be the best option right after its cutoff", likeURec.isBestOption)
    }

    @Test
    fun testMsiCalculation() {
        val expense = Expense(
            id = 10L,
            cardId = 1L,
            concept = "iPhone Memé [3 de 12]",
            amount = 1666.58,
            dateMillis = System.currentTimeMillis(),
            beneficiary = "Memé",
            isMsi = true,
            msiTotalMonths = 12,
            msiCurrentInstallment = 3
        )

        val summary = CreditCardCalculator.computeMsiSummary(expense, likeU)
        assertEquals(9, summary.installmentsRemaining)
        assertEquals(1666.58 * 9, summary.remainingBalance, 0.01)
        assertEquals(0.25f, summary.progressPercent, 0.01f)
    }

    @Test
    fun testStatementSummary() {
        val expenses = listOf(
            Expense(id = 1, cardId = 1, concept = "Despensa", amount = 1000.0, dateMillis = 0, beneficiary = "Personal"),
            Expense(id = 2, cardId = 1, concept = "iPhone Memé", amount = 1500.0, dateMillis = 0, beneficiary = "Memé")
        )
        val payments = listOf(
            Payment(id = 1, cardId = 1, concept = "Pago TDC", amount = 2000.0, dateMillis = 0)
        )

        val summary = CreditCardCalculator.computeStatementSummary("2026-08", likeU, expenses, payments)
        assertEquals(2500.0, summary.totalCharges, 0.01)
        assertEquals(2000.0, summary.totalPayments, 0.01)
        assertEquals(500.0, summary.remainingBalance, 0.01)
        assertEquals(false, summary.isFullyPaid)
        assertEquals(1500.0, summary.beneficiaryBreakdown["Memé"] ?: 0.0, 0.01)
    }

    @Test
    fun testCalculatePaymentMonthName() {
        val cal = Calendar.getInstance()

        // Like U cutoff: day 12, payment due: day 2
        // Purchase on July 10 (before cutoff 12) -> cuts July 12, payment due August 2 -> "Agosto"
        cal.set(2026, Calendar.JULY, 10, 12, 0, 0)
        val monthJuly10 = CreditCardCalculator.calculatePaymentMonthName(likeU, cal.timeInMillis)
        assertEquals("Agosto", monthJuly10)

        // Purchase on July 14 (after cutoff 12) -> cuts August 12, payment due September 2 -> "Septiembre"
        cal.set(2026, Calendar.JULY, 14, 12, 0, 0)
        val monthJuly14 = CreditCardCalculator.calculatePaymentMonthName(likeU, cal.timeInMillis)
        assertEquals("Septiembre", monthJuly14)

        // Card with payment due in same month: Joy Banamex cutoff day 4, payment due day 25
        val joyCard = CreditCard(
            id = 7L,
            name = "Joy",
            bank = "Banamex",
            cutoffDay = 4,
            paymentDueDay = 25,
            creditLimit = 50000.0,
            primaryColorHex = 0xFF008080,
            secondaryColorHex = 0xFF004D4D
        )
        // Purchase on August 2 (before cutoff 4) -> cuts August 4, payment due August 25 -> "Agosto"
        cal.set(2026, Calendar.AUGUST, 2, 12, 0, 0)
        val monthJoyAug2 = CreditCardCalculator.calculatePaymentMonthName(joyCard, cal.timeInMillis)
        assertEquals("Agosto", monthJoyAug2)

        // Purchase on August 10 (after cutoff 4) -> cuts September 4, payment due September 25 -> "Septiembre"
        cal.set(2026, Calendar.AUGUST, 10, 12, 0, 0)
        val monthJoyAug10 = CreditCardCalculator.calculatePaymentMonthName(joyCard, cal.timeInMillis)
        assertEquals("Septiembre", monthJoyAug10)
    }

    @Test
    fun testNormalizeMonth() {
        assertEquals("Agosto", CreditCardCalculator.normalizeMonth("2026-08"))
        assertEquals("Septiembre", CreditCardCalculator.normalizeMonth("2026-09"))
        assertEquals("Octubre", CreditCardCalculator.normalizeMonth("2026-10"))
        assertEquals("Agosto", CreditCardCalculator.normalizeMonth("agosto"))
        assertEquals("Septiembre", CreditCardCalculator.normalizeMonth("SEPTIEMBRE"))
    }

    @Test
    fun testMsiAutoTimelineFirstCharge() {
        // Like U: corte día 12. Compra el día 10 (antes del corte) -> primer cargo en el mismo mes.
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JULY, 10, 12, 0, 0)
        val timelineSameMonth = CreditCardCalculator.calculateMsiAutoTimeline(
            purchaseDateMillis = cal.timeInMillis,
            cardCutoffDay = likeU.cutoffDay,
            totalMonths = 12
        )
        assertEquals(Calendar.JULY, timelineSameMonth.firstChargeMonthIndex)
        assertEquals(2026, timelineSameMonth.firstChargeYear)
        // El año no debe repetirse en la descripción (bug corregido: "Julio 2026 2026")
        assertFalse(timelineSameMonth.explanation.contains("2026 2026"))
        assertFalse(timelineSameMonth.firstChargeMonth.contains("2026 2026"))

        // Compra el día 14 (después del corte) -> primer cargo hasta el mes siguiente.
        cal.set(2026, Calendar.JULY, 14, 12, 0, 0)
        val timelineNextMonth = CreditCardCalculator.calculateMsiAutoTimeline(
            purchaseDateMillis = cal.timeInMillis,
            cardCutoffDay = likeU.cutoffDay,
            totalMonths = 12
        )
        assertEquals(Calendar.AUGUST, timelineNextMonth.firstChargeMonthIndex)
        assertEquals(2026, timelineNextMonth.firstChargeYear)
    }

    @Test
    fun testCalculateOriginalPurchaseDateRoundTrip() {
        // Registrar un MSI "de hace tiempo": corte activo Septiembre 2026, cuota 3 de 12.
        // El primer cargo debió haber sido en Julio 2026 (2 meses antes).
        val purchaseMillis = CreditCardCalculator.calculateOriginalPurchaseDate(
            statementMonthName = "Septiembre",
            statementYear = 2026,
            currentInstallment = 3
        )

        val timeline = CreditCardCalculator.calculateMsiAutoTimeline(
            purchaseDateMillis = purchaseMillis,
            cardCutoffDay = likeU.cutoffDay,
            totalMonths = 12,
            targetStatementMonthName = "Septiembre",
            targetStatementYear = 2026
        )

        assertEquals(Calendar.JULY, timeline.firstChargeMonthIndex)
        assertEquals(2026, timeline.firstChargeYear)
        assertEquals(3, timeline.currentInstallment)
    }

    @Test
    fun testCalculateStatementMonthForInstallment() {
        // Compra el 10 de julio de 2026 (antes del corte día 12 de Like U).
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JULY, 10, 12, 0, 0)
        val purchaseMillis = cal.timeInMillis

        assertEquals(
            "Julio 2026",
            CreditCardCalculator.calculateStatementMonthForInstallment(purchaseMillis, likeU.cutoffDay, 1)
        )
        assertEquals(
            "Septiembre 2026",
            CreditCardCalculator.calculateStatementMonthForInstallment(purchaseMillis, likeU.cutoffDay, 3)
        )
    }

    @Test
    fun testResolveNearestYearForMonthCrossesYearBoundary() {
        val today = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 15, 12, 0, 0) }.time

        // "Diciembre" visto desde enero de 2026 es diciembre del año ANTERIOR (2025), no 2026.
        assertEquals(2025, CreditCardCalculator.resolveNearestYearForMonth("Diciembre", today))
        // Un mes cercano (Febrero) se queda en el mismo año de referencia.
        assertEquals(2026, CreditCardCalculator.resolveNearestYearForMonth("Febrero", today))
    }

    @Test
    fun testIsStatementPeriodSettled() {
        val expenses = listOf(
            Expense(id = 1, cardId = 1, concept = "MSI cuota", amount = 500.0, dateMillis = 0, targetStatementMonth = "Septiembre 2026")
        )
        val paymentsFull = listOf(
            Payment(id = 1, cardId = 1, concept = "Pago TDC", amount = 500.0, dateMillis = 0, targetStatementMonth = "Septiembre 2026")
        )
        val paymentsPartial = listOf(
            Payment(id = 1, cardId = 1, concept = "Pago TDC", amount = 200.0, dateMillis = 0, targetStatementMonth = "Septiembre 2026")
        )

        assertTrue(CreditCardCalculator.isStatementPeriodSettled(1L, 2026, "Septiembre 2026", expenses, paymentsFull))
        assertFalse(CreditCardCalculator.isStatementPeriodSettled(1L, 2026, "Septiembre 2026", expenses, paymentsPartial))
        assertFalse(CreditCardCalculator.isStatementPeriodSettled(1L, 2026, "Septiembre 2026", expenses, emptyList()))
    }

    @Test
    fun testParseLocalizedDouble() {
        assertEquals(1500.50, CreditCardCalculator.parseLocalizedDouble("1,500.50")!!, 0.001)
        assertEquals(1500.50, CreditCardCalculator.parseLocalizedDouble("1.500,50")!!, 0.001)
        assertEquals(230.0, CreditCardCalculator.parseLocalizedDouble("$230.00")!!, 0.001)
        assertEquals(230.0, CreditCardCalculator.parseLocalizedDouble("230,00")!!, 0.001)
        assertNull(CreditCardCalculator.parseLocalizedDouble(""))
        assertNull(CreditCardCalculator.parseLocalizedDouble(null))
    }
}
