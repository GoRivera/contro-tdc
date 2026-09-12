package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,               // e.g. "Like U", "Banorte Platinum", "Oro BBVA"
    val bank: String,               // e.g. "Santander", "Banorte", "BBVA", "Nu", "Banamex"
    val cutoffDay: Int,             // Day of the month for statement closing (1-31)
    val paymentDueDay: Int,         // Day of the month for payment deadline (1-31)
    val creditLimit: Double = 0.0,  // Credit limit in MXN
    val primaryColorHex: Long,      // Primary color for card gradient
    val secondaryColorHex: Long,    // Secondary color for card gradient
    val last4Digits: String = "••••",
    val network: String = "Mastercard", // "Visa", "Mastercard", "Amex", "Carnet", "Departamental"
    val isActive: Boolean = true,
    val isDepartmental: Boolean = false, // Requisito 9 & 10: Tarjeta departamental vs crédito bancario
    val graceDays: Int = 20,             // Requisito 7: Días de gracia entre corte y fecha límite de pago
    val cardholderName: String = "TITULAR" // Nombre del titular de la tarjeta
)
