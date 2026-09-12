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
    val dividedWith: String = "",         // Con quién se dividió
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    /**
     * Costo monetario por kilómetro recorrido ($/km).
     */
    val costPerKm: Double
        get() = if (kmDriven > 0.0) totalCost / kmDriven else 0.0
}
