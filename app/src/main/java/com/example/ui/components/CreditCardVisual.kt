package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.domain.CardBrandingEngine
import com.example.domain.CardFinishType
import com.example.domain.RealCardBranding
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardVisual(
    card: CreditCard,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isCompact: Boolean = false
) {
    // Motor de IA: Identificación de branding real de la tarjeta
    val branding = remember(card.name, card.bank, card.network) {
        CardBrandingEngine.matchRealCardBranding(card.name, card.bank, card.network)
    }

    // Cálculo dinámico de luminancia para asegurar contraste WCAG en fondos claros
    val isLightCard = remember(card.primaryColorHex, card.secondaryColorHex) {
        val r = ((card.primaryColorHex shr 16) and 0xFF) / 255.0
        val g = ((card.primaryColorHex shr 8) and 0xFF) / 255.0
        val b = (card.primaryColorHex and 0xFF) / 255.0
        val lum = 0.299 * r + 0.587 * g + 0.114 * b
        lum > 0.55
    }
    val cardTextColor = if (isLightCard) Color(0xFF14161A) else Color.White.copy(alpha = 0.95f)
    val cardSubtextColor = if (isLightCard) Color(0xFF333842) else Color.White.copy(alpha = 0.55f)

    // Proporción estándar de tarjeta bancaria física (ISO/IEC 7810 ID-1: 1.586)
    val cardShape = RoundedCornerShape(if (isCompact) 12.dp else 16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credit_card_item_${card.id}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompact) 4.dp else 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .clip(cardShape)
        ) {
            // Fondo base con gradiente respetando al 100% los colores seleccionados por el usuario
            CardSurfaceBackground(
                primaryColorHex = card.primaryColorHex,
                secondaryColorHex = card.secondaryColorHex
            )

            // Textura y acabado físico sutil de la tarjeta física
            CardPhysicalFinishTexture(finishType = branding.finishType, accentColor = Color(card.primaryColorHex))

            // Borde reflectivo perimetral realista
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.25f)
                            )
                        ),
                        shape = cardShape
                    )
            )

            // Contenido frontal de la tarjeta física
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 12.dp else 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior: Logo oficial del Banco emisor + Red de Pago
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    BankBrandLogo(bankName = card.bank, isCompact = isCompact)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (card.isDepartmental) {
                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "DEPARTAMENTAL",
                                    color = Color(0xFF2E1A00),
                                    fontSize = if (isCompact) 7.sp else 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        PaymentNetworkBadge(
                            network = card.network.ifBlank { branding.network },
                            isCompact = isCompact
                        )
                    }
                }

                // Fila Media: Chip EMV + Contactless + Nombre exacto de la tarjeta (sin nomenclaturas inventadas)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RealisticEmvChip(
                                isCompact = isCompact,
                                isSilver = branding.finishType == CardFinishType.METALLIC_BRUSHED || branding.finishType == CardFinishType.CARBON_SLATE
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            ContactlessWaveIcon(
                                modifier = Modifier.size(if (isCompact) 14.dp else 18.dp),
                                waveColor = if (isLightCard) Color(0xFF282C34) else Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Sello óptico holográfico de seguridad que llevan las tarjetas reales
                        HolographicSecurityPatch(isCompact = isCompact)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Nombre exacto asignado a la tarjeta por el usuario con contraste garantizado
                    Text(
                        text = card.name,
                        color = cardTextColor,
                        fontSize = if (isCompact) 11.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }

                // Número de tarjeta en relieve metálico (Embossed) adaptado a fondos claros
                EmbossedCardNumber(
                    last4Digits = card.last4Digits,
                    isCompact = isCompact,
                    isGold = branding.finishType == CardFinishType.LUXURY_GOLD,
                    isLightCard = isLightCard
                )

                // Fila Inferior: Nombre del Titular y Ciclos de Corte y Pago reales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "CARDHOLDER / TITULAR",
                            color = cardSubtextColor,
                            fontSize = if (isCompact) 6.sp else 7.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = card.cardholderName.ifBlank { "TITULAR DE LA TARJETA" }.uppercase(),
                            color = cardTextColor,
                            fontSize = if (isCompact) 9.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CORTE / PAGO",
                                color = cardSubtextColor,
                                fontSize = if (isCompact) 6.sp else 7.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "DÍA ${card.cutoffDay} / ${card.paymentDueDay}",
                                color = cardTextColor,
                                fontSize = if (isCompact) 9.sp else 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fondo base con gradientes respetando estrictamente los colores seleccionados por el usuario.
 */
@Composable
private fun CardSurfaceBackground(primaryColorHex: Long, secondaryColorHex: Long) {
    val primary = Color(primaryColorHex)
    val secondary = Color(secondaryColorHex)

    val gradientColors = listOf(
        primary,
        secondary,
        primary.copy(alpha = 0.85f),
        Color(0xFF0F1115).copy(alpha = 0.75f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 700f)
                )
            )
    )
}

