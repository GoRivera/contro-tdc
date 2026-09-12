package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import com.example.ui.util.rememberPrivacyCurrencyFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.domain.CardBrandingEngine
import com.example.domain.CreditCardCalculator
import com.example.domain.MexicanBankInfo
import com.example.domain.MexicanBanks
import com.example.ui.components.BankBrandBadge
import com.example.ui.components.ContactlessWaveIcon
import com.example.ui.components.CreditCardVisual
import com.example.ui.components.PaymentNetworkBadge
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Paleta de colores distintivos y exclusivos para tarjetas en México.
 * Regla de negocio: No podrá haber dos tarjetas con el mismo color principal.
 */
data class CardColorOption(val name: String, val primary: Long, val secondary: Long)

val AppCardColorPalette = listOf(
    CardColorOption("Rojo Santander", 0xFFEC0000L, 0xFF990000L),
    CardColorOption("Azul Marino BBVA", 0xFF0B2265L, 0xFF1464A5L),
    CardColorOption("Morado Nu", 0xFF820AD1L, 0xFF4C0677L),
    CardColorOption("Azul Banamex", 0xFF003B70L, 0xFF001F3FL),
    CardColorOption("Gris Grafito Banorte", 0xFF373B44L, 0xFF1B1D22L),
    CardColorOption("Magenta Liverpool", 0xFFE10098L, 0xFF88005CL),
    CardColorOption("Azul Índigo Sears", 0xFF0A2540L, 0xFFB21E27L),
    CardColorOption("Plata Metálico", 0xFF5C677DL, 0xFF2A2E35L),
    CardColorOption("Azul AMEX", 0xFF0077A6L, 0xFF004466L),
    CardColorOption("Verde Neón Hey", 0xFF1C2826L, 0xFF004D40L),
    CardColorOption("Verde Esmeralda", 0xFF007A33L, 0xFF004D20L),
    CardColorOption("Oro Imperial", 0xFFB8860BL, 0xFF5C450BL),
    CardColorOption("Naranja Coral", 0xFFE65100L, 0xFFBF360CL),
    CardColorOption("Borgoña Vino", 0xFF800020L, 0xFF4A0013L),
    CardColorOption("Violeta Real", 0xFF4A148CL, 0xFF1A0033L),
    CardColorOption("Turquesa Oscuro", 0xFF005B5CL, 0xFF003333L),
    CardColorOption("Negro Carbón Onyx", 0xFF181818L, 0xFF080808L)
)

fun calculateSecondaryColorHex(primaryHex: Long): Long {
    val r = (((primaryHex shr 16) and 0xFF) * 60 / 100)
    val g = (((primaryHex shr 8) and 0xFF) * 60 / 100)
    val b = ((primaryHex and 0xFF) * 60 / 100)
    return (0xFF000000L or (r shl 16) or (g shl 8) or b)
}

val CustomColorPresets = listOf(
    0xFF00B0FFL, // Celeste Eléctrico
    0xFF00C853L, // Verde Menta Brillante
    0xFFFF6D00L, // Ámbar Intenso
    0xFFD50000L, // Rojo Carmesí
    0xFFAA00FFL, // Púrpura Neón
    0xFF2962FFL, // Azul Real Profundo
    0xFF00BFA5L, // Turquesa Esmeralda
    0xFFFFD600L, // Oro Brillante
    0xFFFF4081L, // Rosa Fucsia
    0xFF4E342EL, // Café Moca / Bronce
    0xFF37474FL, // Gris Titanio
    0xFF1A237EL, // Azul Noche Profundo
    0xFF263238L, // Carbón Oscuro
    0xFF5D4037L, // Cobre Metálico
    0xFF880E4FL, // Vino Ciruela
    0xFF004D40L  // Verde Petróleo
)

