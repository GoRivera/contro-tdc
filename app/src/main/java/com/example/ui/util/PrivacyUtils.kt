package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import java.text.FieldPosition
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

/**
 * CompositionLocal to provide current privacy mode state to any Composable.
 */
val LocalPrivacyMode = compositionLocalOf { false }

/**
 * Specialized NumberFormat that returns masked text when privacy mode is active.
 */
class PrivacyNumberFormat(
    private val delegate: NumberFormat,
    private val isPrivate: Boolean
) : NumberFormat() {
    override fun format(number: Double, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
        return if (isPrivate) {
            toAppendTo.append("$ ••••••")
        } else {
            delegate.format(number, toAppendTo, pos)
        }
    }

    override fun format(number: Long, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
        return if (isPrivate) {
            toAppendTo.append("$ ••••••")
        } else {
            delegate.format(number, toAppendTo, pos)
        }
    }

    override fun parse(source: String, parsePosition: ParsePosition): Number? {
        return delegate.parse(source, parsePosition)
    }
}

@Composable
fun rememberPrivacyCurrencyFormat(): NumberFormat {
    val isPrivate = LocalPrivacyMode.current
    return remember(isPrivate) {
        val base = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        PrivacyNumberFormat(base, isPrivate)
    }
}

/**
 * Utility for formatting financial values with privacy masking when enabled.
 */
object PrivacyFormat {
    private val defaultCurrency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    fun format(amount: Double, isPrivate: Boolean, customFormatter: NumberFormat? = null): String {
        val fmt = customFormatter ?: defaultCurrency
        return if (isPrivate) "$ ••••••" else fmt.format(amount)
    }

    fun formatNumber(numberString: String, isPrivate: Boolean): String {
        return if (isPrivate) "••••" else numberString
    }
}

/**
 * Ergonomic Haptic Feedback helper for tactile response across the application.
 */
object AppHaptics {
    fun light(haptic: HapticFeedback?, enabled: Boolean = true) {
        if (enabled) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    fun heavy(haptic: HapticFeedback?, enabled: Boolean = true) {
        if (enabled) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }
}