/**
 * Textura física y efectos ópticos según el material de la tarjeta.
 */
@Composable
private fun CardPhysicalFinishTexture(finishType: CardFinishType, accentColor: Color) {
    when (finishType) {
        CardFinishType.METALLIC_BRUSHED -> {
            // Textura de micro-líneas horizontales de titanio/aluminio cepillado
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 3f
                var y = 0f
                var toggle = true
                while (y < size.height) {
                    drawLine(
                        color = if (toggle) Color.White.copy(alpha = 0.035f) else Color.Black.copy(alpha = 0.055f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += step
                    toggle = !toggle
                }
            }
            // Brillo diagonal reflectivo de metal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f),
                                Color.Black.copy(alpha = 0.25f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(900f, 600f)
                        )
                    )
            )
        }
        CardFinishType.LUXURY_GOLD -> {
            // Brillo dorado 24K y reflejo de hoja de oro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFE082).copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(200f, 150f),
                            radius = 600f
                        )
                    )
            )
            // Filigrana perimetral dorada sutil
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color(0xFFFFD700).copy(alpha = 0.22f),
                    topLeft = Offset(14f, 14f),
                    size = androidx.compose.ui.geometry.Size(size.width - 28f, size.height - 28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                    style = Stroke(width = 1.2f)
                )
            }
        }
        CardFinishType.CARBON_SLATE -> {
            // Textura de fibra de carbono oscura
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 6f
                var x = 0f
                while (x < size.width + size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.025f),
                        start = Offset(x, 0f),
                        end = Offset(x - size.height, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
            }
            // Acento láser sutil en la esquina superior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            center = Offset(0f, 0f),
                            radius = 450f
                        )
                    )
            )
        }
        CardFinishType.NEON_PURPLE -> {
            // Destello violeta terciopelo Nubank
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFB347EB).copy(alpha = 0.30f),
                                Color.Transparent
                            ),
                            center = Offset(700f, 100f),
                            radius = 500f
                        )
                    )
            )
        }
        CardFinishType.CENTURION_STEEL -> {
            // Marco guilloché característico de American Express
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 1.5f
                val margin = 16f
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.20f),
                    topLeft = Offset(margin, margin),
                    size = androidx.compose.ui.geometry.Size(size.width - margin * 2, size.height - margin * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    style = Stroke(width = strokeWidth)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = Offset(margin + 4f, margin + 4f),
                    size = androidx.compose.ui.geometry.Size(size.width - (margin + 4f) * 2, size.height - (margin + 4f) * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                    style = Stroke(width = 0.8f)
                )
            }
        }
        else -> {
            // Acabado glossy satinado estándar con destello diagonal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.20f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(900f, 600f)
                        )
                    )
            )
        }
    }
}

/**
 * Logotipos oficiales e insignias identitarias de los bancos emisores.
 * Sin inventar modelos ni nomenclaturas artificiales.
 */
