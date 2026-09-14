package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.FirebaseInitializer
import com.example.notifications.NotificationHelper
import com.example.notifications.PaymentReminderWorker
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CreditCardViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: CreditCardViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Si se niega, los recordatorios simplemente no se muestran; no hace falta manejarlo aquí. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Asegurar inicialización de Firebase para sincronización en la nube y autenticación
        FirebaseInitializer.ensureInitialized(this)

        // Recordatorios de fecha límite de pago (item 14): se agenda una revisión diaria en
        // segundo plano y se pide el permiso de notificaciones (requerido desde Android 13).
        NotificationHelper.ensureChannel(this)
        PaymentReminderWorker.schedule(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Acceso directo de la app (mantener presionado el ícono): registrar gasto/abono en un toque.
        val shortcutAction = intent?.getStringExtra("shortcut_action")
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = darkTheme) {
                MainScreen(viewModel = viewModel, initialAction = shortcutAction)
            }
        }
    }
}
