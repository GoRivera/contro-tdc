package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.CreditCard

private const val CHANNEL_ID = "payment_reminders"

/**
 * Notificaciones de recordatorio de fecha límite de pago. Un solo canal para todas las tarjetas;
 * cada tarjeta usa su propio id como notificationId para no pisarse entre sí.
 */
object NotificationHelper {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de pago",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Avisa cuando se acerca la fecha límite de pago de una tarjeta"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Muestra una notificación por cada tarjeta con pago próximo. Requiere el permiso
     * POST_NOTIFICATIONS en Android 13+ (si no está concedido, simplemente no se muestra nada).
     */
    fun showPaymentReminders(context: Context, upcoming: List<Pair<CreditCard, Long>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        upcoming.forEach { (card, daysUntilDue) ->
            val openIntent = android.content.Intent(context, MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                card.id.toInt(),
                openIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val bodyText = when {
                daysUntilDue <= 0 -> "El pago de ${card.name} vence hoy."
                daysUntilDue == 1L -> "El pago de ${card.name} vence mañana."
                else -> "El pago de ${card.name} vence en $daysUntilDue días."
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Recordatorio de pago")
                .setContentText(bodyText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            manager.notify(card.id.toInt(), notification)
        }
    }
}
