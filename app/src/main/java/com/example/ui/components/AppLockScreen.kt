package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.util.AppHaptics
import com.example.ui.util.BiometricHelper

@Composable
fun AppLockScreen(
    onUnlockAttempt: (String) -> Boolean,
    onBiometricUnlock: () -> Unit = {},
    hapticsEnabled: Boolean = true
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val isBiometricAvailable = remember(context) { BiometricHelper.isBiometricAvailable(context) }

    fun triggerBiometrics() {
        if (activity != null && isBiometricAvailable) {
            BiometricHelper.authenticate(
                activity = activity,
                title = "Desbloqueo de Control TDC",
                subtitle = "Usa tu huella dactilar para acceder a la aplicación",
                negativeButtonText = "Usar PIN",
                onSuccess = {
                    AppHaptics.light(haptic, hapticsEnabled)
                    onBiometricUnlock()
                },
                onError = { _, errString ->
                    // Si el usuario canceló explícitamente para usar PIN, no mostramos error ruidoso
                    if (!errString.contains("cancel", ignoreCase = true)) {
                        errorMessage = errString
                    }
                },
                onFailed = {
                    AppHaptics.heavy(haptic, hapticsEnabled)
                    errorMessage = "Huella no reconocida. Intenta de nuevo o ingresa tu PIN."
                }
            )
        }
    }

    // Al aparecer la pantalla, si el sensor biométrico está listo, abre el lector de huella
    LaunchedEffect(Unit) {
        if (isBiometricAvailable && activity != null) {
            triggerBiometrics()
        }
    }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            val success = onUnlockAttempt(enteredPin)
            if (success) {
                AppHaptics.light(haptic, hapticsEnabled)
                errorMessage = null
            } else {
                AppHaptics.heavy(haptic, hapticsEnabled)
                errorMessage = "PIN incorrecto. Intenta de nuevo."
                enteredPin = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueo de seguridad",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Control TDC Protegido",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ingresa tu PIN de 4 dígitos para continuar",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4-dot PIN indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFilled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Numeric Keypad (3 columns)
            val leftAction = if (isBiometricAvailable) "BIO" else "C"
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(leftAction, "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            KeypadButton(
                                label = key,
                                onClick = {
                                    AppHaptics.light(haptic, hapticsEnabled)
                                    when (key) {
                                        "BIO" -> {
                                            triggerBiometrics()
                                        }
                                        "C" -> {
                                            enteredPin = ""
                                            errorMessage = null
                                        }
                                        "DEL" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                errorMessage = null
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (isBiometricAvailable) {
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedButton(
                    onClick = { triggerBiometrics() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("biometric_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Sensor de huella",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Usar sensor de huella",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val isAction = label == "C" || label == "DEL" || label == "BIO"
    Surface(
        shape = CircleShape,
        color = if (label == "BIO") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else if (isAction) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("keypad_btn_$label")
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (label) {
                "DEL" -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Borrar dígito",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                "BIO" -> {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Desbloquear con Huella",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Text(
                        text = label,
                        fontSize = if (label == "C") 16.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (label == "C") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
