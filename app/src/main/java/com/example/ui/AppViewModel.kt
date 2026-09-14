package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.InitialData
import com.example.data.model.*
import com.example.domain.CreditCardCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {

    private val _cards = MutableStateFlow<List<CreditCard>>(InitialData.CARDS)
    val cards: StateFlow<List<CreditCard>> = _cards.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(InitialData.EXPENSES)
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(InitialData.PAYMENTS)
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<Subscription>>(InitialData.SUBSCRIPTIONS)
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    private val _fuelEntries = MutableStateFlow<List<FuelEntry>>(InitialData.FUEL_ENTRIES)
    val fuelEntries: StateFlow<List<FuelEntry>> = _fuelEntries.asStateFlow()

    private val _serviceEntries = MutableStateFlow<List<ServiceEntry>>(InitialData.SERVICE_ENTRIES)
    val serviceEntries: StateFlow<List<ServiceEntry>> = _serviceEntries.asStateFlow()

    // Current selected screen in navigation
    private val _selectedTab = MutableStateFlow("recommendation")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    // Recommendations for today
    fun getRecommendations(): List<CardRecommendation> {
        return CreditCardCalculator.evaluateCardsForPurchase(_cards.value)
    }

    // Balances
    fun getCardBalance(card: CreditCard): CardCreditBalance {
        return CreditCardCalculator.calculateCardCreditBalance(card, _expenses.value, _payments.value)
    }

    // MSI Summaries
    fun getMsiSummaries(): List<MsiSummary> {
        val currentCards = _cards.value.associateBy { it.id }
        return _expenses.value
            .filter { it.isMsi }
            .map { exp ->
                CreditCardCalculator.computeMsiSummary(exp, currentCards[exp.cardId])
            }
    }

    // Cashflow releases
    fun getCashFlowReleases(): List<CashFlowRelease> {
        return CreditCardCalculator.projectCashFlowRelease(getMsiSummaries())
    }

    // Total required to avoid interest
    fun getTotalToPayToAvoidInterest(): Double {
        val regular = _expenses.value.filter { !it.isMsi }.sumOf { it.amount }
        val msiCycle = _expenses.value.filter { it.isMsi }.sumOf { it.amount }
        val totalPaid = _payments.value.sumOf { it.amount }
        return kotlin.math.max(0.0, (regular + msiCycle) - totalPaid)
    }

    // Beneficiary breakdown
    fun getBeneficiaryBreakdown(): Map<String, Double> {
        return _expenses.value.groupBy { it.beneficiary }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }

    // Actions
    fun addExpense(
        cardId: Long,
        concept: String,
        amount: Double,
        beneficiary: String,
        category: String,
        isMsi: Boolean,
        msiMonths: Int,
        notes: String
    ) {
        val newId = (_expenses.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newExpense = Expense(
            id = newId,
            cardId = cardId,
            concept = concept,
            amount = if (isMsi && msiMonths > 0) amount / msiMonths else amount,
            beneficiary = beneficiary,
            category = category,
            isMsi = isMsi,
            msiTotalMonths = if (isMsi) msiMonths else 1,
            msiCurrentInstallment = 1,
            msiTotalPurchaseAmount = if (isMsi) amount else 0.0,
            notes = notes
        )
        _expenses.update { listOf(newExpense) + it }
    }

    fun deleteExpense(id: Long) {
        _expenses.update { it.filter { exp -> exp.id != id } }
    }

    fun addPayment(
        cardId: Long,
        concept: String,
        amount: Double,
        sourcePayer: String,
        notes: String
    ) {
        val newId = (_payments.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newPayment = Payment(
            id = newId,
            cardId = cardId,
            concept = concept,
            amount = amount,
            sourcePayer = sourcePayer,
            notes = notes
        )
        _payments.update { listOf(newPayment) + it }
    }

    fun deletePayment(id: Long) {
        _payments.update { it.filter { p -> p.id != id } }
    }

    fun addCard(
        name: String,
        bank: String,
        cutoffDay: Int,
        paymentDueDay: Int,
        creditLimit: Double,
        primaryColorHex: String,
        secondaryColorHex: String,
        last4Digits: String,
        isDepartmental: Boolean,
        interestRate: Double
    ) {
        val newId = (_cards.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newCard = CreditCard(
            id = newId,
            name = name,
            bank = bank,
            cutoffDay = cutoffDay,
            paymentDueDay = paymentDueDay,
            creditLimit = creditLimit,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            last4Digits = last4Digits,
            isDepartmental = isDepartmental,
            annualInterestRatePercent = interestRate
        )
        _cards.update { it + newCard }
    }

    fun toggleCardActive(id: Long) {
        _cards.update { list ->
            list.map { card ->
                if (card.id == id) card.copy(isActive = !card.isActive) else card
            }
        }
    }

    fun addSubscription(
        name: String,
        cardId: Long,
        billingDay: Int,
        monthlyAmount: Double,
        category: String,
        participants: String
    ) {
        val newId = (_subscriptions.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val sub = Subscription(
            id = newId,
            name = name,
            cardId = cardId,
            billingDayOfMonth = billingDay,
            totalMonthlyAmount = monthlyAmount,
            category = category,
            participantsSummary = participants
        )
        _subscriptions.update { listOf(sub) + it }
    }

    fun addFuelEntry(
        cardId: Long,
        kmDriven: Double,
        pricePerLiter: Double,
        litersLoaded: Double,
        fuelType: String,
        isDivided: Boolean,
        dividedWith: String
    ) {
        val newId = (_fuelEntries.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val totalCost = pricePerLiter * litersLoaded
        val efficiency = if (litersLoaded > 0) kmDriven / litersLoaded else 0.0
        val personalShare = if (isDivided) totalCost / 2.0 else totalCost
        val entry = FuelEntry(
            id = newId,
            cardId = cardId,
            kmDriven = kmDriven,
            fuelType = fuelType,
            pricePerLiter = pricePerLiter,
            litersLoaded = litersLoaded,
            totalCost = totalCost,
            efficiencyKmPerL = efficiency,
            isDivided = isDivided,
            personalShare = personalShare,
            dividedWith = dividedWith
        )
        _fuelEntries.update { listOf(entry) + it }
    }

    fun addServiceEntry(
        serviceType: String,
        amount: Double,
        consumption: Double,
        notes: String,
        cardId: Long
    ) {
        val newId = (_serviceEntries.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val entry = ServiceEntry(
            id = newId,
            serviceType = serviceType,
            amount = amount,
            consumption = consumption,
            notes = notes,
            cardId = cardId
        )
        _serviceEntries.update { listOf(entry) + it }
    }
}
