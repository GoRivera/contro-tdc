package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NaturalPrimaryGreenDark,
    onPrimary = NaturalOnPrimaryDark,
    primaryContainer = NaturalPrimaryContainerDark,
    onPrimaryContainer = NaturalOnPrimaryContainerDark,
    secondary = NaturalSecondaryDark,
    onSecondary = NaturalSurfaceDark, // NaturalSecondaryDark es un azul claro; texto oscuro para contraste
    secondaryContainer = NaturalSecondaryContainerDark,
    onSecondaryContainer = NaturalOnSecondaryContainerDark,
    tertiary = NaturalTertiaryDark,
    tertiaryContainer = NaturalTertiaryContainerDark,
    onTertiaryContainer = NaturalOnTertiaryContainerDark,
    background = NaturalBackgroundDark,
    onBackground = NaturalTextPrimaryDark,
    surface = NaturalSurfaceDark,
    onSurface = NaturalTextPrimaryDark,
    surfaceVariant = NaturalSurfaceVariantDark,
    onSurfaceVariant = NaturalTextSecondaryDark,
    outline = NaturalOutlineDark,
    error = NaturalError,
    errorContainer = NaturalErrorContainer,
    onErrorContainer = NaturalOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalPrimaryGreen,
    onPrimary = NaturalOnPrimary,
    primaryContainer = NaturalPrimaryContainer,
    onPrimaryContainer = NaturalOnPrimaryContainer,
    secondary = NaturalSecondary,
    onSecondary = NaturalOnPrimary, // blanco: NaturalSecondary es un azul medio-oscuro
    secondaryContainer = NaturalSecondaryContainer,
    onSecondaryContainer = NaturalOnSecondaryContainer,
    background = NaturalBackgroundLight,
    onBackground = NaturalTextPrimary,
    surface = NaturalSurfaceLight,
    onSurface = NaturalTextPrimary,
    surfaceVariant = NaturalSurfaceVariantLight,
    onSurfaceVariant = NaturalTextSecondary,
    outline = NaturalOutline,
    outlineVariant = NaturalOutlineVariant,
    error = NaturalError,
    errorContainer = NaturalErrorContainer,
    onErrorContainer = NaturalOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep handcrafted Natural Tones theme consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
