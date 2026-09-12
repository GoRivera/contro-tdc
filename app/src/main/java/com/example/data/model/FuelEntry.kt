package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId"), Index("dateMillis"), Index("expenseId")]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val expenseId: Long? = null,
    val kmDriven: Double,                 // Kilometraje recorrido
    val fuelType: String,                 // "Premium (Roja)" o "Regular (Verde)"
    val pricePerLiter: Double,            // Precio por litro ($/L)
    val litersLoaded: Double,             // Cantidad de litros cargados (hasta 5 decimales)
    val totalCost: Double,                // Costo total = litros * precio
    val efficiencyKmPerL: Double,         // Rendimiento = kmDriven / litersLoaded
    val isDivided: Boolean = false,       // Gasto dividido o personal
    val personalShare: Double = 0.0,      // Monto personal si fue dividido
    val dividedWith: String = "",         // Con quién se dividió (texto libre informativo)
    val dividedCount: Int = 2,            // Entre cuántas personas se divide el costo total (incluyéndote), usado para calcular personalShare
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val firestoreId: String = ""          // Identificador estable (UUID) para sincronización con la nube
) {
    /**
     * Costo monetario por kilómetro recorrido ($/km).
     */
    val costPerKm: Double
        get() = if (kmDriven > 0.0) totalCost / kmDriven else 0.0
}
