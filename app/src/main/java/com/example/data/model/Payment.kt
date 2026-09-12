package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId"), Index("dateMillis")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val concept: String,                  // "Pago TDC", "Bonificación Hot Sale", "Saldo a favor Memé", "Devolución Cashi", "Abono Ale"
    val amount: Double,
    val dateMillis: Long,
    val sourcePayer: String = "Personal", // "Personal", "Memé", "Ale", "Poncho", "Banco"
    val targetStatementMonth: String = "",// e.g. "2026-08", "2026-09"
    val notes: String = "",
    val firestoreId: String = ""          // Identificador estable (UUID) para sincronización con la nube
)
