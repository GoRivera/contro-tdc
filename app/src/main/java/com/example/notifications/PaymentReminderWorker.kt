package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.domain.CreditCardCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "payment_reminder_check"

/**
 * Revisa, una vez al día, si alguna tarjeta activa tiene su fecha límite de pago a 3 días o menos
 * (o ya vencida) y muestra un recordatorio. No requiere que la app esté abierta: por eso vive en
 * WorkManager y no en el ViewModel, que solo existe mientras la Activity está viva.
 */
class PaymentReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
            val activeCards = db.creditCardDao().getActiveCards().first()
            val today = Date()

            val upcoming = activeCards.mapNotNull { card ->
                val (_, dueDate) = CreditCardCalculator.calculateCycleDates(card, today)
                val daysUntilDue = TimeUnit.MILLISECONDS.toDays(dueDate.time - today.time)
                if (daysUntilDue in 0..3) card to daysUntilDue else null
            }

            if (upcoming.isNotEmpty()) {
                NotificationHelper.showPaymentReminders(applicationContext, upcoming)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PaymentReminderWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
