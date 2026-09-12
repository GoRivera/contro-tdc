package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId"), Index("dateMillis"), Index("subscriptionId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val concept: String,
    val amount: Double,
    val dateMillis: Long,
    val beneficiary: String = "Personal", // "Personal", "Memé", "Ale", "Poncho", "Familiar", "Otro"
    val category: String = "General",     // "Despensa", "Servicios", "Tecnología", "Gasolina", "Restaurantes", "Salud", "Hogar", "Otros"
    val isMsi: Boolean = false,
    val msiTotalMonths: Int = 1,          // e.g. 3, 6, 9, 12, 15, 18, 24
    val msiCurrentInstallment: Int = 1,   // e.g. 8 (out of 15)
    val msiTotalPurchaseAmount: Double = 0.0, // Total purchase cost, or amount * msiTotalMonths
    val notes: String = "",
    val targetStatementMonth: String = "", // e.g. "2026-08", "2026-09"
    val isSubscription: Boolean = false,   // Requisito 5: Cargo recurrente de suscripción
    val subscriptionId: Long? = null,      // Id de la suscripción asociada
    val firestoreId: String = ""           // Identificador estable (UUID) para sincronización con la nube
)
