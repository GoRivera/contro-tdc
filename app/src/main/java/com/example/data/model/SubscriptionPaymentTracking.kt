package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscription_payment_trackings",
    foreignKeys = [
        ForeignKey(
            entity = Subscription::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subscriptionId"), Index("yearMonth")]
)
data class SubscriptionPaymentTracking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val yearMonth: String,         // e.g. "2026-09"
    val participantName: String,   // Nombre del participante
    val amountOwed: Double,
    val isPaid: Boolean = false,
    val paidDateMillis: Long? = null,
    val firestoreId: String = "" // Identificador estable (UUID) para sincronización con la nube
)
