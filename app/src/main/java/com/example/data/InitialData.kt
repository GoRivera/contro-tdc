package com.example.data

import com.example.data.model.*

object InitialData {
    private val now = System.currentTimeMillis()
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    val CARDS = listOf(
        CreditCard(
            id = 1L,
            name = "BBVA Azul",
            bank = "BBVA",
            cutoffDay = 18,
            paymentDueDay = 8,
            creditLimit = 45000.0,
            primaryColorHex = "#004481",
            secondaryColorHex = "#042C54",
            last4Digits = "4821",
            network = "Visa",
            isActive = true,
            isDepartmental = false,
            graceDays = 20,
            cardholderName = "G. RIVERA",
            annualInterestRatePercent = 54.5
        ),
        CreditCard(
            id = 2L,
            name = "LikeU",
            bank = "Santander",
            cutoffDay = 5,
            paymentDueDay = 25,
            creditLimit = 32000.0,
            primaryColorHex = "#EC0000",
            secondaryColorHex = "#9E0000",
            last4Digits = "9312",
            network = "Mastercard",
            isActive = true,
            isDepartmental = false,
            graceDays = 20,
            cardholderName = "G. RIVERA",
            annualInterestRatePercent = 49.0
        ),
        CreditCard(
            id = 3L,
            name = "Nu Morada",
            bank = "Nu México",
            cutoffDay = 24,
            paymentDueDay = 14,
            creditLimit = 28000.0,
            primaryColorHex = "#820AD1",
            secondaryColorHex = "#580391",
            last4Digits = "1054",
            network = "Mastercard",
            isActive = true,
            isDepartmental = false,
            graceDays = 20,
            cardholderName = "G. RIVERA",
            annualInterestRatePercent = 62.0
        ),
        CreditCard(
            id = 4L,
            name = "Liverpool Visa",
            bank = "Liverpool",
            cutoffDay = 15,
            paymentDueDay = 5,
            creditLimit = 20000.0,
            primaryColorHex = "#D81B60",
            secondaryColorHex = "#880E4F",
            last4Digits = "7749",
            network = "Visa",
            isActive = true,
            isDepartmental = true,
            graceDays = 20,
            cardholderName = "G. RIVERA",
            annualInterestRatePercent = 58.0
        )
    )

    val EXPENSES = listOf(
        Expense(
            id = 1L,
            cardId = 1L,
            concept = "MacBook Air M2 15\"",
            amount = 1999.00,
            dateMillis = now - (60 * DAY_MILLIS),
            beneficiary = "Personal",
            category = "Tecnología",
            isMsi = true,
            msiTotalMonths = 12,
            msiCurrentInstallment = 5,
            msiTotalPurchaseAmount = 23988.00,
            notes = "Compra en tienda Apple en línea con promoción 12 MSI",
            targetStatementMonth = "Septiembre 2026"
        ),
        Expense(
            id = 2L,
            cardId = 3L,
            concept = "Refrigerador Samsung Inverter",
            amount = 1450.00,
            dateMillis = now - (120 * DAY_MILLIS),
            beneficiary = "Hogar",
            category = "Hogar",
            isMsi = true,
            msiTotalMonths = 18,
            msiCurrentInstallment = 8,
            msiTotalPurchaseAmount = 26100.00,
            notes = "Buen Fin Liverpool con MSI Nu",
            targetStatementMonth = "Septiembre 2026"
        ),
        Expense(
            id = 3L,
            cardId = 2L,
            concept = "Llantas Michelin 4x",
            amount = 1200.00,
            dateMillis = now - (90 * DAY_MILLIS),
            beneficiary = "Personal",
            category = "Otros",
            isMsi = true,
            msiTotalMonths = 6,
            msiCurrentInstallment = 5,
            msiTotalPurchaseAmount = 7200.00,
            notes = "Taller Costco",
            targetStatementMonth = "Septiembre 2026"
        ),
        Expense(
            id = 4L,
            cardId = 1L,
            concept = "Supermercado HEB Semanal",
            amount = 2450.50,
            dateMillis = now - (2 * DAY_MILLIS),
            beneficiary = "Personal",
            category = "Despensa",
            isMsi = false,
            notes = "Despensa quincenal completa",
            targetStatementMonth = "Septiembre 2026"
        ),
        Expense(
            id = 5L,
            cardId = 2L,
            concept = "Cena Restaurante Sonora Grill",
            amount = 1850.00,
            dateMillis = now - (4 * DAY_MILLIS),
            beneficiary = "Ale",
            category = "Restaurantes",
            isMsi = false,
            notes = "Celebración aniversario",
            targetStatementMonth = "Septiembre 2026"
        ),
        Expense(
            id = 6L,
            cardId = 3L,
            concept = "Farmacia Guadalajara",
            amount = 680.00,
            dateMillis = now - (5 * DAY_MILLIS),
            beneficiary = "Memé",
            category = "Salud",
            isMsi = false,
            notes = "Medicamentos periódicos",
            targetStatementMonth = "Septiembre 2026"
        )
    )

