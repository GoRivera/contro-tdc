package com.example.data.model

data class CreditCard(
    val id: Long,
    val name: String,
    val bank: String,
    val cutoffDay: Int,
    val paymentDueDay: Int,
    val creditLimit: Double,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val last4Digits: String,
    val network: String = "Visa", // Visa, Mastercard, Amex, Departamental
    val isActive: Boolean = true,
    val isDepartmental: Boolean = false,
    val graceDays: Int = 20,
    val cardholderName: String = "G. RIVERA",
    val annualInterestRatePercent: Double = 55.0,
    val annualFee: Double = 0.0
)

data class Expense(
    val id: Long,
    val cardId: Long,
    val concept: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val beneficiary: String = "Personal", // Personal, Memé, Ale, Poncho, Familiar, Otro
    val category: String = "Despensa", // Despensa, Servicios, Tecnología, Gasolina, Restaurantes, Salud, Hogar, Otros
    val isMsi: Boolean = false,
    val msiTotalMonths: Int = 1,
    val msiCurrentInstallment: Int = 1,
    val msiTotalPurchaseAmount: Double = 0.0,
    val notes: String = "",
    val targetStatementMonth: String = "Septiembre 2026"
)

data class Payment(
    val id: Long,
    val cardId: Long,
    val concept: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val sourcePayer: String = "Personal", // Personal, Memé, Ale, Poncho, Banco
    val targetStatementMonth: String = "Septiembre 2026",
    val notes: String = ""
)

data class Subscription(
    val id: Long,
    val name: String,
    val cardId: Long,
    val billingDayOfMonth: Int,
    val totalMonthlyAmount: Double,
    val category: String = "Streaming",
    val startMonth: String = "Enero 2026",
    val isActive: Boolean = true,
    val notes: String = "",
    val participantsSummary: String = "",
    val periodicity: String = "MENSUAL" // MENSUAL, BIMESTRAL, ANUAL
)

data class FuelEntry(
    val id: Long,
    val cardId: Long,
    val kmDriven: Double,
    val fuelType: String = "Premium (Roja)", // Premium (Roja), Regular (Verde)
    val pricePerLiter: Double,
    val litersLoaded: Double,
    val totalCost: Double,
    val efficiencyKmPerL: Double,
    val isDivided: Boolean = false,
    val personalShare: Double,
    val dividedWith: String = "",
    val dividedCount: Int = 1,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class ServiceEntry(
    val id: Long,
    val serviceType: String, // CFE (Luz), Agua, Gas, Internet
    val dateMillis: Long = System.currentTimeMillis(),
    val amount: Double,
    val consumption: Double = 0.0, // kWh, m3
    val notes: String = "",
    val cardId: Long = 1L
)

enum class TrafficLight {
    GREEN, YELLOW, RED
}

data class CardRecommendation(
    val card: CreditCard,
    val daysOfFinancing: Int,
    val nextCutoffDateFormatted: String,
    val paymentDueDateFormatted: String,
    val daysUntilCutoff: Int,
    val isBestOption: Boolean,
    val recommendationReason: String,
    val trafficLight: TrafficLight,
    val isPaymentShiftedByHoliday: Boolean
)

data class CardCreditBalance(
    val creditLimit: Double,
    val totalOccupiedCredit: Double,
    val availableCredit: Double,
    val occupancyPercentage: Int,
    val regularDebt: Double,
    val totalMsiPendingBalance: Double,
    val currentCycleMsiCharge: Double,
    val totalPayments: Double
)

data class MsiSummary(
    val expense: Expense,
    val cardName: String,
    val card: CreditCard?,
    val monthlyPayment: Double,
    val totalPaidSoFar: Double,
    val remainingBalance: Double,
    val installmentsRemaining: Int,
    val progressPercent: Float,
    val completionDateString: String,
    val totalPurchaseAmount: Double,
    val isCompleted: Boolean,
    val isLastInstallmentPending: Boolean
)

data class CashFlowRelease(
    val monthYearLabel: String,
    val monthlyAmountFreed: Double,
    val finishingItems: List<String>
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