@Composable
fun CardColorPickerRow(
    selectedPrimaryHex: Long,
    usedPrimaryHexes: Set<Long>,
    onColorSelected: (primary: Long, secondary: Long) -> Unit,
    onOpenCustomPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedOptions = remember(selectedPrimaryHex) {
        val inPalette = AppCardColorPalette.any { it.primary == selectedPrimaryHex }
        if (inPalette) {
            AppCardColorPalette
        } else {
            listOf(
                CardColorOption(
                    "Color Personalizado",
                    selectedPrimaryHex,
                    calculateSecondaryColorHex(selectedPrimaryHex)
                )
            ) + AppCardColorPalette
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón especial para crear nuevo color personalizado
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable { onOpenCustomPicker() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Crear nuevo color",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        displayedOptions.forEach { opt ->
            val isSelected = opt.primary == selectedPrimaryHex
            val isUsed = usedPrimaryHexes.contains(opt.primary)

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(opt.primary), Color(opt.secondary))
                        )
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .clickable(enabled = !isUsed || isSelected) {
                        onColorSelected(opt.primary, opt.secondary)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (isUsed) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "En uso",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomColorPickerDialog(
    usedPrimaryHexes: Set<Long>,
    onDismiss: () -> Unit,
    onApplyColor: (primary: Long, secondary: Long) -> Unit
) {
    var hexInput by remember { mutableStateOf("00B0FF") }
    var selectedHex by remember { mutableLongStateOf(0xFF00B0FFL) }

    val parsedPrimary = remember(hexInput, selectedHex) {
        val clean = hexInput.replace("#", "").trim()
        if (clean.length == 6) {
            try {
                0xFF000000L or clean.toLong(16)
            } catch (e: Exception) {
                selectedHex
            }
        } else {
            selectedHex
        }
    }
    val calculatedSecondary = remember(parsedPrimary) {
        calculateSecondaryColorHex(parsedPrimary)
    }
    val isUsed = usedPrimaryHexes.contains(parsedPrimary)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Color de Tarjeta")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecciona una tonalidad o escribe tu propio código hexadecimal:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Muestra en vivo del color resultante
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(parsedPrimary), Color(calculatedSecondary)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${java.lang.Long.toHexString(parsedPrimary).takeLast(6).uppercase()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Text("Tonalidades sugeridas:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomColorPresets.forEach { colorHex ->
                        val isPresetSelected = colorHex == parsedPrimary
                        val isPresetUsed = usedPrimaryHexes.contains(colorHex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isPresetSelected) 2.5.dp else 1.dp,
                                    color = if (isPresetSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isPresetUsed) {
                                    selectedHex = colorHex
                                    hexInput = java.lang.Long.toHexString(colorHex).takeLast(6).uppercase()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPresetSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            } else if (isPresetUsed) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }

                // Input manual de Hexadecimal
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val clean = input.replace("#", "").trim().take(6)
                        hexInput = clean
                        if (clean.length == 6) {
                            try {
                                selectedHex = 0xFF000000L or clean.toLong(16)
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Código HEX") },
                    prefix = { Text("# ") },
                    placeholder = { Text("00B0FF") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isUsed) {
                    Text(
                        text = "⚠️ Este color ya está asignado a otra tarjeta. Elige uno diferente.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyColor(parsedPrimary, calculatedSecondary)
                    onDismiss()
                },
                enabled = !isUsed && hexInput.isNotBlank()
            ) {
                Text("Usar este Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatInputWithCommas(input: String): String {
    val clean = input.filter { it.isDigit() }
    if (clean.isBlank()) return ""
    return try {
        val num = clean.toLong()
        NumberFormat.getNumberInstance(Locale.US).format(num)
    } catch (e: Exception) {
        clean
    }
}

/**
 * Selector interactivo de red de pago: VISA, MasterCard y AMEX.
 */
@Composable
fun PaymentNetworkSelector(
    selectedNetwork: String,
    onNetworkSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val networks = listOf("Visa", "Mastercard", "Amex")

    Column(modifier = modifier) {
        Text(
            text = "Red de la Tarjeta:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            networks.forEach { netKey ->
                val isSelected = when (netKey) {
                    "Visa" -> selectedNetwork.contains("Visa", ignoreCase = true)
                    "Mastercard" -> selectedNetwork.contains("Mastercard", ignoreCase = true)
                    "Amex" -> selectedNetwork.contains("Amex", ignoreCase = true) || selectedNetwork.contains("American", ignoreCase = true)
                    else -> selectedNetwork.equals(netKey, ignoreCase = true)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNetworkSelected(netKey) }
                        .testTag("network_select_$netKey")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (netKey) {
                            "Visa" -> {
                                Text(
                                    text = "VISA",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            "Mastercard" -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(18.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .background(Color(0xFFEB001B), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-5).dp)
                                            .size(15.dp)
                                            .background(Color(0xFFF79E1B).copy(alpha = 0.95f), CircleShape)
                                    )
                                }
                            }
                            "Amex" -> {
                                Surface(
                                    color = Color(0xFF006FCF),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "AMEX",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = netKey,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactWalletCardStrip(
    card: CreditCard,
    availableCredit: Double,
    creditLimit: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val branding = remember(card.name, card.bank, card.network) {
        CardBrandingEngine.matchRealCardBranding(card.name, card.bank, card.network)
    }
    val isLightCard = remember(card.primaryColorHex) {
        val r = ((card.primaryColorHex shr 16) and 0xFF) / 255.0
        val g = ((card.primaryColorHex shr 8) and 0xFF) / 255.0
        val b = (card.primaryColorHex and 0xFF) / 255.0
        (0.299 * r + 0.587 * g + 0.114 * b) > 0.55
    }
    val cardTextColor = if (isLightCard) Color(0xFF14161A) else Color.White.copy(alpha = 0.95f)
    val cardSubtextColor = if (isLightCard) Color(0xFF333842) else Color.White.copy(alpha = 0.65f)
    val currencyFormat = rememberPrivacyCurrencyFormat()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("card_strip_${card.id}"),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con el gradiente característico de la tarjeta
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(card.primaryColorHex),
                                Color(card.secondaryColorHex),
                                Color(card.primaryColorHex).copy(alpha = 0.85f),
                                Color(0xFF0F1115).copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Borde reflectivo perimetral
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp,
                        color = if (isLightCard) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info izquierda: Logo del Banco + Nombre + Últimos 4 dígitos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BankBrandBadge(bankName = card.bank, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = card.name,
                            color = cardTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "•••• ${card.last4Digits}",
                                color = cardSubtextColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (card.isDepartmental) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DEPTO",
                                    color = if (isLightCard) Color(0xFFB78103) else Color(0xFFFFD54F),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Info derecha: Logo de Red de pago + Saldo disponible
                Column(horizontalAlignment = Alignment.End) {
                    PaymentNetworkBadge(
                        network = card.network.ifBlank { branding.network },
                        isCompact = true
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(availableCredit),
                        color = cardTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "disp. de ${currencyFormat.format(creditLimit)}",
                        color = cardSubtextColor,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveCardManagementCard(
    card: CreditCard,
    spent: Double,
    availableCredit: Double,
    currencyFormat: NumberFormat,
    msiPendingBalance: Double = 0.0,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usedRatio = if (card.creditLimit > 0.0) (spent / card.creditLimit).toFloat().coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Línea de Crédito & Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LÍNEA DISPONIBLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = currencyFormat.format(availableCredit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            usedRatio > 0.85f -> MaterialTheme.colorScheme.error
                            usedRatio > 0.60f -> Color(0xFFE65100)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LÍMITE TOTAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = currencyFormat.format(card.creditLimit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso de uso
            LinearProgressIndicator(
                progress = { usedRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    usedRatio > 0.85f -> Color(0xFFD32F2F)
                    usedRatio > 0.60f -> Color(0xFFF57C00)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (msiPendingBalance > 0.0) {
                        "Ocupado: ${currencyFormat.format(spent)} (retención MSI: ${currencyFormat.format(msiPendingBalance)})"
                    } else {
                        "Gastado: ${currencyFormat.format(spent)}"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(usedRatio * 100).toInt()}% en uso",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fechas del Ciclo (Corte y Pago)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Día de Corte", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Día ${card.cutoffDay}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Límite de Pago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Día ${card.paymentDueDay} (+${card.graceDays}d)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("edit_card_${card.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_card_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar tarjeta",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CardsManagementScreen(
    cards: List<CreditCard>,
    expenses: List<Expense> = emptyList(),
    payments: List<Payment> = emptyList(),
    onAddCard: (
        name: String,
        bank: String,
        cutoffDay: Int,
        paymentDueDay: Int,
        creditLimit: Double,
        primaryColorHex: Long,
        secondaryColorHex: Long,
        last4Digits: String,
        network: String,
        isDepartmental: Boolean,
        graceDays: Int,
        cardholderName: String
    ) -> Unit,
    onUpdateCardDates: (
        card: CreditCard,
        newName: String,
        newCutoff: Int,
        newDue: Int,
        newLimit: Double,
        newGrace: Int,
        isDep: Boolean,
        cardholderName: String,
        primaryColorHex: Long,
        secondaryColorHex: Long,
        newNetwork: String,
        newBank: String,
        newAnnualInterestRatePercent: Double
    ) -> Unit,
    onDeleteCard: (CreditCard) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CreditCard?>(null) }
    var cardToDelete by remember { mutableStateOf<CreditCard?>(null) }
    var selectedCategoryFilter by remember { mutableIntStateOf(0) } // 0: Todas, 1: Bancarias, 2: Departamentales
    var selectedWalletCardId by remember(cards) { mutableStateOf<Long?>(cards.firstOrNull()?.id) }
    var walletViewMode by remember { mutableIntStateOf(0) } // 0: Billetera (Stack), 1: Carrusel

    val coroutineScope = rememberCoroutineScope()
    val currencyFormat = rememberPrivacyCurrencyFormat()

    // Requisito 6 & MSI: Línea de crédito real considerando gastos regulares y el saldo TOTAL retenido por MSI
    val cardBalanceMap = remember(cards, expenses, payments) {
        cards.associate { card ->
            card.id to CreditCardCalculator.calculateCardCreditBalance(card, expenses, payments)
        }
    }
    val cardSpentMap = remember(cardBalanceMap) {
        cardBalanceMap.mapValues { it.value.totalOccupiedCredit }
    }

    val totalCreditLimit = remember(cards) { cards.sumOf { it.creditLimit } }
    val totalSpent = remember(cardSpentMap) { cardSpentMap.values.sum() }
    val totalMsiPendingDebt = remember(cardBalanceMap) { cardBalanceMap.values.sumOf { it.totalMsiPendingBalance } }
    val totalAvailableCredit = remember(totalCreditLimit, totalSpent) {
        (totalCreditLimit - totalSpent).coerceAtLeast(0.0)
    }
    val globalUsedRatio = if (totalCreditLimit > 0.0) (totalSpent / totalCreditLimit).toFloat().coerceIn(0f, 1f) else 0f

    val departmentalCount = remember(cards) { cards.count { it.isDepartmental } }
    val bankingCount = remember(cards) { cards.size - departmentalCount }

    val filteredCards = remember(cards, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            1 -> cards.filter { !it.isDepartmental }
            2 -> cards.filter { it.isDepartmental }
            else -> cards
        }
    }

    val activeCard = filteredCards.firstOrNull { it.id == selectedWalletCardId } ?: filteredCards.firstOrNull()
    val otherCards = remember(filteredCards, activeCard) {
        filteredCards.filter { it.id != activeCard?.id }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { filteredCards.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState.currentPage, filteredCards) {
        if (walletViewMode == 1 && filteredCards.isNotEmpty()) {
            val pCard = filteredCards.getOrNull(pagerState.currentPage)
            if (pCard != null && pCard.id != selectedWalletCardId) {
                selectedWalletCardId = pCard.id
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Google Wallet Header
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            ContactlessWaveIcon(
                                modifier = Modifier.size(24.dp),
                                waveColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Billetera",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Google Wallet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("add_card_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Línea Global Resumen Compacto
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LÍNEA GLOBAL DISPONIBLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = currencyFormat.format(totalAvailableCredit),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                globalUsedRatio > 0.85f -> MaterialTheme.colorScheme.error
                                globalUsedRatio > 0.60f -> Color(0xFFE65100)
                                else -> Color(0xFF2E7D32)
                            }
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LÍMITE TOTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = currencyFormat.format(totalCreditLimit),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Filtros de Categoría
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == 0,
                    onClick = { selectedCategoryFilter = 0 },
                    label = { Text("Todas (${cards.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == 1,
                    onClick = { selectedCategoryFilter = 1 },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    label = { Text("Bancarias ($bankingCount)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == 2,
                    onClick = { selectedCategoryFilter = 2 },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    label = { Text("Deptos ($departmentalCount)", fontSize = 12.sp) }
                )
            }
        }

        // 4. Barra de Vista (Billetera vs Carrusel) proporcional y estética
        if (cards.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARJETAS (${filteredCards.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Control segmentado estilizado con proporciones Material 3
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = if (walletViewMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .height(30.dp)
                                    .clickable { walletViewMode = 0 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = if (walletViewMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Billetera",
                                        fontSize = 11.sp,
                                        fontWeight = if (walletViewMode == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (walletViewMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = if (walletViewMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .height(30.dp)
                                    .clickable { walletViewMode = 1 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = if (walletViewMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Carrusel",
                                        fontSize = 11.sp,
                                        fontWeight = if (walletViewMode == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (walletViewMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Estado vacío o Lista de Tarjetas
        if (filteredCards.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Billetera vacía",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registra tus tarjetas de crédito y departamentales para organizar tus gastos, límites y fechas de corte.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_state_add_card_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Añadir primera tarjeta", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            if (walletViewMode == 0) {
                // MODO BILLETERA (STACK GOOGLE WALLET)
                // Tarjeta Activa Principal
                if (activeCard != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CreditCardVisual(
                                card = activeCard,
                                modifier = Modifier.fillMaxWidth(),
                                isCompact = false
                            )

                            val activeBalance = cardBalanceMap[activeCard.id]
                            val activeSpent = activeBalance?.totalOccupiedCredit ?: (cardSpentMap[activeCard.id] ?: 0.0)
                            val activeAvailable = activeBalance?.availableCredit ?: (activeCard.creditLimit - activeSpent).coerceAtLeast(0.0)
                            val activeMsiPending = activeBalance?.totalMsiPendingBalance ?: 0.0

                            ActiveCardManagementCard(
                                card = activeCard,
                                spent = activeSpent,
                                availableCredit = activeAvailable,
                                currencyFormat = currencyFormat,
                                msiPendingBalance = activeMsiPending,
                                onEdit = { editingCard = activeCard },
                                onDelete = { cardToDelete = activeCard }
                            )
                        }
                    }

                    // Otras tarjetas en formato compacto Google Wallet
                    if (otherCards.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OTRAS TARJETAS EN TU BILLETERA (${otherCards.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Toca para activar",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        items(otherCards, key = { it.id }) { card ->
                            val spent = cardSpentMap[card.id] ?: 0.0
                            val available = (card.creditLimit - spent).coerceAtLeast(0.0)

                            CompactWalletCardStrip(
                                card = card,
                                availableCredit = available,
                                creditLimit = card.creditLimit,
                                onClick = {
                                    selectedWalletCardId = card.id
                                }
                            )
                        }
                    }
                }
            } else {
                // MODO CARRUSEL (GOOGLE WALLET HORIZONTAL)
                item {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val card = filteredCards[page]
                        CreditCardVisual(
                            card = card,
                            modifier = Modifier.fillMaxWidth(),
                            isCompact = false
                        )
                    }
                }

                // Indicador de páginas (Dots)
                if (filteredCards.size > 1) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            filteredCards.forEachIndexed { idx, _ ->
                                val isPage = pagerState.currentPage == idx
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isPage) 8.dp else 6.dp)
                                        .background(
                                            if (isPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                // Panel de gestión para la tarjeta actualmente visible en el carrusel
                val currentCarouselCard = filteredCards.getOrNull(pagerState.currentPage) ?: activeCard
                if (currentCarouselCard != null) {
                    item {
                        val cBalance = cardBalanceMap[currentCarouselCard.id]
                        val cSpent = cBalance?.totalOccupiedCredit ?: (cardSpentMap[currentCarouselCard.id] ?: 0.0)
                        val cAvailable = cBalance?.availableCredit ?: (currentCarouselCard.creditLimit - cSpent).coerceAtLeast(0.0)
                        val cMsiPending = cBalance?.totalMsiPendingBalance ?: 0.0

                        ActiveCardManagementCard(
                            card = currentCarouselCard,
                            spent = cSpent,
                            availableCredit = cAvailable,
                            currencyFormat = currencyFormat,
                            msiPendingBalance = cMsiPending,
                            onEdit = { editingCard = currentCarouselCard },
                            onDelete = { cardToDelete = currentCarouselCard }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Diálogo de confirmación para eliminar tarjeta
    if (cardToDelete != null) {
        val card = cardToDelete!!
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("¿Eliminar ${card.name}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Estás a punto de eliminar '${card.name}' (${card.bank}). Todos los cargos, pagos y planes MSI asociados a esta tarjeta también se darán de baja.\n\nEsta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = cardToDelete
                        cardToDelete = null
                        if (toDel != null) onDeleteCard(toDel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para editar tarjeta
    if (editingCard != null) {
        val card = editingCard!!
        var editName by remember(card) { mutableStateOf(card.name) }
        var editCardholder by remember(card) { mutableStateOf(card.cardholderName) }
        var editBank by remember(card) { mutableStateOf(card.bank) }
        var editCutoff by remember(card) { mutableStateOf(card.cutoffDay.toString()) }
        var editGrace by remember(card) { mutableStateOf(card.graceDays.toString()) }
        var editDue by remember(card) { mutableStateOf(card.paymentDueDay.toString()) }
        // Corrección: antes se truncaba a entero (card.creditLimit.toLong()), perdiendo los centavos
        // del límite real cada vez que se abría el diálogo de edición.
        var editLimit by remember(card) {
            mutableStateOf(
                if (card.creditLimit % 1.0 == 0.0) card.creditLimit.toLong().toString() else card.creditLimit.toString()
            )
        }
        var editRate by remember(card) {
            mutableStateOf(
                if (card.annualInterestRatePercent % 1.0 == 0.0) card.annualInterestRatePercent.toLong().toString() else card.annualInterestRatePercent.toString()
            )
        }
        var editIsDepartmental by remember(card) { mutableStateOf(card.isDepartmental) }
        var editPrimaryHex by remember(card) { mutableLongStateOf(card.primaryColorHex) }
        var editSecondaryHex by remember(card) { mutableLongStateOf(card.secondaryColorHex) }
        var editNetwork by remember(card) { mutableStateOf(card.network) }
        var showCustomPickerForEdit by remember { mutableStateOf(false) }

        val cutoffNum = (editCutoff.toIntOrNull() ?: card.cutoffDay).coerceIn(1, 31)
        val graceNum = (editGrace.toIntOrNull() ?: card.graceDays).coerceAtLeast(1)
        val dueNum = (editDue.toIntOrNull() ?: card.paymentDueDay).coerceIn(1, 31)
        val parsedLimit = CreditCardCalculator.parseLocalizedDouble(editLimit) ?: card.creditLimit
        val parsedRate = CreditCardCalculator.parseLocalizedDouble(editRate) ?: card.annualInterestRatePercent

        val otherCardsUsedColors = remember(cards, card) {
            cards.filter { it.id != card.id }.map { it.primaryColorHex }.toSet()
        }
        val isDuplicateColor = otherCardsUsedColors.contains(editPrimaryHex)

        if (showCustomPickerForEdit) {
            CustomColorPickerDialog(
                usedPrimaryHexes = otherCardsUsedColors,
                onDismiss = { showCustomPickerForEdit = false },
                onApplyColor = { p, s ->
                    editPrimaryHex = p
                    editSecondaryHex = s
                }
            )
        }

        AlertDialog(
            onDismissRequest = { editingCard = null },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar ${card.name}")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CreditCardVisual(
                        card = card.copy(
                            name = if (editName.isNotBlank()) editName else card.name,
                            cardholderName = if (editCardholder.isNotBlank()) editCardholder else card.cardholderName,
                            cutoffDay = cutoffNum,
                            paymentDueDay = dueNum,
                            creditLimit = parsedLimit,
                            primaryColorHex = editPrimaryHex,
                            secondaryColorHex = editSecondaryHex,
                            isDepartmental = editIsDepartmental
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isCompact = true
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre de la Tarjeta") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Requisito 4: Titular de la tarjeta
                    OutlinedTextField(
                        value = editCardholder,
                        onValueChange = { editCardholder = it },
                        label = { Text("Nombre del Titular") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Corrección: antes no existía forma de editar el banco de una tarjeta ya creada.
                    OutlinedTextField(
                        value = editBank,
                        onValueChange = { editBank = it },
                        label = { Text("Banco Emisor") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de Color Único
                    Column {
                        Text(
                            text = "Color de la Tarjeta (Único por tarjeta):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CardColorPickerRow(
                            selectedPrimaryHex = editPrimaryHex,
                            usedPrimaryHexes = otherCardsUsedColors,
                            onColorSelected = { p, s ->
                                editPrimaryHex = p
                                editSecondaryHex = s
                            },
                            onOpenCustomPicker = { showCustomPickerForEdit = true }
                        )
                        if (isDuplicateColor) {
                            Text(
                                text = "⚠️ Este color ya está en uso por otra tarjeta. Selecciona uno diferente.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Selector de Red / Emisor (VISA, MasterCard o AMEX)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Red de la Tarjeta (VISA, MasterCard o AMEX):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val brandList = listOf(
                                Triple("Visa", "VISA", Color(0xFF1A1F71)),
                                Triple("Mastercard", "MasterCard", Color(0xFFEB001B)),
                                Triple("Amex", "AMEX", Color(0xFF006FCF))
                            )
                            brandList.forEach { (brandKey, brandLabel, brandBgColor) ->
                                val isSelected = editNetwork.equals(brandKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) brandBgColor else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) brandBgColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            editNetwork = brandKey
                                            editIsDepartmental = false
                                        }
                                        .testTag("edit_brand_$brandKey")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = brandLabel,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tipo de tarjeta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !editIsDepartmental,
                            onClick = {
                                editIsDepartmental = false
                                // Corrección: antes, al marcar "Crédito Bancario" en edición, el campo
                                // "network" se quedaba en "Departamental" (inconsistencia con el Alta).
                                if (editNetwork.equals("Departamental", ignoreCase = true)) {
                                    editNetwork = "Mastercard"
                                }
                            },
                            label = { Text("Crédito Bancario", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = editIsDepartmental,
                            onClick = {
                                editIsDepartmental = true
                                editNetwork = "Departamental"
                            },
                            label = { Text("Departamental", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Día de Corte y Días de Gracia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editCutoff,
                            onValueChange = { input ->
                                editCutoff = input.filter { it.isDigit() }.take(2)
                                val c = editCutoff.toIntOrNull() ?: 1
                                val g = editGrace.toIntOrNull() ?: 20
                                editDue = CreditCardCalculator.calculatePaymentDueDayFromGrace(c, g).toString()
                            },
                            label = { Text("Día Corte") },
                            placeholder = { Text("1-31") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = editGrace,
                            onValueChange = { input ->
                                editGrace = input.filter { it.isDigit() }.take(3)
                                val c = editCutoff.toIntOrNull() ?: 1
                                val g = editGrace.toIntOrNull() ?: 20
                                editDue = CreditCardCalculator.calculatePaymentDueDayFromGrace(c, g).toString()
                            },
                            label = { Text("Días Gracia") },
                            placeholder = { Text("Ej. 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fecha límite de pago editable manualmente
                    OutlinedTextField(
                        value = editDue,
                        onValueChange = { input ->
                            editDue = input.filter { it.isDigit() }.take(2)
                            val c = editCutoff.toIntOrNull() ?: 1
                            val d = editDue.toIntOrNull() ?: 1
                            val computedGrace = CreditCardCalculator.calculateGraceDaysFromDue(c, d)
                            editGrace = computedGrace.toString()
                        },
                        label = { Text("Día Límite de Pago") },
                        placeholder = { Text("1-31") },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Límite de crédito
                    // Corrección: el filtro anterior solo permitía dígitos y descartaba el punto decimal,
                    // por lo que era imposible teclear centavos aunque el valor se precargara con ellos.
                    OutlinedTextField(
                        value = editLimit,
                        onValueChange = { input ->
                            val filtered = input.filter { c -> c.isDigit() || c == '.' }
                            editLimit = if (filtered.count { it == '.' } > 1) editLimit else filtered
                        },
                        label = { Text("Línea de Crédito (MXN)") },
                        placeholder = { Text("Ej. 50000.00") },
                        prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                        suffix = { Text("MXN", fontSize = 12.sp) },
                        supportingText = {
                            val p = editLimit.toDoubleOrNull() ?: 0.0
                            if (p > 0) {
                                Text("Monto: ${currencyFormat.format(p)}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("edit_credit_limit_input")
                    )

                    // Tasa de interés anual real de la tarjeta (usada en el simulador de pago mínimo)
                    OutlinedTextField(
                        value = editRate,
                        onValueChange = { input ->
                            val filtered = input.filter { c -> c.isDigit() || c == '.' }
                            editRate = if (filtered.count { it == '.' } > 1) editRate else filtered
                        },
                        label = { Text("Tasa de Interés Anual (%)") },
                        placeholder = { Text("Ej. 55.0") },
                        supportingText = { Text("Tasa ordinaria anual de tu contrato, usada en el simulador de pago mínimo", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCardDates(
                            card,
                            editName,
                            cutoffNum,
                            dueNum,
                            parsedLimit,
                            graceNum,
                            editIsDepartmental,
                            editCardholder,
                            editPrimaryHex,
                            editSecondaryHex,
                            editNetwork,
                            editBank,
                            parsedRate
                        )
                        editingCard = null
                    },
                    enabled = !isDuplicateColor && editName.isNotBlank() && editCardholder.isNotBlank() && editBank.isNotBlank()
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCard = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para registrar una nueva tarjeta
    if (showAddDialog) {
        val usedPrimaryHexes = remember(cards) { cards.map { it.primaryColorHex }.toSet() }

        var cardholderName by remember { mutableStateOf("") }
        var cardName by remember { mutableStateOf("") }
        var bankInput by remember { mutableStateOf("") }
        var isDepartmental by remember { mutableStateOf(false) }
        var cutoffText by remember { mutableStateOf("15") }
        var graceDaysText by remember { mutableStateOf("20") }
        var paymentDueText by remember { mutableStateOf("5") }
        var limitText by remember { mutableStateOf("") }
        var lastDigits by remember { mutableStateOf("") }
        var network by remember { mutableStateOf("Mastercard") }
        var showCustomPickerForAdd by remember { mutableStateOf(false) }

        // Color inicial: primer color no usado
        val initialColor = remember(usedPrimaryHexes) {
            AppCardColorPalette.firstOrNull { it.primary !in usedPrimaryHexes } ?: AppCardColorPalette.first()
        }
        var selectedPrimaryHex by remember { mutableLongStateOf(initialColor.primary) }
        var selectedSecondaryHex by remember { mutableLongStateOf(initialColor.secondary) }

        if (showCustomPickerForAdd) {
            CustomColorPickerDialog(
                usedPrimaryHexes = usedPrimaryHexes,
                onDismiss = { showCustomPickerForAdd = false },
                onApplyColor = { p, s ->
                    selectedPrimaryHex = p
                    selectedSecondaryHex = s
                }
            )
        }

        val matchingBanks = remember(bankInput) {
            MexicanBanks.findMatching(bankInput)
        }

        val liveBranding = remember(cardName, bankInput, network) {
            CardBrandingEngine.matchRealCardBranding(cardName, bankInput, network)
        }

        fun applyBank(info: MexicanBankInfo) {
            bankInput = info.name
            isDepartmental = info.isDepartmental
            if (info.isDepartmental) {
                network = "Departamental"
            }
            // Si el color del banco no está en uso, aplicarlo; si ya está en uso, asignar el primer color libre
            if (!usedPrimaryHexes.contains(info.primaryColorHex)) {
                selectedPrimaryHex = info.primaryColorHex
                selectedSecondaryHex = info.secondaryColorHex
            } else {
                val nextAvailable = AppCardColorPalette.firstOrNull { it.primary !in usedPrimaryHexes }
                if (nextAvailable != null) {
                    selectedPrimaryHex = nextAvailable.primary
                    selectedSecondaryHex = nextAvailable.secondary
                }
            }
        }

        val cutoffNum = (cutoffText.toIntOrNull() ?: 15).coerceIn(1, 31)
        val graceNum = (graceDaysText.toIntOrNull() ?: 20).coerceAtLeast(1)
        val dueNum = (paymentDueText.toIntOrNull() ?: 5).coerceIn(1, 31)
        val parsedLimit = CreditCardCalculator.parseLocalizedDouble(limitText) ?: 0.0
        val isColorDuplicate = usedPrimaryHexes.contains(selectedPrimaryHex)

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva Tarjeta", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Previsualización en vivo compacta para no estorbar los campos
                    CreditCardVisual(
                        card = CreditCard(
                            name = if (cardName.isNotBlank()) cardName else "Mi Tarjeta",
                            bank = if (bankInput.isNotBlank()) bankInput else "Banco Emisor",
                            cutoffDay = cutoffNum,
                            paymentDueDay = dueNum,
                            creditLimit = parsedLimit,
                            primaryColorHex = selectedPrimaryHex,
                            secondaryColorHex = selectedSecondaryHex,
                            last4Digits = if (lastDigits.isNotBlank()) lastDigits else "••••",
                            network = network,
                            isDepartmental = isDepartmental,
                            graceDays = graceNum,
                            cardholderName = if (cardholderName.isNotBlank()) cardholderName else "Titular"
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isCompact = true
                    )

                    // Requisito 4: Al registrar una tarjeta nueva, pregunta el nombre del titular
                    OutlinedTextField(
                        value = cardholderName,
                        onValueChange = { cardholderName = it },
                        label = { Text("Nombre del Titular") },
                        placeholder = { Text("Ej. Juan Pérez") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_cardholder_input")
                    )

                    // Requisito 3: Selector de Color Único (no podrá haber dos tarjetas del mismo color)
                    Column {
                        Text(
                            text = "Color de la Tarjeta (Único por tarjeta):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CardColorPickerRow(
                            selectedPrimaryHex = selectedPrimaryHex,
                            usedPrimaryHexes = usedPrimaryHexes,
                            onColorSelected = { p, s ->
                                selectedPrimaryHex = p
                                selectedSecondaryHex = s
                            },
                            onOpenCustomPicker = { showCustomPickerForAdd = true }
                        )

                        if (isColorDuplicate) {
                            Text(
                                text = "⚠️ Este color ya está asignado a otra tarjeta. Selecciona uno disponible.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Requisito Explícito: Preguntar si es VISA, MasterCard o AMEX
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "¿Es VISA, MasterCard o AMEX? *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Selecciona la red emisora de tu tarjeta:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val brandList = listOf(
                                Triple("Visa", "VISA", Color(0xFF1A1F71)),
                                Triple("Mastercard", "MasterCard", Color(0xFFEB001B)),
                                Triple("Amex", "AMEX", Color(0xFF006FCF))
                            )
                            brandList.forEach { (brandKey, brandLabel, brandBgColor) ->
                                val isSelected = network.equals(brandKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) brandBgColor else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) brandBgColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            network = brandKey
                                            isDepartmental = false
                                        }
                                        .testTag("add_brand_$brandKey")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = brandLabel,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tipo de tarjeta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isDepartmental,
                            onClick = {
                                isDepartmental = false
                                if (network == "Departamental") network = "Mastercard"
                            },
                            label = { Text("Crédito Bancario", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = isDepartmental,
                            onClick = {
                                isDepartmental = true
                                network = "Departamental"
                            },
                            label = { Text("Departamental", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Nombre de la Tarjeta") },
                        placeholder = { Text("Ej. Like U, Oro BBVA, Azul, Platinum") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campo Banco con autocompletado
                    OutlinedTextField(
                        value = bankInput,
                        onValueChange = { input ->
                            bankInput = input
                            val exactMatch = MexicanBanks.findExactOrBest(input)
                            if (exactMatch != null && exactMatch.name.equals(input.trim(), ignoreCase = true)) {
                                applyBank(exactMatch)
                            }
                        },
                        label = { Text("Banco en México (manual o sugerido)") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        placeholder = { Text("Ej. BBVA, Santander, Banamex...") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Chips de autocompletado rápido
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingBanks.take(8).forEach { bankInfo ->
                            FilterChip(
                                selected = bankInput.equals(bankInfo.name, ignoreCase = true),
                                onClick = { applyBank(bankInfo) },
                                label = { Text(bankInfo.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Día de Corte y Días de Gracia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cutoffText,
                            onValueChange = { input ->
                                cutoffText = input.filter { it.isDigit() }.take(2)
                                val c = cutoffText.toIntOrNull() ?: 1
                                val g = graceDaysText.toIntOrNull() ?: 20
                                paymentDueText = CreditCardCalculator.calculatePaymentDueDayFromGrace(c, g).toString()
                            },
                            label = { Text("Día Corte") },
                            placeholder = { Text("1-31") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = graceDaysText,
                            onValueChange = { input ->
                                graceDaysText = input.filter { it.isDigit() }.take(3)
                                val c = cutoffText.toIntOrNull() ?: 1
                                val g = graceDaysText.toIntOrNull() ?: 20
                                paymentDueText = CreditCardCalculator.calculatePaymentDueDayFromGrace(c, g).toString()
                            },
                            label = { Text("Días Gracia") },
                            placeholder = { Text("Ej. 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fecha límite de pago editable manualmente
                    OutlinedTextField(
                        value = paymentDueText,
                        onValueChange = { input ->
                            paymentDueText = input.filter { it.isDigit() }.take(2)
                            val c = cutoffText.toIntOrNull() ?: 1
                            val d = paymentDueText.toIntOrNull() ?: 1
                            val calculatedGrace = CreditCardCalculator.calculateGraceDaysFromDue(c, d)
                            graceDaysText = calculatedGrace.toString()
                        },
                        label = { Text("Día Límite de Pago (editable)") },
                        placeholder = { Text("1-31") },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Línea de crédito y últimos 4 dígitos
                    // Corrección: el filtro anterior solo permitía dígitos, sin punto decimal.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = limitText,
                            onValueChange = { input ->
                                val filtered = input.filter { c -> c.isDigit() || c == '.' }
                                limitText = if (filtered.count { it == '.' } > 1) limitText else filtered
                            },
                            label = { Text("Línea Crédito (MXN)") },
                            placeholder = { Text("Ej. 40000.00") },
                            prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                            suffix = { Text("MXN", fontSize = 12.sp) },
                            supportingText = {
                                val p = limitText.toDoubleOrNull() ?: 0.0
                                if (p > 0) {
                                    Text("Monto: ${currencyFormat.format(p)}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.3f).testTag("add_credit_limit_input")
                        )

                        OutlinedTextField(
                            value = lastDigits,
                            onValueChange = { lastDigits = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Últimos 4") },
                            placeholder = { Text("1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            },
            confirmButton = {
                val canCreate = cardName.trim().isNotBlank() && bankInput.trim().isNotBlank() && !isColorDuplicate
                Button(
                    onClick = {
                        if (canCreate) {
                            onAddCard(
                                cardName.trim(),
                                bankInput.trim(),
                                cutoffNum,
                                dueNum,
                                parsedLimit,
                                selectedPrimaryHex,
                                selectedSecondaryHex,
                                lastDigits.filter { it.isDigit() }.ifBlank { "0000" },
                                network,
                                isDepartmental,
                                graceNum,
                                cardholderName.trim().ifBlank { "Titular" }
                            )
                            showAddDialog = false
                        }
                    },
                    enabled = canCreate
                ) {
                    Text("Registrar Tarjeta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
