package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.FuelEntry
import com.example.data.model.Subscription
import com.example.data.model.UserProfile
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.util.AppHaptics
import com.example.data.sync.FirebaseAccountInfo
import com.example.data.sync.SyncState

@Composable
fun AccountScreen(
    userProfile: UserProfile,
    cards: List<CreditCard>,
    subscriptions: List<Subscription>,
    fuelEntries: List<FuelEntry>,
    syncState: SyncState = SyncState.Idle,
    firebaseUser: FirebaseAccountInfo? = null,
    isPrivacyMode: Boolean = false,
    onTogglePrivacyMode: () -> Unit = {},
    isHapticsEnabled: Boolean = true,
    onToggleHaptics: (Boolean) -> Unit = {},
    isAppLockEnabled: Boolean = false,
    onSetAppLock: (enabled: Boolean, pin: String) -> Unit = { _, _ -> },
    onSaveProfile: (fullName: String, email: String, shortName: String) -> Unit,
    onSyncToCloud: () -> Unit = {},
    onRestoreFromCloud: () -> Unit = {},
    onSignOutCloud: () -> Unit = {},
    onLaunchGoogleSignIn: () -> Unit = {},
    onClose: () -> Unit
) {
    // Corrección del bug de "se pierde lo que estabas escribiendo": antes, remember(userProfile) volvía
    // a inicializar estos campos cada vez que userProfile cambiaba (p. ej. tras un inicio de sesión con
    // Google o una restauración desde la nube), aunque el usuario ya estuviera escribiendo algo distinto
    // en el formulario. Ahora solo se sincroniza un campo desde afuera si el usuario no lo había tocado.
    var lastSyncedProfile by remember { mutableStateOf(userProfile) }
    var fullName by remember { mutableStateOf(userProfile.fullName) }
    var email by remember { mutableStateOf(userProfile.email) }
    var shortName by remember { mutableStateOf(userProfile.shortName) }
    var showSavedFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        if (userProfile != lastSyncedProfile) {
            if (fullName == lastSyncedProfile.fullName) fullName = userProfile.fullName
            if (email == lastSyncedProfile.email) email = userProfile.email
            if (shortName == lastSyncedProfile.shortName) shortName = userProfile.shortName
            lastSyncedProfile = userProfile
        }
    }

    var showPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf<String?>(null) }

    // Corrección: "Restaurar" y "Cerrar Sesión de la Nube" no pedían confirmación pese a poder
    // sobrescribir/perder datos, a diferencia de "Eliminar tarjeta" que sí confirma.
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("account_screen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Encabezado con botón de regreso explícito (antes "onClose" no se usaba en ningún botón visible)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_account_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mi Cuenta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Tarjeta de Identidad / Avatar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.initials,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = userProfile.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = userProfile.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Titular Principal • Control TDC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Formulario de edición del perfil
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "DATOS DE LA CUENTA Y PERFIL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Configura tu nombre de titular para que se use en la titularidad de tarjetas, en la división de suscripciones y en tus comprobantes.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; showSavedFeedback = false },
                        label = { Text("Nombre Completo del Titular") },
                        placeholder = { Text("Ej. Juan Pérez") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_fullname")
                    )

                    OutlinedTextField(
                        value = shortName,
                        onValueChange = { shortName = it; showSavedFeedback = false },
                        label = { Text("Nombre Corto / Alias") },
                        placeholder = { Text("Ej. Juan") },
                        supportingText = { Text("Se utilizará para etiquetar tus cuotas en suscripciones y gastos.") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_shortname")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; showSavedFeedback = false },
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("Ej. usuario@correo.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_email")
                    )

                    val isNameValid = fullName.trim().isNotBlank()
                    val isEmailValid = email.trim().isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
                    val canSaveAccount = isNameValid && isEmailValid

                    if (!isNameValid && fullName.isNotEmpty()) {
                        Text(
                            text = "El nombre del titular no puede estar vacío",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    } else if (!isEmailValid) {
                        Text(
                            text = "Ingresa un formato de correo electrónico válido",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (showSavedFeedback) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2E6C38).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF2E6C38).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E6C38), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "¡Datos de cuenta guardados correctamente!",
                                    color = Color(0xFF2E6C38),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (canSaveAccount) {
                                onSaveProfile(fullName.trim(), email.trim(), shortName.trim())
                                showSavedFeedback = true
                            }
                        },
                        enabled = canSaveAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_account_profile")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios de Cuenta", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Resumen de Productos del Usuario
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RESUMEN DE PRODUCTOS VINCULADOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tarjetas
                        ProductStatItem(
                            icon = Icons.Default.CreditCard,
                            count = cards.size,
                            label = "Tarjetas de Crédito",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Suscripciones
                        ProductStatItem(
                            icon = Icons.Default.Subscriptions,
                            count = subscriptions.size,
                            label = "Suscripciones",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Cargas Gasolina
                        ProductStatItem(
                            icon = Icons.Default.LocalGasStation,
                            count = fuelEntries.size,
                            label = "Cargas Gasolina",
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Tarjeta de Sincronización en la Nube con Firebase & Google Sign-In
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (firebaseUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (firebaseUser != null) Color(0xFF2E6C38) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SINCRONIZACIÓN EN LA NUBE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = if (firebaseUser != null) "Conectado como ${firebaseUser.email}" else "Firebase Cloud Sync (Multi-dispositivo)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Estado actual de sincronización
                    when (syncState) {
                        is SyncState.Syncing -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Sincronizando datos con Cloud Firestore...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        is SyncState.Success -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E6C38).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFF2E6C38).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E6C38), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = syncState.message,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E6C38)
                                    )
                                }
                            }
                        }
                        is SyncState.Error -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = syncState.errorMessage,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        is SyncState.FirebaseNotConfigured -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Esperando vinculación de Firebase",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = "Coloca tu google-services.json para habilitar la nube y sincronizar con tus otros dispositivos.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        is SyncState.Idle -> {}
                    }

                    // Acciones de autenticación y sincronización
                    if (firebaseUser == null) {
                        Button(
                            onClick = onLaunchGoogleSignIn,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Sesión con Google", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSyncToCloud,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Subir a Nube", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showRestoreConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restaurar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showSignOutConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cerrar Sesión de la Nube", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Centro de Seguridad, Privacidad y Ergonomía (Fase 2)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().testTag("card_security_privacy_center")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacidad, Seguridad y Ergonomía",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Modo Privacidad (Ocultar Cifras)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isPrivacyMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Modo Privacidad (Ocultar montos)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Muestra •••• en saldos y límites para evitar miradas indiscretas.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPrivacyMode,
                            onCheckedChange = {
                                AppHaptics.light(haptic, isHapticsEnabled)
                                onTogglePrivacyMode()
                            },
                            modifier = Modifier.testTag("switch_privacy_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Respuesta Háptica
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (isHapticsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Respuesta Háptica Táctil",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Vibración ergonómica al pulsar botones, abonos y registros.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isHapticsEnabled,
                            onCheckedChange = { checked ->
                                AppHaptics.light(haptic, true)
                                onToggleHaptics(checked)
                            },
                            modifier = Modifier.testTag("switch_haptics")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Bloqueo con PIN de 4 dígitos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAppLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Bloqueo de App con PIN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isAppLockEnabled) "PIN de 4 dígitos activo para proteger el acceso."
                                    else "Protege la apertura de la app con un código de seguridad.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                AppHaptics.light(haptic, isHapticsEnabled)
                                if (enabled) {
                                    newPinInput = ""
                                    confirmPinInput = ""
                                    pinDialogError = null
                                    showPinDialog = true
                                } else {
                                    onSetAppLock(false, "")
                                }
                            },
                            modifier = Modifier.testTag("switch_app_lock")
                        )
                    }

                    if (isAppLockEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                newPinInput = ""
                                confirmPinInput = ""
                                pinDialogError = null
                                showPinDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cambiar PIN de 4 dígitos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔒 Tus datos financieros y personales se guardan de forma 100% local y segura en este dispositivo con protección de privacidad.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Confirmación antes de restaurar desde la nube (puede sobrescribir datos locales no sincronizados)
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("¿Restaurar datos desde la nube?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Esto traerá tarjetas, gastos, abonos, suscripciones y cargas de gasolina guardados en tu cuenta. Los registros que ya tengas en este dispositivo se conservan y solo se actualizan los que coincidan con la nube.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        onRestoreFromCloud()
                    }
                ) { Text("Restaurar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Confirmación antes de cerrar sesión de la nube (deja de sincronizar hasta volver a iniciar sesión)
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Cerrar sesión de la nube?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tus datos seguirán guardados en este dispositivo y en tu cuenta, pero dejarán de sincronizarse hasta que vuelvas a iniciar sesión.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        onSignOutCloud()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cerrar Sesión") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para configurar o cambiar el PIN de 4 dígitos
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (isAppLockEnabled) "Cambiar PIN de Seguridad" else "Configurar PIN de 4 Dígitos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Elige un código numérico de 4 dígitos para proteger tu app:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                newPinInput = it
                                pinDialogError = null
                            }
                        },
                        label = { Text("Nuevo PIN (4 dígitos)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                confirmPinInput = it
                                pinDialogError = null
                            }
                        },
                        label = { Text("Confirmar PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin")
                    )

                    if (pinDialogError != null) {
                        Text(
                            text = pinDialogError ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length != 4) {
                            pinDialogError = "El PIN debe tener exactamente 4 dígitos."
                        } else if (newPinInput != confirmPinInput) {
                            pinDialogError = "Los PINs no coinciden."
                        } else {
                            AppHaptics.light(haptic, isHapticsEnabled)
                            onSetAppLock(true, newPinInput)
                            showPinDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_save_pin")
                ) {
                    Text("Guardar PIN")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProductStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