@Composable
fun BankBrandLogo(bankName: String, isCompact: Boolean = false, modifier: Modifier = Modifier) {
    val bLower = bankName.lowercase().trim()

    Box(modifier = modifier) {
        when {
            bLower.contains("bbva") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BBVA",
                        color = Color.White,
                        fontSize = if (isCompact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 6.dp else 7.dp)
                            .background(Color(0xFF00A9E0), CircleShape)
                    )
                }
            }
            bLower.contains("santander") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flama Santander oficial
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEC0000),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontSize = if (isCompact) 10.sp else 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Santander",
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
            bLower.contains("citibanamex") || bLower.contains("banamex") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "citibanamex",
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(if (isCompact) 10.dp else 14.dp)
                            .height(if (isCompact) 4.dp else 5.dp)
                            .background(Color(0xFFED1C24), RoundedCornerShape(2.dp))
                    )
                }
            }
            bLower.contains("banorte") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEB0029),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(width = 9.dp, height = 2.dp).background(Color.White))
                                Box(modifier = Modifier.size(width = 9.dp, height = 2.dp).background(Color.White))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BANORTE",
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }
            Regex("\\bnu\\b").containsMatchIn(bLower) -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF820AD1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "nu",
                            color = Color.White,
                            fontSize = if (isCompact) 15.sp else 18.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            bLower.contains("hsbc") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Isotipo de triángulos HSBC
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color.White,
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row {
                                Box(modifier = Modifier.size(if (isCompact) 6.dp else 7.dp).background(Color(0xFFDB0011)))
                                Box(modifier = Modifier.size(if (isCompact) 6.dp else 7.dp).background(Color.White))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HSBC",
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
            bLower.contains("scotiabank") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFED0722),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontSize = if (isCompact) 10.sp else 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scotiabank",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            bLower.contains("amex") || bLower.contains("american") -> {
                Surface(
                    color = Color(0xFF006FCF),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = "AMERICAN EXPRESS",
                        color = Color.White,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            bLower.contains("liverpool") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE10098),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "L",
                                color = Color.White,
                                fontSize = if (isCompact) 10.sp else 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Liverpool",
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.3.sp
                    )
                }
            }
            bLower.contains("sears") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFB21E27),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontSize = if (isCompact) 10.sp else 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SEARS",
                        color = Color.White,
                        fontSize = if (isCompact) 14.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
            Regex("\\bhey\\b").containsMatchIn(bLower) -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "hey",
                        color = Color.White,
                        fontSize = if (isCompact) 16.sp else 19.sp,
                        fontWeight = FontWeight.Black
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 6.dp else 7.dp)
                            .background(Color(0xFFB4F94C), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Banco",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = if (isCompact) 12.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Regex("\\bplata\\b").containsMatchIn(bLower) -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.30f),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "P",
                                color = Color.White,
                                fontSize = if (isCompact) 10.sp else 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "plata card",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            bLower.contains("mercado") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF009EE3),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "mercado pago",
                        color = Color.White,
                        fontSize = if (isCompact) 12.sp else 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            bLower.contains("rappi") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF441F),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "R",
                                color = Color.White,
                                fontSize = if (isCompact) 10.sp else 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RappiCard",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            bLower.contains("coppel") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF005691),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("C", color = Color(0xFFFFD100), fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BanCoppel",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            bLower.contains("inbursa") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF003865),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8102E)),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("I", color = Color.White, fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INBURSA",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            bLower.contains("azteca") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF007A33),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("A", color = Color.White, fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Banco Azteca",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            bLower.contains("banregio") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFF5A00),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("b", color = Color.White, fontSize = if (isCompact) 11.sp else 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "banregio",
                        color = Color.White,
                        fontSize = if (isCompact) 13.sp else 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            bLower.contains("palacio") -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFB8860B),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("H", color = Color.White, fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PALACIO DE HIERRO",
                        color = Color.White,
                        fontSize = if (isCompact) 11.sp else 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bankName.ifBlank { "BANCO" }.uppercase(),
                        color = Color.White,
                        fontSize = if (isCompact) 12.sp else 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * Insignia/Badge oficial y distintiva del banco emisor para listas, pantallas de detalle y diálogos.
 */
@Composable
fun BankBrandBadge(bankName: String, modifier: Modifier = Modifier.size(34.dp)) {
    val bLower = bankName.lowercase().trim()

    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            bLower.contains("bbva") -> {
                Surface(
                    color = Color(0xFF004481),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "BBVA",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            bLower.contains("santander") -> {
                Surface(
                    color = Color(0xFFEC0000),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "S",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("citibanamex") || bLower.contains("banamex") -> {
                Surface(
                    color = Color(0xFF002D72),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "citi",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(width = 6.dp, height = 3.dp)
                                    .background(Color(0xFFED1C24), RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }
            bLower.contains("banorte") -> {
                Surface(
                    color = Color(0xFFEB0029),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(width = 14.dp, height = 3.dp).background(Color.White))
                            Box(modifier = Modifier.size(width = 14.dp, height = 3.dp).background(Color.White))
                        }
                    }
                }
            }
            Regex("\\bnu\\b").containsMatchIn(bLower) -> {
                Surface(
                    color = Color(0xFF820AD1),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "nu",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("hsbc") -> {
                Surface(
                    color = Color(0xFFDB0011),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "HSBC",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("scotiabank") -> {
                Surface(
                    color = Color(0xFFED0722),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "S",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("amex") || bLower.contains("american") -> {
                Surface(
                    color = Color(0xFF006FCF),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AMEX",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("liverpool") -> {
                Surface(
                    color = Color(0xFFE10098),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "L",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("sears") -> {
                Surface(
                    color = Color(0xFFB21E27),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "S",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Regex("\\bhey\\b").containsMatchIn(bLower) -> {
                Surface(
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "hey",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(5.dp)
                                    .background(Color(0xFFB4F94C), CircleShape)
                            )
                        }
                    }
                }
            }
            Regex("\\bplata\\b").containsMatchIn(bLower) -> {
                Surface(
                    color = Color(0xFF2C2C2C),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "P",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("coppel") -> {
                Surface(
                    color = Color(0xFF005691),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "C",
                            color = Color(0xFFFFD100),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("azteca") -> {
                Surface(
                    color = Color(0xFF007A33),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "A",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            bLower.contains("banregio") -> {
                Surface(
                    color = Color(0xFFFF5A00),
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "b",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            else -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chip EMV con grabado de pistas de contacto metálicas.
 */
@Composable
private fun RealisticEmvChip(isCompact: Boolean, isSilver: Boolean = false) {
    val chipWidth = if (isCompact) 28.dp else 36.dp
    val chipHeight = if (isCompact) 22.dp else 28.dp
    val chipColor = if (isSilver) Color(0xFFD8DEE4) else Color(0xFFE5C158)
    val grooveColor = if (isSilver) Color(0xFF7A8694) else Color(0xFF9E7E24)

    Surface(
        modifier = Modifier.size(width = chipWidth, height = chipHeight),
        shape = RoundedCornerShape(4.dp),
        color = chipColor
    ) {
        Canvas(modifier = Modifier.size(width = chipWidth, height = chipHeight)) {
            val w = size.width
            val h = size.height
            val strokeWidth = 1.2f

            // Pistas del circuito integrado del chip
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.35f, 0f),
                end = Offset(w * 0.35f, h),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.65f, 0f),
                end = Offset(w * 0.65f, h),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = grooveColor,
                start = Offset(0f, h * 0.5f),
                end = Offset(w, h * 0.5f),
                strokeWidth = strokeWidth
            )
            drawRoundRect(
                color = grooveColor,
                topLeft = Offset(w * 0.35f, h * 0.32f),
                size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.36f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * Ícono de ondas de pago Contactless.
 */
@Composable
internal fun ContactlessWaveIcon(
    modifier: Modifier = Modifier,
    waveColor: Color = Color.White.copy(alpha = 0.85f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8f)

        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.7f),
            style = stroke
        )
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.05f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.9f),
            style = stroke
        )
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.5f, 0f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h),
            style = stroke
        )
    }
}

/**
 * Parche holográfico reflectivo de seguridad presente en tarjetas bancarias reales.
 */
@Composable
private fun HolographicSecurityPatch(isCompact: Boolean) {
    Box(
        modifier = Modifier
            .size(width = if (isCompact) 20.dp else 26.dp, height = if (isCompact) 14.dp else 18.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0C3FC),
                        Color(0xFF8EC5FC),
                        Color(0xFFE0C3FC),
                        Color(0xFFFFD1DC)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(40f, 40f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.size(if (isCompact) 10.dp else 12.dp)
        )
    }
}

/**
 * Número de tarjeta con relieve tridimensional táctil (Embossing).
 */
@Composable
private fun EmbossedCardNumber(
    last4Digits: String,
    isCompact: Boolean,
    isGold: Boolean = false,
    isLightCard: Boolean = false
) {
    val numberText = "••••  ••••  ••••  $last4Digits"
    val fontSize = if (isCompact) 14.sp else 18.sp

    Box {
        // Sombra de relieve adaptativa
        Text(
            text = numberText,
            color = if (isLightCard) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.45f),
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            letterSpacing = if (isCompact) 1.8.sp else 2.5.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.offset(x = 1.dp, y = 1.dp)
        )
        // Texto principal metálico reflectivo con contraste garantizado
        Text(
            text = numberText,
            color = when {
                isGold -> Color(0xFFFFE082)
                isLightCard -> Color(0xFF14161A)
                else -> Color.White.copy(alpha = 0.95f)
            },
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            letterSpacing = if (isCompact) 1.8.sp else 2.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Logotipo oficial de la red de pago (Mastercard, Visa, Amex, Tienda).
 */
@Composable
internal fun PaymentNetworkBadge(network: String, isCompact: Boolean = false) {
    when {
        network.contains("Mastercard", ignoreCase = true) -> {
            Row(
                modifier = Modifier.height(if (isCompact) 18.dp else 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 16.dp else 20.dp)
                        .background(Color(0xFFEB001B), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-6).dp)
                        .size(if (isCompact) 16.dp else 20.dp)
                        .background(Color(0xFFF79E1B).copy(alpha = 0.95f), CircleShape)
                )
            }
        }
        network.contains("Visa", ignoreCase = true) -> {
            Text(
                text = "VISA",
                color = Color.White,
                fontSize = if (isCompact) 15.sp else 19.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp
            )
        }
        network.contains("Amex", ignoreCase = true) || network.contains("American", ignoreCase = true) -> {
            Surface(
                color = Color(0xFF006FCF),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Text(
                    text = "AMEX",
                    color = Color.White,
                    fontSize = if (isCompact) 9.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
        network.contains("Departamental", ignoreCase = true) || network.contains("Tienda", ignoreCase = true) -> {
            Surface(
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TIENDA",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        else -> {
            Surface(
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = network,
                    color = Color.White,
                    fontSize = if (isCompact) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Franja con parámetros operativos financieros mostrados de forma limpia y moderna
 * por fuera de la tarjeta física real.
 */
@Composable
fun CardParametersStrip(card: CreditCard, modifier: Modifier = Modifier) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val isBefore15th = card.paymentDueDay <= 15

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Corte
                Column {
                    Text(
                        text = "CORTE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Día ${card.cutoffDay}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Días de Gracia
                Column {
                    Text(
                        text = "GRACIA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${card.graceDays} días",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Límite de Pago con etiqueta clara
                Column {
                    Text(
                        text = "LÍMITE PAGO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Día ${card.paymentDueDay}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (card.creditLimit > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Línea de Crédito
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LÍNEA CRÉDITO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormat.format(card.creditLimit),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Indicador de quincena de pago: "A pagar antes del 15" o "A pagar antes de fin de mes"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isBefore15th) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (isBefore15th) "📅 Antes del 15" else "📅 Fin de mes",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBefore15th) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = if (card.isDepartmental) "Tienda Departamental" else "Bancaria (${card.network})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
