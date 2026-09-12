package com.example.domain

import com.example.data.model.CreditCard

enum class CardFinishType {
    MATTE,
    METALLIC_BRUSHED,
    GLOSSY_VIBRANT,
    NEON_PURPLE,
    CENTURION_STEEL,
    CARBON_SLATE,
    LUXURY_GOLD;

    fun getDisplayName(): String = when (this) {
        MATTE -> "Mate Satinado"
        METALLIC_BRUSHED -> "Titanio Cepillado"
        GLOSSY_VIBRANT -> "Brillo Institucional"
        NEON_PURPLE -> "Terciopelo Neón"
        CENTURION_STEEL -> "Acero Centurión"
        CARBON_SLATE -> "Obsidiana / Carbón"
        LUXURY_GOLD -> "Dorado"
    }
}

data class RealCardBranding(
    val bankDisplayName: String,
    val cardModelName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val finishType: CardFinishType,
    val network: String,
    val styleNotes: String
) {
    val finishDisplayName: String get() = finishType.getDisplayName()
}

object CardBrandingEngine {

    /**
     * Intelligent matching engine that recognizes Mexican bank credit cards,
     * departmental cards, and fintechs, replicating their real-life physical look.
     */
    fun matchRealCardBranding(cardName: String, bank: String, network: String = "Visa"): RealCardBranding {
        val combined = "$cardName $bank".lowercase()

        return when {
            // BBVA
            combined.contains("bbva") -> when {
                combined.contains("platino") || combined.contains("platinum") -> RealCardBranding(
                    bankDisplayName = "BBVA",
                    cardModelName = "Platino",
                    primaryColorHex = 0xFF2C3238,
                    secondaryColorHex = 0xFF14171A,
                    accentColorHex = 0xFF00A9E0,
                    finishType = CardFinishType.METALLIC_BRUSHED,
                    network = "Visa",
                    styleNotes = "Acabado titanio cepillado con vivo BBVA azul cielo"
                )
                combined.contains("oro") || combined.contains("gold") -> RealCardBranding(
                    bankDisplayName = "BBVA",
                    cardModelName = "Oro",
                    primaryColorHex = 0xFFB8860B,
                    secondaryColorHex = 0xFF7A5900,
                    accentColorHex = 0xFFFFD700,
                    finishType = CardFinishType.LUXURY_GOLD,
                    network = "Visa",
                    styleNotes = "Acabado dorado metálico satinado"
                )
                combined.contains("infinite") || combined.contains("black") -> RealCardBranding(
                    bankDisplayName = "BBVA",
                    cardModelName = "Infinite",
                    primaryColorHex = 0xFF121314,
                    secondaryColorHex = 0xFF000000,
                    accentColorHex = 0xFFE0E0E0,
                    finishType = CardFinishType.CARBON_SLATE,
                    network = "Visa",
                    styleNotes = "Negro obsidiana mate con bordes reflectivos"
                )
                else -> RealCardBranding(
                    bankDisplayName = "BBVA",
                    cardModelName = "Azul",
                    primaryColorHex = 0xFF004481,
                    secondaryColorHex = 0xFF042C54,
                    accentColorHex = 0xFF00A9E0,
                    finishType = CardFinishType.GLOSSY_VIBRANT,
                    network = "Visa",
                    styleNotes = "Azul marino icónico BBVA con corte diagonal"
                )
            }

            // SANTANDER
            combined.contains("santander") -> when {
                combined.contains("likeu") || combined.contains("like u") -> when {
                    combined.contains("azul") || combined.contains("blue") -> RealCardBranding(
                        bankDisplayName = "Santander",
                        cardModelName = "LikeU Azul",
                        primaryColorHex = 0xFF0072CE,
                        secondaryColorHex = 0xFF003865,
                        accentColorHex = 0xFFEC0000,
                        finishType = CardFinishType.MATTE,
                        network = "Mastercard",
                        styleNotes = "LikeU Personalizable Azul Océano"
                    )
                    combined.contains("rosa") || combined.contains("pink") -> RealCardBranding(
                        bankDisplayName = "Santander",
                        cardModelName = "LikeU Rosa",
                        primaryColorHex = 0xFFE6007E,
                        secondaryColorHex = 0xFF8A004B,
                        accentColorHex = 0xFFFFFFFF,
                        finishType = CardFinishType.MATTE,
                        network = "Mastercard",
                        styleNotes = "LikeU Cáncer de Mama Rosa Mate"
                    )
                    combined.contains("verde") || combined.contains("green") -> RealCardBranding(
                        bankDisplayName = "Santander",
                        cardModelName = "LikeU Verde",
                        primaryColorHex = 0xFF009639,
                        secondaryColorHex = 0xFF004D1D,
                        accentColorHex = 0xFFFFFFFF,
                        finishType = CardFinishType.MATTE,
                        network = "Mastercard",
                        styleNotes = "LikeU Reforestación Verde Bosque"
                    )
                    else -> RealCardBranding(
                        bankDisplayName = "Santander",
                        cardModelName = "LikeU Rojo",
                        primaryColorHex = 0xFFEC0000,
                        secondaryColorHex = 0xFF9E0000,
                        accentColorHex = 0xFFFFFFFF,
                        finishType = CardFinishType.GLOSSY_VIBRANT,
                        network = "Mastercard",
                        styleNotes = "Rojo emblemático Santander con relieve de flama"
                    )
                }
                combined.contains("fiesta rewards") -> RealCardBranding(
                    bankDisplayName = "Santander",
                    cardModelName = "Fiesta Rewards",
                    primaryColorHex = 0xFF0A2240,
                    secondaryColorHex = 0xFF031021,
                    accentColorHex = 0xFFD4AF37,
                    finishType = CardFinishType.GLOSSY_VIBRANT,
                    network = "Visa",
                    styleNotes = "Azul profundo con escudo dorado Fiesta Americana"
                )
                combined.contains("aeromexico") -> RealCardBranding(
                    bankDisplayName = "Santander",
                    cardModelName = "Aeroméxico",
                    primaryColorHex = 0xFF00205B,
                    secondaryColorHex = 0xFF0B1325,
                    accentColorHex = 0xFFD11242,
                    finishType = CardFinishType.METALLIC_BRUSHED,
                    network = "Visa",
                    styleNotes = "Azul aero con destellos plateados y Caballero Águila"
                )
                else -> RealCardBranding(
                    bankDisplayName = "Santander",
                    cardModelName = "Crédito",
                    primaryColorHex = 0xFFEC0000,
                    secondaryColorHex = 0xFF8A0000,
                    accentColorHex = 0xFFFFFFFF,
                    finishType = CardFinishType.GLOSSY_VIBRANT,
                    network = "Mastercard",
                    styleNotes = "Rojo Santander auténtico con franja estilizada"
                )
            }

            // BANAMEX
            combined.contains("citibanamex") || combined.contains("banamex") -> when {
                combined.contains("costco") -> RealCardBranding(
                    bankDisplayName = "Banamex",
                    cardModelName = "Costco",
                    primaryColorHex = 0xFF005DAA,
                    secondaryColorHex = 0xFF003057,
                    accentColorHex = 0xFFE01A22,
                    finishType = CardFinishType.MATTE,
                    network = "Visa",
                    styleNotes = "Azul Costco con franja roja de reembolso"
                )
                combined.contains("oro") || combined.contains("gold") -> RealCardBranding(
                    bankDisplayName = "Banamex",
                    cardModelName = "Oro",
                    primaryColorHex = 0xFFC29B38,
                    secondaryColorHex = 0xFF7D6017,
                    accentColorHex = 0xFFED1C24,
                    finishType = CardFinishType.LUXURY_GOLD,
                    network = "Mastercard",
                    styleNotes = "Oro brillante con arco rojo Banamex"
                )
                combined.contains("platino") || combined.contains("platinum") || combined.contains("prestige") -> RealCardBranding(
                    bankDisplayName = "Banamex",
                    cardModelName = "Platino",
                    primaryColorHex = 0xFF374151,
                    secondaryColorHex = 0xFF111827,
                    accentColorHex = 0xFFED1C24,
                    finishType = CardFinishType.METALLIC_BRUSHED,
                    network = "Mastercard",
                    styleNotes = "Gris platinado cepillado"
                )
                else -> RealCardBranding(
                    bankDisplayName = "Banamex",
                    cardModelName = "Clásica",
                    primaryColorHex = 0xFF002F6C,
                    secondaryColorHex = 0xFF001738,
                    accentColorHex = 0xFFED1C24,
                    finishType = CardFinishType.GLOSSY_VIBRANT,
                    network = "Mastercard",
                    styleNotes = "Azul real Banamex con arco rojo institucional"
                )
            }

            // NU / NUBANK
            Regex("\\bnu\\b").containsMatchIn(combined) || combined.contains("nubank") -> when {
                combined.contains("ultravioleta") -> RealCardBranding(
                    bankDisplayName = "Nu",
                    cardModelName = "Ultravioleta",
                    primaryColorHex = 0xFF1C0D2E,
                    secondaryColorHex = 0xFF08020F,
                    accentColorHex = 0xFFB347EB,
                    finishType = CardFinishType.CARBON_SLATE,
                    network = "Mastercard",
                    styleNotes = "Metal negro con borde láser ultravioleta"
                )
                else -> RealCardBranding(
                    bankDisplayName = "Nu",
                    cardModelName = "Crédito",
                    primaryColorHex = 0xFF820AD1,
                    secondaryColorHex = 0xFF580391,
                    accentColorHex = 0xFFFFFFFF,
                    finishType = CardFinishType.NEON_PURPLE,
                    network = "Mastercard",
                    styleNotes = "Morado Nubank de tacto terciopelo y letras curvas"
                )
            }

            // AMERICAN EXPRESS
            combined.contains("amex") || combined.contains("american express") -> when {
                combined.contains("platinum") || combined.contains("platino") -> RealCardBranding(
                    bankDisplayName = "American Express",
                    cardModelName = "The Platinum Card",
                    primaryColorHex = 0xFF9E9E9E,
                    secondaryColorHex = 0xFF424242,
                    accentColorHex = 0xFF2196F3,
                    finishType = CardFinishType.CENTURION_STEEL,
                    network = "American Express",
                    styleNotes = "Acero inoxidable pulido con grabado del Centurión Romano"
                )
                combined.contains("gold") || combined.contains("oro") -> RealCardBranding(
                    bankDisplayName = "American Express",
                    cardModelName = "Gold Card",
                    primaryColorHex = 0xFFD4AF37,
                    secondaryColorHex = 0xFF8C6D1F,
                    accentColorHex = 0xFF002663,
                    finishType = CardFinishType.LUXURY_GOLD,
                    network = "American Express",
                    styleNotes = "Oro antiguo con patrón guilloché tradicional Amex"
                )
                combined.contains("centurion") || combined.contains("black") -> RealCardBranding(
                    bankDisplayName = "American Express",
                    cardModelName = "Centurion",
                    primaryColorHex = 0xFF171717,
                    secondaryColorHex = 0xFF050505,
                    accentColorHex = 0xFFCCCCCC,
                    finishType = CardFinishType.CARBON_SLATE,
                    network = "American Express",
                    styleNotes = "Titanio anodizado negro mate de lujo"
                )
                else -> RealCardBranding(
                    bankDisplayName = "American Express",
                    cardModelName = "Green Card",
                    primaryColorHex = 0xFF2E6C38,
                    secondaryColorHex = 0xFF143B1B,
                    accentColorHex = 0xFF002663,
                    finishType = CardFinishType.CENTURION_STEEL,
                    network = "American Express",
                    styleNotes = "Verde monetario clásico Amex con Centurión central"
                )
            }

            // BANORTE
            combined.contains("banorte") -> when {
                combined.contains("oro") || combined.contains("gold") -> RealCardBranding(
                    bankDisplayName = "Banorte",
                    cardModelName = "Oro",
                    primaryColorHex = 0xFFC09B38,
                    secondaryColorHex = 0xFF694F10,
                    accentColorHex = 0xFFEB0029,
                    finishType = CardFinishType.LUXURY_GOLD,
                    network = "Visa",
                    styleNotes = "Dorado metálico con sello rojo Banorte"
                )
                combined.contains("platino") || combined.contains("marriott") -> RealCardBranding(
                    bankDisplayName = "Banorte",
                    cardModelName = "Platino",
                    primaryColorHex = 0xFF2B2D42,
                    secondaryColorHex = 0xFF12131C,
                    accentColorHex = 0xFFEB0029,
                    finishType = CardFinishType.METALLIC_BRUSHED,
                    network = "Visa",
                    styleNotes = "Gris grafito con ribete rojo"
                )
                else -> RealCardBranding(
                    bankDisplayName = "Banorte",
                    cardModelName = "Clásica",
                    primaryColorHex = 0xFFEB0029,
                    secondaryColorHex = 0xFF8A0014,
                    accentColorHex = 0xFFFFFFFF,
                    finishType = CardFinishType.GLOSSY_VIBRANT,
                    network = "Visa",
                    styleNotes = "Rojo pasión Banorte con isotipo institucional"
                )
            }

            // HSBC
            combined.contains("hsbc") -> when {
                combined.contains("zero") -> RealCardBranding(
                    bankDisplayName = "HSBC",
                    cardModelName = "Zero",
                    primaryColorHex = 0xFF2A2A2A,
                    secondaryColorHex = 0xFF121212,
                    accentColorHex = 0xFFDB0011,
                    finishType = CardFinishType.MATTE,
                    network = "Mastercard",
                    styleNotes = "Negro minimalista mate con hexágono rojo HSBC"
                )
                combined.contains("viva") -> RealCardBranding(
                    bankDisplayName = "HSBC",
                    cardModelName = "Viva",
                    primaryColorHex = 0xFF009639,
                    secondaryColorHex = 0xFF003815,
                    accentColorHex = 0xFFDB0011,
                    finishType = CardFinishType.GLOSSY_VIBRANT,
                    network = "Mastercard",
                    styleNotes = "Verde esmeralda con logo Viva Aerobus"
                )
                else -> RealCardBranding(
                    bankDisplayName = "HSBC",
                    cardModelName = "Clásica",
                    primaryColorHex = 0xFFFFFFFF,
                    secondaryColorHex = 0xFFE0E0E0,
                    accentColorHex = 0xFFDB0011,
                    finishType = CardFinishType.MATTE,
                    network = "Visa",
                    styleNotes = "Blanco perla puro con triángulos geométricos HSBC"
                )
            }

            // SCOTIABANK
            combined.contains("scotiabank") -> RealCardBranding(
                bankDisplayName = "Scotiabank",
                cardModelName = "Tradicional",
                primaryColorHex = 0xFFED0722,
                secondaryColorHex = 0xFF8F000F,
                accentColorHex = 0xFFFFFFFF,
                finishType = CardFinishType.GLOSSY_VIBRANT,
                network = "Visa",
                styleNotes = "Rojo Scotia con símbolo de la S alada"
            )

            // HEY BANCO
            Regex("\\bhey\\b").containsMatchIn(combined) -> RealCardBranding(
                bankDisplayName = "Hey Banco",
                cardModelName = "Crédito",
                primaryColorHex = 0xFF181818,
                secondaryColorHex = 0xFF080808,
                accentColorHex = 0xFFB4F94C,
                finishType = CardFinishType.MATTE,
                network = "Visa",
                styleNotes = "Negro profundo con acentos verde neón Hey"
            )

            // PLATA CARD
            Regex("\\bplata\\b").containsMatchIn(combined) -> RealCardBranding(
                bankDisplayName = "Plata",
                cardModelName = "Card",
                primaryColorHex = 0xFF222222,
                secondaryColorHex = 0xFF111111,
                accentColorHex = 0xFF00D1FF,
                finishType = CardFinishType.MATTE,
                network = "Mastercard",
                styleNotes = "Gris plomo mate con línea cian luminosa"
            )

            // DEPARTAMENTALES: LIVERPOOL
            combined.contains("liverpool") -> RealCardBranding(
                bankDisplayName = "Liverpool",
                cardModelName = if (combined.contains("visa")) "Visa" else "Departamental",
                primaryColorHex = 0xFFD81B60,
                secondaryColorHex = 0xFF880E4F,
                accentColorHex = 0xFFFFFFFF,
                finishType = CardFinishType.GLOSSY_VIBRANT,
                network = if (combined.contains("visa")) "Visa" else "Departamental",
                styleNotes = "Rosa magenta Liverpool con tipografía elegante"
            )

            // PALACIO DE HIERRO
            combined.contains("palacio") -> RealCardBranding(
                bankDisplayName = "Palacio de Hierro",
                cardModelName = "Palacio",
                primaryColorHex = 0xFFE5B80B,
                secondaryColorHex = 0xFF8F6D00,
                accentColorHex = 0xFF111111,
                finishType = CardFinishType.LUXURY_GOLD,
                network = "Departamental",
                styleNotes = "Amarillo oro icónico Totalmente Palacio"
            )

            // SEARS
            combined.contains("sears") -> RealCardBranding(
                bankDisplayName = "Sears",
                cardModelName = "Sears",
                primaryColorHex = 0xFF003087,
                secondaryColorHex = 0xFF001A4B,
                accentColorHex = 0xFFFFFFFF,
                finishType = CardFinishType.GLOSSY_VIBRANT,
                network = "Departamental",
                styleNotes = "Azul marino Sears clásico"
            )

            // DEFAULT / FALLBACK
            else -> RealCardBranding(
                bankDisplayName = bank.ifBlank { "Banco" },
                cardModelName = cardName.ifBlank { "Crédito" },
                primaryColorHex = 0xFF1E293B,
                secondaryColorHex = 0xFF0F172A,
                accentColorHex = 0xFF38BDF8,
                finishType = CardFinishType.METALLIC_BRUSHED,
                network = network.ifBlank { "Visa" },
                styleNotes = "Acabado titanio moderno con chip metálico"
            )
        }
    }
}
