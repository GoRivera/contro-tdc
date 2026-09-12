package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de un consumo de servicio (agua, luz o gas). Este módulo es intencionalmente
 * independiente de las tarjetas de crédito: no genera cargos, no afecta el saldo ocupado de
 * ninguna tarjeta, ni aparece en los estados de cuenta. Es solo un control de consumos.
 */
@Entity(tableName = "service_entries")
data class ServiceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceType: String,        // "AGUA", "LUZ" o "GAS"
    val dateMillis: Long,           // Mes al que corresponde el consumo (agua/luz) o fecha de carga (gas)
    val amount: Double,             // Monto a pagar (agua/luz) o monto cargado (gas)
    val consumption: Double = 0.0,  // Agua: metros cúbicos (m³). Luz: kWh. Gas: no aplica.
    val notes: String = "",
    val firestoreId: String = ""    // Identificador estable (UUID) para sincronización con la nube
)

object ServiceType {
    const val AGUA = "AGUA"
    const val LUZ = "LUZ"
    const val GAS = "GAS"

    val ALL = listOf(AGUA, LUZ, GAS)

    fun displayName(type: String): String = when (type) {
        AGUA -> "Agua"
        LUZ -> "Luz"
        GAS -> "Gas"
        else -> type
    }

    fun consumptionUnit(type: String): String = when (type) {
        AGUA -> "m³"
        LUZ -> "kWh"
        else -> ""
    }

    fun hasConsumption(type: String): Boolean = type == AGUA || type == LUZ
}
