package com.example.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.data.AppDatabase
import com.example.domain.CreditCardCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Widget de pantalla de inicio (item 15): muestra la tarjeta con el pago más próximo a vencer y en
 * cuántos días. Lee directo de Room (igual que PaymentReminderWorker) porque un widget puede
 * dibujarse sin que la app/ViewModel esté corriendo.
 */
class NextPaymentGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
        val activeCards = db.creditCardDao().getActiveCards().first()
        val today = Date()

        val nearest = activeCards
            .map { card ->
                val (_, dueDate) = CreditCardCalculator.calculateCycleDates(card, today)
                Triple(card.name, dueDate, TimeUnit.MILLISECONDS.toDays(dueDate.time - today.time))
            }
            .minByOrNull { it.third }

        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = "Control TDC",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                )
                if (nearest == null) {
                    Text(text = "Sin tarjetas registradas", style = TextStyle(fontSize = 12.sp))
                } else {
                    val (cardName, _, days) = nearest
                    Text(text = cardName, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    Text(
                        text = when {
                            days <= 0 -> "El pago vence hoy"
                            days == 1L -> "Vence mañana"
                            else -> "Vence en $days días"
                        },
                        style = TextStyle(fontSize = 13.sp)
                    )
                }
            }
        }
    }
}
