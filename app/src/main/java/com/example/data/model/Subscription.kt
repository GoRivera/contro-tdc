package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("cardId")]
)
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                  // Ej: "Netflix Familiar 4K", "Spotify Duo", "iCloud+ 2TB"
    val cardId: Long,                  // Tarjeta donde está domiciliada
    val billingDayOfMonth: Int,        // Día del mes en que se efectúa el cargo (1 - 31)
    val totalMonthlyAmount: Double,    // Monto mensual total en MXN
    val category: String = "Streaming",// "Streaming", "Música", "Almacenamiento", "Software", "Servicios", "Salud"
    val startMonth: String = "2026-01",// Mes de inicio de la suscripción
    val isActive: Boolean = true,
    val notes: String = "",
    val participantsSummary: String = "", // Cadena legible de participantes con sus montos: ej. "Participante 1: $100.00, Participante 2: $100.00"
    val periodicity: String = "MENSUAL"   // "MENSUAL", "BIMESTRAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"
) {
    val monthlyEquivalentAmount: Double
        get() = when (periodicity.uppercase()) {
            "BIMESTRAL" -> totalMonthlyAmount / 2.0
            "TRIMESTRAL" -> totalMonthlyAmount / 3.0
            "SEMESTRAL" -> totalMonthlyAmount / 6.0
            "ANUAL" -> totalMonthlyAmount / 12.0
            else -> totalMonthlyAmount
        }
}

data class SubscriptionParticipant(
    val name: String,
    val amount: Double
)
