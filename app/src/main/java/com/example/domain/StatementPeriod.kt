package com.example.domain

/**
 * Representa un periodo de corte/estado de cuenta (mes + año) como un tipo con significado, en vez
 * de pasar por separado un nombre de mes (String) y un año (Int) sueltos o una cadena "Mes Año"
 * armada a mano en cada sitio — la causa raíz de varios bugs de fechas corregidos en esta app (año
 * duplicado en la descripción de MSI, año equivocado al cruzar de diciembre a enero, etc.).
 *
 * No reemplaza el campo `targetStatementMonth` persistido en Room/Firestore (eso requeriría una
 * migración de esquema); es una envoltura para trabajar con esos mismos datos de forma más segura
 * en el código nuevo, con igualdad y orden bien definidos en vez de comparar strings sueltas.
 */
data class StatementPeriod(val year: Int, val monthName: String) : Comparable<StatementPeriod> {

    val normalizedMonthName: String get() = CreditCardCalculator.normalizeMonth(monthName)

    /** Ej. "Septiembre 2026" */
    val label: String get() = "$normalizedMonthName $year"

    private val monthIndex: Int
        get() = MONTH_ORDER.indexOf(normalizedMonthName).let { if (it < 0) 0 else it }

    override fun compareTo(other: StatementPeriod): Int {
        val yearCompare = year.compareTo(other.year)
        return if (yearCompare != 0) yearCompare else monthIndex.compareTo(other.monthIndex)
    }

    override fun toString(): String = label

    companion object {
        private val MONTH_ORDER = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        /**
         * Construye el periodo a partir de los mismos datos que ya usan `extractYear`/
         * `normalizeMonth`: el campo `targetStatementMonth` de un Expense/Payment (o, si viene
         * vacío, la fecha real del registro).
         */
        fun from(dateMillis: Long, targetStatementMonth: String): StatementPeriod {
            val year = CreditCardCalculator.extractYear(dateMillis, targetStatementMonth)
            val monthSource = targetStatementMonth.ifBlank { CreditCardCalculator.extractYearMonth(dateMillis) }
            return StatementPeriod(year, CreditCardCalculator.normalizeMonth(monthSource))
        }
    }
}
