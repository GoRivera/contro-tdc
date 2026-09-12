package com.example.domain

data class MexicanBankInfo(
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val defaultNetwork: String = "Mastercard",
    val isDepartmental: Boolean = false
)

object MexicanBanks {
    val banks = listOf(
        MexicanBankInfo("BBVA", 0xFF004481L, 0xFF1464A5L, "Visa"),
        MexicanBankInfo("Santander", 0xFFEC0000L, 0xFF990000L, "Mastercard"),
        MexicanBankInfo("Banamex", 0xFF002D72L, 0xFF001F3FL, "Mastercard"),
        MexicanBankInfo("Banorte", 0xFFEB0029L, 0xFF1B1D22L, "Mastercard"),
        MexicanBankInfo("Nu México", 0xFF820AD1L, 0xFF4C0677L, "Mastercard"),
        MexicanBankInfo("HSBC", 0xFFDB0011L, 0xFF222222L, "Visa"),
        MexicanBankInfo("Scotiabank", 0xFFED0722L, 0xFF880000L, "Visa"),
        MexicanBankInfo("American Express", 0xFF0077A6L, 0xFF002F6CL, "Amex"),
        MexicanBankInfo("Plata Card", 0xFF2A2E35L, 0xFF5C677DL, "Mastercard"),
        MexicanBankInfo("Inbursa", 0xFF003865L, 0xFFC8102EL, "Visa"),
        MexicanBankInfo("Banco Azteca", 0xFF007A33L, 0xFF004D20L, "Mastercard"),
        MexicanBankInfo("BanCoppel", 0xFF005691L, 0xFFFFD100L, "Visa"),
        MexicanBankInfo("Hey Banco", 0xFF1A1A1AL, 0xFFFFB800L, "Visa"),
        MexicanBankInfo("Banregio", 0xFFFF5A00L, 0xFFCC4400L, "Visa"),
        MexicanBankInfo("Afirme", 0xFF00833EL, 0xFF005A2BL, "Mastercard"),
        MexicanBankInfo("Liverpool", 0xFFE10098L, 0xFF88005CL, "Visa", isDepartmental = true),
        MexicanBankInfo("Palacio de Hierro", 0xFFB8860BL, 0xFF7A5806L, "Departamental", isDepartmental = true),
        MexicanBankInfo("Sears", 0xFF0A2540L, 0xFFB21E27L, "Departamental", isDepartmental = true),
        MexicanBankInfo("Mercado Pago", 0xFF009EE3L, 0xFF006699L, "Visa"),
        MexicanBankInfo("RappiCard", 0xFFFF441FL, 0xFF1F1F1FL, "Visa"),
        MexicanBankInfo("Klar", 0xFF00D1B2L, 0xFF002244L, "Mastercard"),
        MexicanBankInfo("Stori", 0xFF00C853L, 0xFF003322L, "Mastercard"),
        MexicanBankInfo("INVEX", 0xFF002D62L, 0xFF8A0027L, "Mastercard"),
        MexicanBankInfo("Costco", 0xFF005DAAL, 0xFFE31837L, "Visa", isDepartmental = true),
        MexicanBankInfo("Suburbia", 0xFF6A1B9AL, 0xFF4A148CL, "Departamental", isDepartmental = true)
    )

    fun findMatching(query: String): List<MexicanBankInfo> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return banks
        return banks.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    fun findExactOrBest(query: String): MexicanBankInfo? {
        val trimmed = query.trim()
        return banks.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
            ?: banks.firstOrNull { it.name.contains(trimmed, ignoreCase = true) }
    }
}
