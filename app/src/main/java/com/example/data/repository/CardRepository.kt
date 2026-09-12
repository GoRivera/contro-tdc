package com.example.data.repository

import com.example.data.dao.CreditCardDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FuelEntryDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.SubscriptionDao
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.FuelEntry
import com.example.data.model.Payment
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val cardDao: CreditCardDao,
    private val expenseDao: ExpenseDao,
    private val paymentDao: PaymentDao,
    private val subscriptionDao: SubscriptionDao,
    private val fuelEntryDao: FuelEntryDao
) {
    val allCards: Flow<List<CreditCard>> = cardDao.getAllCards()
    val activeCards: Flow<List<CreditCard>> = cardDao.getActiveCards()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val msiExpenses: Flow<List<Expense>> = expenseDao.getAllMsiExpenses()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
    val activeSubscriptions: Flow<List<Subscription>> = subscriptionDao.getActiveSubscriptions()
    val allFuelEntries: Flow<List<FuelEntry>> = fuelEntryDao.getAllFuelEntries()

    fun getFuelEntriesSince(sinceMillis: Long): Flow<List<FuelEntry>> =
        fuelEntryDao.getFuelEntriesSince(sinceMillis)

    suspend fun insertFuelEntry(entry: FuelEntry): Long = fuelEntryDao.insertFuelEntry(entry)
    suspend fun updateFuelEntry(entry: FuelEntry) = fuelEntryDao.updateFuelEntry(entry)
    suspend fun deleteFuelEntry(entry: FuelEntry) = fuelEntryDao.deleteFuelEntry(entry)
    suspend fun deleteFuelEntryById(id: Long) = fuelEntryDao.deleteFuelEntryById(id)

    fun getExpensesForStatement(month: String): Flow<List<Expense>> =
        expenseDao.getExpensesByStatementMonth(month)

    fun getPaymentsForStatement(month: String): Flow<List<Payment>> =
        paymentDao.getPaymentsByStatementMonth(month)

    fun getSubscriptionTrackingsForMonth(yearMonth: String): Flow<List<SubscriptionPaymentTracking>> =
        subscriptionDao.getTrackingsForMonth(yearMonth)

    suspend fun insertCard(card: CreditCard): Long = cardDao.insertCard(card)
    suspend fun updateCard(card: CreditCard) = cardDao.updateCard(card)
    suspend fun deleteCard(card: CreditCard) = cardDao.deleteCard(card)
    suspend fun deleteCardById(cardId: Long) = cardDao.deleteCardById(cardId)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)
    suspend fun deletePaymentById(id: Long) = paymentDao.deletePaymentById(id)

    suspend fun insertSubscription(subscription: Subscription): Long =
        subscriptionDao.insertSubscription(subscription)
    suspend fun updateSubscription(subscription: Subscription) =
        subscriptionDao.updateSubscription(subscription)
    suspend fun deleteSubscription(subscription: Subscription) =
        subscriptionDao.deleteSubscription(subscription)

    suspend fun insertTracking(tracking: SubscriptionPaymentTracking) =
        subscriptionDao.insertTracking(tracking)
    suspend fun insertTrackings(trackings: List<SubscriptionPaymentTracking>) =
        subscriptionDao.insertTrackings(trackings)
    suspend fun updateTracking(tracking: SubscriptionPaymentTracking) =
        subscriptionDao.updateTracking(tracking)
}