    val PAYMENTS = listOf(
        Payment(
            id = 1L,
            cardId = 1L,
            concept = "Pago Total para no generar intereses",
            amount = 3500.00,
            dateMillis = now - (15 * DAY_MILLIS),
            sourcePayer = "Personal",
            targetStatementMonth = "Agosto 2026",
            notes = "Transferencia SPEI desde cuenta nómina"
        ),
        Payment(
            id = 2L,
            cardId = 2L,
            concept = "Abono Liquidación Ciclo",
            amount = 2500.00,
            dateMillis = now - (20 * DAY_MILLIS),
            sourcePayer = "Personal",
            targetStatementMonth = "Agosto 2026",
            notes = "Pago adelantado"
        )
    )

    val SUBSCRIPTIONS = listOf(
        Subscription(
            id = 1L,
            name = "Netflix Premium 4K",
            cardId = 3L,
            billingDayOfMonth = 14,
            totalMonthlyAmount = 299.00,
            category = "Streaming",
            startMonth = "Enero 2026",
            isActive = true,
            notes = "Plan 4 pantallas familiares",
            participantsSummary = "Ale ($150) / Personal ($149)",
            periodicity = "MENSUAL"
        ),
        Subscription(
            id = 2L,
            name = "Spotify Familiar",
            cardId = 1L,
            billingDayOfMonth = 18,
            totalMonthlyAmount = 199.00,
            category = "Música",
            startMonth = "Febrero 2026",
            isActive = true,
            notes = "6 cuentas activas",
            participantsSummary = "Memé ($50) / Poncho ($50) / Ale ($50) / Personal ($49)",
            periodicity = "MENSUAL"
        ),
        Subscription(
            id = 3L,
            name = "Apple One Familiar",
            cardId = 2L,
            billingDayOfMonth = 25,
            totalMonthlyAmount = 319.00,
            category = "Nube & Servicios",
            startMonth = "Marzo 2026",
            isActive = true,
            notes = "2TB iCloud + Music + TV",
            participantsSummary = "Personal ($319)",
            periodicity = "MENSUAL"
        ),
        Subscription(
            id = 4L,
            name = "Amazon Prime Anual",
            cardId = 1L,
            billingDayOfMonth = 12,
            totalMonthlyAmount = 899.00,
            category = "Compras",
            startMonth = "Mayo 2026",
            isActive = true,
            notes = "Envíos gratis y Prime Video",
            participantsSummary = "Personal",
            periodicity = "ANUAL"
        )
    )

    val FUEL_ENTRIES = listOf(
        FuelEntry(
            id = 1L,
            cardId = 1L,
            kmDriven = 485.0,
            fuelType = "Premium (Roja)",
            pricePerLiter = 25.80,
            litersLoaded = 42.5,
            totalCost = 1096.50,
            efficiencyKmPerL = 11.41,
            isDivided = false,
            personalShare = 1096.50,
            dividedWith = "",
            dividedCount = 1,
            dateMillis = now - (3 * DAY_MILLIS),
            notes = "Tanque lleno en Shell Insurgentes"
        ),
        FuelEntry(
            id = 2L,
            cardId = 2L,
            kmDriven = 520.0,
            fuelType = "Regular (Verde)",
            pricePerLiter = 23.90,
            litersLoaded = 45.0,
            totalCost = 1075.50,
            efficiencyKmPerL = 11.55,
            isDivided = true,
            personalShare = 537.75,
            dividedWith = "Ale (50%)",
            dividedCount = 2,
            dateMillis = now - (14 * DAY_MILLIS),
            notes = "Viaje en carretera a Querétaro"
        )
    )

    val SERVICE_ENTRIES = listOf(
        ServiceEntry(
            id = 1L,
            serviceType = "CFE (Luz)",
            dateMillis = now - (10 * DAY_MILLIS),
            amount = 1140.00,
            consumption = 340.0,
            notes = "Recibo CFE Bimestral Verano (A/C)",
            cardId = 1L
        ),
        ServiceEntry(
            id = 2L,
            serviceType = "Agua y Drenaje",
            dateMillis = now - (22 * DAY_MILLIS),
            amount = 380.00,
            consumption = 24.0,
            notes = "Consumo regular hogar",
            cardId = 1L
        ),
        ServiceEntry(
            id = 3L,
            serviceType = "Gas Natural",
            dateMillis = now - (40 * DAY_MILLIS),
            amount = 850.00,
            consumption = 0.0,
            notes = "Carga Naturgy tanque estacionario",
            cardId = 2L
        )
    )
}
