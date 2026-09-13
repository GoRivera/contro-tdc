package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import com.example.data.AppDatabase
import com.example.data.PrefsKeys
import com.example.data.appSettingsDataStore
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.FuelEntry
import com.example.data.model.Payment
import com.example.data.model.ServiceEntry
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import com.example.data.model.UserProfile
import com.example.data.repository.CardRepository
import com.example.auth.GoogleAuthManager
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.FirebaseAccountInfo
import com.example.data.sync.SyncState
import com.example.domain.CardRecommendation
import com.example.domain.CashFlowRelease
import com.example.domain.CreditCardCalculator
import com.example.domain.MsiSummary
import com.example.domain.StatementSummary
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CreditCardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = CardRepository(
        database.creditCardDao(),
        database.expenseDao(),
        database.paymentDao(),
        database.subscriptionDao(),
        database.fuelEntryDao(),
        database.serviceEntryDao()
    )

    val cloudSyncManager = CloudSyncManager(application, repository)
    val googleAuthManager = GoogleAuthManager(application)

    val syncState: StateFlow<SyncState> = cloudSyncManager.syncState
    val currentFirebaseUser: StateFlow<FirebaseAccountInfo?> = cloudSyncManager.currentUser

    private val prefs = application.getSharedPreferences("app_user_prefs", android.content.Context.MODE_PRIVATE)
    private val _userProfile = MutableStateFlow(
        UserProfile(
            fullName = prefs.getString("user_fullname", "Usuario Principal") ?: "Usuario Principal",
            email = prefs.getString("user_email", "") ?: "",
            shortName = prefs.getString("user_shortname", "Usuario") ?: "Usuario",
            initials = prefs.getString("user_initials", "US") ?: "US"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun updateUserProfile(fullName: String, email: String, shortName: String) {
        val safeFull = fullName.trim().ifBlank { "Usuario Principal" }
        val safeEmail = email.trim()
        val safeShort = shortName.trim().ifBlank { safeFull.split(" ").firstOrNull() ?: "Usuario" }
        val initials = safeFull.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifBlank { "US" }

        prefs.edit()
            .putString("user_fullname", safeFull)
            .putString("user_email", safeEmail)
            .putString("user_shortname", safeShort)
            .putString("user_initials", initials)
            .apply()

        _userProfile.value = UserProfile(
            fullName = safeFull,
            email = safeEmail,
            shortName = safeShort,
            initials = initials
        )
    }

    // Fase 2 + item 4: Privacidad, Ergonomía y listas personalizables ahora viven en Jetpack
    // DataStore en vez de SharedPreferences (más seguro con corrutinas, evita bugs de lectura/
    // escritura concurrente). El bloqueo con PIN y el perfil de usuario se quedan en
    // SharedPreferences (`prefs`) a propósito: son datos sensibles/con lógica síncrona al arrancar
    // la app, y migrarlos sin poder compilar ni probar la app localmente es un riesgo de seguridad
    // innecesario (la app podría abrir momentáneamente sin PIN mientras carga el valor real).
    // SharedPreferencesMigration copia automáticamente, la primera vez, cualquier valor que ya
    // existiera en SharedPreferences para estas mismas claves, así que nada de lo que el usuario
    // ya haya configurado se pierde con la migración.
    private val dataStore = application.appSettingsDataStore

    private val _isPrivacyMode = dataStore.data
        .map { it[PrefsKeys.PRIVACY_MODE] ?: prefs.getBoolean("privacy_mode_enabled", false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), prefs.getBoolean("privacy_mode_enabled", false))
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode

    fun togglePrivacyMode() {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.PRIVACY_MODE] = !(it[PrefsKeys.PRIVACY_MODE] ?: _isPrivacyMode.value) }
        }
    }

    fun setPrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.PRIVACY_MODE] = enabled }
        }
    }

    // Selector manual de tema claro/oscuro (además de seguir al sistema, como antes).
    private val _themeMode = dataStore.data
        .map { prefsData ->
            val stored = prefsData[PrefsKeys.THEME_MODE] ?: prefs.getString("theme_mode", null)
            ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            ThemeMode.entries.firstOrNull { it.name == prefs.getString("theme_mode", null) } ?: ThemeMode.SYSTEM
        )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.THEME_MODE] = mode.name }
        }
    }

    private val _isHapticEnabled = dataStore.data
        .map { it[PrefsKeys.HAPTICS_ENABLED] ?: prefs.getBoolean("haptics_enabled", true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), prefs.getBoolean("haptics_enabled", true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.HAPTICS_ENABLED] = enabled }
        }
    }

    // Listas personalizables de conceptos sugeridos y personas/beneficiarios: antes vivían solo como
    // estado local de la pantalla de registrar gasto (remember), por lo que se reiniciaban a los
    // valores por defecto cada vez que se cerraba y volvía a abrir el formulario, perdiendo cualquier
    // nombre o concepto que el usuario hubiera agregado o quitado. Ahora se guardan en DataStore a
    // través de PersistedStringListPref, que centraliza el patrón de guardar/cargar una lista.
    private val quickConceptsPref = PersistedStringListPref(
        dataStore, viewModelScope, PrefsKeys.QUICK_CONCEPTS, prefs, "quick_concepts",
        listOf("Gasolina", "Despensa Walmart", "Amazon", "TotalPlay", "CFE", "Mercado Pago", "Aurrerá", "Restaurante")
    )
    val quickConcepts: StateFlow<List<String>> = quickConceptsPref.state
    fun updateQuickConcepts(concepts: List<String>) = quickConceptsPref.update(concepts)

    private val peopleListPref = PersistedStringListPref(
        dataStore, viewModelScope, PrefsKeys.PEOPLE_LIST, prefs, "people_list",
        listOf("Personal", "Familiar", "Pareja", "Hijos", "Trabajo", "Amigo")
    )
    val peopleList: StateFlow<List<String>> = peopleListPref.state
    fun updatePeopleList(people: List<String>) = peopleListPref.update(people)

    // Mismo caso para los abonos/pagos: conceptos sugeridos y personas/fuentes de pago.
    private val paymentConceptsPref = PersistedStringListPref(
        dataStore, viewModelScope, PrefsKeys.PAYMENT_CONCEPTS, prefs, "payment_concepts",
        listOf("Pago TDC", "Bonificación", "Devolución / Reembolso", "Abono Terceros", "Abono Familiar")
    )
    val paymentConcepts: StateFlow<List<String>> = paymentConceptsPref.state
    fun updatePaymentConcepts(concepts: List<String>) = paymentConceptsPref.update(concepts)

    private val payersListPref = PersistedStringListPref(
        dataStore, viewModelScope, PrefsKeys.PAYERS_LIST, prefs, "payers_list",
        listOf("Personal", "Familiar", "Pareja", "Banco", "Empresa")
    )
    val payersList: StateFlow<List<String>> = payersListPref.state
    fun updatePayersList(payers: List<String>) = payersListPref.update(payers)

    private val _isAppLockEnabled = MutableStateFlow(prefs.getBoolean("app_lock_enabled", false))
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _appPin = MutableStateFlow(prefs.getString("app_lock_pin", "") ?: "")
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    private val _isAppLocked = MutableStateFlow(_isAppLockEnabled.value && _appPin.value.length == 4)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    fun setAppLock(enabled: Boolean, pin: String) {
        prefs.edit()
            .putBoolean("app_lock_enabled", enabled)
            .putString("app_lock_pin", pin)
            .apply()
        _isAppLockEnabled.value = enabled
        _appPin.value = pin
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _appPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun unlockAppWithBiometrics() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_isAppLockEnabled.value && _appPin.value.length == 4) {
            _isAppLocked.value = true
        }
    }

    val allCards: StateFlow<List<CreditCard>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fuel entries
    val allFuelEntries: StateFlow<List<FuelEntry>> = repository.allFuelEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Servicios (agua, luz, gas) — módulo independiente de tarjetas y gastos
    val allServiceEntries: StateFlow<List<ServiceEntry>> = repository.allServiceEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscriptions flows
    val allSubscriptions: StateFlow<List<Subscription>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _subscriptionYearMonth = MutableStateFlow("2026-09")
    val subscriptionYearMonth: StateFlow<String> = _subscriptionYearMonth.asStateFlow()
    val trackingYearMonth: StateFlow<String> = _subscriptionYearMonth.asStateFlow()

    fun setTrackingYearMonth(ym: String) {
        _subscriptionYearMonth.value = ym
    }

    val subscriptionTrackings: StateFlow<List<SubscriptionPaymentTracking>> = _subscriptionYearMonth
        .flatMapLatest { ym -> repository.getSubscriptionTrackingsForMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Corrige el bug de que el seguimiento de "quién ya pagó" no se regeneraba al cambiar de mes:
        // cada vez que cambia el mes seleccionado de suscripciones (o cambia la lista de suscripciones),
        // nos aseguramos de que exista tracking y cargo para ese mes en cada suscripción activa.
        viewModelScope.launch {
            combine(allSubscriptions, _subscriptionYearMonth) { subs, ym -> subs to ym }
                .collect { (subs, ym) ->
                    ensureSubscriptionDataForMonth(ym, subs)
                }
        }

        // Los MSI no avanzaban de cuota solos: msiCurrentInstallment y targetStatementMonth se
        // quedaban congelados en lo que se capturó al registrar el gasto, porque la única función
        // que los avanzaba (advanceMsiInstallment) no estaba conectada a ningún botón ni proceso
        // automático — el usuario tenía que editar el gasto a mano cada vez que pasaba un corte.
        // Ahora, cada vez que se cargan las tarjetas o los gastos (p. ej. al abrir la app), se
        // resincroniza automáticamente cada MSI activo con la fecha real de hoy: si ya pasó el
        // corte de la tarjeta correspondiente una o más veces desde el último registro, la cuota
        // actual avanza sola (incluso varios meses de golpe si la app no se abrió en un tiempo).
        viewModelScope.launch {
            combine(allExpenses, allCards) { expenses, cards -> expenses to cards }
                .collect { (expenses, cards) ->
                    resyncMsiInstallmentsToToday(expenses, cards)
                }
        }
    }

    /**
     * Recalcula, para cada gasto MSI activo, la cuota que le corresponde HOY según su fecha real de
     * compra y el día de corte de su tarjeta, y avanza msiCurrentInstallment/targetStatementMonth
     * automáticamente si el cálculo indica que ya se debieron cobrar más mensualidades de las
     * registradas. Nunca retrocede una cuota (respeta ajustes manuales hacia adelante) ni la avanza
     * más allá del plazo total.
     */
    private suspend fun resyncMsiInstallmentsToToday(expenses: List<Expense>, cards: List<CreditCard>) {
        val cardMap = cards.associateBy { it.id }
        for (exp in expenses) {
            if (!exp.isMsi) continue
            val card = cardMap[exp.cardId] ?: continue
            val totalMonths = exp.msiTotalMonths.coerceAtLeast(1)
            if (exp.msiCurrentInstallment >= totalMonths) continue

            val timeline = CreditCardCalculator.calculateMsiAutoTimeline(
                purchaseDateMillis = exp.dateMillis,
                cardCutoffDay = card.cutoffDay,
                totalMonths = totalMonths
            )
            if (timeline.currentInstallment > exp.msiCurrentInstallment) {
                val newTargetStatementMonth = CreditCardCalculator.calculateStatementMonthForInstallment(
                    purchaseDateMillis = exp.dateMillis,
                    cardCutoffDay = card.cutoffDay,
                    installmentNumber = timeline.currentInstallment
                )
                repository.updateExpense(
                    exp.copy(
                        msiCurrentInstallment = timeline.currentInstallment,
                        targetStatementMonth = newTargetStatementMonth
                    )
                )
            }
        }
    }

    /**
     * Garantiza que una suscripción activa tenga su registro de seguimiento de pagos (quién ya pagó)
     * y su cargo correspondiente en la tarjeta para [yearMonth], generándolos a partir de la plantilla
     * de participantes del mes anterior más reciente si aún no existen. Es idempotente: si ya existe
     * tracking para ese mes, no hace nada.
     */
    private suspend fun ensureSubscriptionDataForMonth(yearMonth: String, subscriptions: List<Subscription>) {
        for (sub in subscriptions.filter { it.isActive }) {
            if (sub.id == 0L || yearMonth < sub.startMonth) continue

            val existingForMonth = repository.getTrackingsForSubscriptionAndMonth(sub.id, yearMonth)
            if (existingForMonth.isEmpty()) {
                val allTrackingsForSub = repository.getTrackingsForSubscriptionFlow(sub.id).first()
                val latestPreviousMonth = allTrackingsForSub
                    .map { it.yearMonth }
                    .filter { it < yearMonth }
                    .maxOrNull()
                if (latestPreviousMonth != null) {
                    val template = allTrackingsForSub.filter { it.yearMonth == latestPreviousMonth }
                    val newTrackings = template.map { t ->
                        SubscriptionPaymentTracking(
                            subscriptionId = sub.id,
                            yearMonth = yearMonth,
                            participantName = t.participantName,
                            amountOwed = t.amountOwed,
                            isPaid = false,
                            paidDateMillis = null
                        )
                    }
                    if (newTrackings.isNotEmpty()) {
                        repository.insertTrackings(newTrackings)
                    }
                }
            }

            val alreadyCharged = allExpenses.value.any {
                it.subscriptionId == sub.id && it.targetStatementMonth == yearMonth
            }
            if (!alreadyCharged) {
                val cal = Calendar.getInstance()
                val parts = yearMonth.split("-")
                val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
                val month = parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)
                cal.set(year, month - 1, sub.billingDayOfMonth.coerceIn(1, 28), 12, 0, 0)
                val exp = Expense(
                    cardId = sub.cardId,
                    concept = sub.name,
                    amount = sub.totalMonthlyAmount,
                    dateMillis = cal.timeInMillis,
                    category = sub.category,
                    isSubscription = true,
                    subscriptionId = sub.id,
                    targetStatementMonth = yearMonth,
                    firestoreId = java.util.UUID.randomUUID().toString()
                )
                repository.insertExpense(exp)
            }
        }
    }

    // Filter states
    private val _selectedStatementYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedStatementYear: StateFlow<Int> = _selectedStatementYear.asStateFlow()

    private val _selectedStatementMonth = MutableStateFlow(
        SimpleDateFormat("MMMM", Locale("es", "MX")).format(Date()).replaceFirstChar { it.uppercase() }
    )
    val selectedStatementMonth: StateFlow<String> = _selectedStatementMonth.asStateFlow()

    private val _selectedCardId = MutableStateFlow<Long?>(null) // null = all cards
    val selectedCardId: StateFlow<Long?> = _selectedCardId.asStateFlow()

    private val _purchaseDate = MutableStateFlow(Date())
    val purchaseDate: StateFlow<Date> = _purchaseDate.asStateFlow()

    val availableMonths = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // Requisito 3: Años disponibles mostrando SOLO aquellos de los cuales se tenga información
    val availableYears: StateFlow<List<Int>> = combine(allExpenses, allPayments) { expenses, payments ->
        val expYears = expenses.map { CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) }
        val payYears = payments.map { CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) }
        val distinctYears = (expYears + payYears).distinct().sortedDescending()
        if (distinctYears.isEmpty()) {
            listOf(Calendar.getInstance().get(Calendar.YEAR))
        } else {
            distinctYears
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Calendar.getInstance().get(Calendar.YEAR)))

    // Meses con información disponible para el año seleccionado
    val availableMonthsForSelectedYear: StateFlow<List<String>> = combine(
        allExpenses,
        allPayments,
        _selectedStatementYear
    ) { expenses, payments, year ->
        val expMonths = expenses.filter { CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == year }
            .map { CreditCardCalculator.normalizeMonth(it.targetStatementMonth) }
        val payMonths = payments.filter { CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == year }
            .map { CreditCardCalculator.normalizeMonth(it.targetStatementMonth) }
        val foundMonths = (expMonths + payMonths).distinct()
        val monthOrder = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val sorted = foundMonths.sortedBy { m ->
            val idx = monthOrder.indexOf(m)
            if (idx >= 0) idx else 99
        }
        if (sorted.isEmpty()) {
            listOf(CreditCardCalculator.normalizeMonth(_selectedStatementMonth.value))
        } else {
            sorted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Agosto"))

    // Card recommendations for today (or custom purchase date)
    val recommendations: StateFlow<List<CardRecommendation>> = combine(
        allCards,
        _purchaseDate
    ) { cards, pDate ->
        CreditCardCalculator.evaluateCardsForPurchase(cards, pDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // MSI Summaries
    val msiSummaries: StateFlow<List<MsiSummary>> = combine(
        allExpenses,
        allCards
    ) { expenses, cards ->
        val cardMap = cards.associateBy { it.id }
        val msiExpenses = expenses.filter { it.isMsi }
        // Agrupar por tarjeta y concepto base para evitar duplicados si existen registros de periodos anteriores
        val deduplicatedMsi = msiExpenses.groupBy { exp ->
            val cleanConcept = exp.concept.replace(Regex("\\[\\s*\\d+\\s+de\\s+\\d+\\s*\\]", RegexOption.IGNORE_CASE), "").trim().lowercase()
            "${exp.cardId}_${cleanConcept}"
        }.mapValues { (_, group) ->
            // Seleccionar el registro con la cuota más avanzada / reciente
            group.maxByOrNull { it.msiCurrentInstallment } ?: group.first()
        }.values.toList()

        deduplicatedMsi
            .map { expense ->
                CreditCardCalculator.computeMsiSummary(expense, cardMap[expense.cardId])
            }
            .sortedBy { it.installmentsRemaining }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cash flow release projections
    val cashFlowReleases: StateFlow<List<CashFlowRelease>> = msiSummaries.combine(_selectedStatementMonth) { msiList, _ ->
        CreditCardCalculator.projectCashFlowRelease(msiList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statement summary for selected year, month, and card
    val statementSummary: StateFlow<StatementSummary> = combine(
        combine(allExpenses, allPayments, allCards) { expenses, payments, cards ->
            Triple(expenses, payments, cards)
        },
        combine(_selectedStatementYear, _selectedStatementMonth, _selectedCardId) { year, month, cardId ->
            Triple(year, month, cardId)
        }
    ) { (expenses, payments, cards), (year, month, cardId) ->
        val normMonth = CreditCardCalculator.normalizeMonth(month)
        val card = cards.firstOrNull { it.id == cardId }
        val monthExpenses = expenses.filter {
            CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == year &&
            CreditCardCalculator.normalizeMonth(it.targetStatementMonth) == normMonth
        }
        val monthPayments = payments.filter {
            CreditCardCalculator.extractYear(it.dateMillis, it.targetStatementMonth) == year &&
            CreditCardCalculator.normalizeMonth(it.targetStatementMonth) == normMonth
        }
        CreditCardCalculator.computeStatementSummary("$normMonth $year", card, monthExpenses, monthPayments)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatementSummary(
            statementMonth = "Agosto",
            cardId = null,
            cardName = "Todas las Tarjetas",
            cutoffDateString = "",
            paymentDueDateString = "",
            totalCharges = 0.0,
            totalPayments = 0.0,
            remainingBalance = 0.0,
            isFullyPaid = true,
            beneficiaryBreakdown = emptyMap()
        )
    )

    fun setSelectedYear(year: Int) {
        _selectedStatementYear.value = year
    }

    fun setSelectedMonth(month: String) {
        _selectedStatementMonth.value = CreditCardCalculator.normalizeMonth(month)
    }

    fun setSelectedCard(cardId: Long?) {
        _selectedCardId.value = cardId
    }

    fun setPurchaseDate(date: Date) {
        _purchaseDate.value = date
    }

    // Expense actions
    fun addExpense(
        cardId: Long,
        concept: String,
        amount: Double,
        dateMillis: Long,
        beneficiary: String,
        category: String,
        isMsi: Boolean,
        msiTotalMonths: Int,
        msiCurrentInstallment: Int,
        notes: String,
        targetStatementMonth: String,
        msiTotalPurchaseAmount: Double = 0.0
    ) {
        viewModelScope.launch {
            val totalPurchase = if (isMsi) {
                if (msiTotalPurchaseAmount > 0.0) msiTotalPurchaseAmount else amount * msiTotalMonths
            } else {
                amount
            }
            val monthlyCharge = if (isMsi) {
                if (msiTotalPurchaseAmount > 0.0) msiTotalPurchaseAmount / msiTotalMonths else amount
            } else {
                amount
            }
            val normMonth = CreditCardCalculator.normalizeMonth(targetStatementMonth.ifBlank { _selectedStatementMonth.value })
            val yearMatch = Regex("\\b(20\\d\\d)\\b").find(targetStatementMonth)
            val effectiveYear = yearMatch?.value?.toIntOrNull() ?: _selectedStatementYear.value
            val fullStatementMonth = "$normMonth $effectiveYear"

            val expense = Expense(
                cardId = cardId,
                concept = concept.trim(),
                amount = monthlyCharge,
                dateMillis = dateMillis,
                beneficiary = beneficiary.trim().ifBlank { "Personal" },
                category = category,
                isMsi = isMsi,
                msiTotalMonths = msiTotalMonths,
                msiCurrentInstallment = msiCurrentInstallment,
                msiTotalPurchaseAmount = totalPurchase,
                notes = notes,
                targetStatementMonth = fullStatementMonth,
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun advanceMsiInstallment(expense: Expense) {
        viewModelScope.launch {
            if (expense.isMsi && expense.msiCurrentInstallment < expense.msiTotalMonths) {
                val nextInstallment = expense.msiCurrentInstallment + 1
                // Mantiene targetStatementMonth sincronizado con la fecha de compra real al avanzar.
                val card = allCards.value.firstOrNull { it.id == expense.cardId }
                val newTargetStatementMonth = if (card != null) {
                    CreditCardCalculator.calculateStatementMonthForInstallment(
                        purchaseDateMillis = expense.dateMillis,
                        cardCutoffDay = card.cutoffDay,
                        installmentNumber = nextInstallment
                    )
                } else {
                    expense.targetStatementMonth
                }
                val updated = expense.copy(
                    msiCurrentInstallment = nextInstallment,
                    targetStatementMonth = newTargetStatementMonth
                )
                repository.updateExpense(updated)
            }
        }
    }

    // Payment actions
    fun addPayment(
        cardId: Long,
        concept: String,
        amount: Double,
        dateMillis: Long,
        sourcePayer: String,
        targetStatementMonth: String,
        notes: String
    ) {
        viewModelScope.launch {
            val normMonth = CreditCardCalculator.normalizeMonth(targetStatementMonth.ifBlank { _selectedStatementMonth.value })
            val yearMatch = Regex("\\b(20\\d\\d)\\b").find(targetStatementMonth)
            val effectiveYear = yearMatch?.value?.toIntOrNull() ?: _selectedStatementYear.value
            val fullStatementMonth = "$normMonth $effectiveYear"

            val payment = Payment(
                cardId = cardId,
                concept = concept.trim(),
                amount = amount,
                dateMillis = dateMillis,
                sourcePayer = sourcePayer.trim().ifBlank { "Personal" },
                targetStatementMonth = fullStatementMonth,
                notes = notes,
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            repository.insertPayment(payment)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    // Card actions
    fun addCard(
        name: String,
        bank: String,
        cutoffDay: Int,
        paymentDueDay: Int,
        creditLimit: Double,
        primaryColorHex: Long,
        secondaryColorHex: Long,
        last4Digits: String,
        network: String,
        isDepartmental: Boolean = false,
        graceDays: Int = 20,
        cardholderName: String = "TITULAR",
        annualInterestRatePercent: Double = 55.0
    ) {
        viewModelScope.launch {
            val card = CreditCard(
                name = name.trim(),
                bank = bank.trim(),
                cutoffDay = cutoffDay.coerceIn(1, 31),
                paymentDueDay = paymentDueDay.coerceIn(1, 31),
                creditLimit = creditLimit,
                primaryColorHex = primaryColorHex,
                secondaryColorHex = secondaryColorHex,
                last4Digits = last4Digits.trim().takeLast(4).ifBlank { "••••" },
                network = network,
                isDepartmental = isDepartmental,
                graceDays = graceDays.coerceAtLeast(1),
                cardholderName = cardholderName.trim().ifBlank { "TITULAR" },
                annualInterestRatePercent = annualInterestRatePercent.coerceAtLeast(0.0),
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            repository.insertCard(card)
        }
    }

    fun updateCardDates(
        card: CreditCard,
        newName: String = card.name,
        newCutoffDay: Int,
        newPaymentDueDay: Int,
        newLimit: Double,
        newGraceDays: Int = card.graceDays,
        isDepartmental: Boolean = card.isDepartmental,
        cardholderName: String = card.cardholderName,
        primaryColorHex: Long = card.primaryColorHex,
        secondaryColorHex: Long = card.secondaryColorHex,
        newNetwork: String = card.network,
        newBank: String = card.bank,
        newAnnualInterestRatePercent: Double = card.annualInterestRatePercent
    ) {
        viewModelScope.launch {
            val updated = card.copy(
                name = newName.trim().ifBlank { card.name },
                bank = newBank.trim().ifBlank { card.bank },
                cutoffDay = newCutoffDay.coerceIn(1, 31),
                paymentDueDay = newPaymentDueDay.coerceIn(1, 31),
                creditLimit = newLimit,
                graceDays = newGraceDays.coerceAtLeast(1),
                isDepartmental = isDepartmental,
                cardholderName = cardholderName.trim().ifBlank { card.cardholderName },
                primaryColorHex = primaryColorHex,
                secondaryColorHex = secondaryColorHex,
                network = newNetwork.trim().ifBlank { card.network },
                annualInterestRatePercent = newAnnualInterestRatePercent.coerceAtLeast(0.0)
            )
            repository.updateCard(updated)
        }
    }

    fun toggleCardActive(card: CreditCard) {
        viewModelScope.launch {
            val updated = card.copy(isActive = !card.isActive)
            repository.updateCard(updated)
        }
    }

    fun updateCardBranding(card: CreditCard, primaryColorHex: Long, secondaryColorHex: Long) {
        viewModelScope.launch {
            val updated = card.copy(
                primaryColorHex = primaryColorHex,
                secondaryColorHex = secondaryColorHex
            )
            repository.updateCard(updated)
        }
    }

    fun deleteCard(card: CreditCard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    // Subscriptions actions (Requisito 5)
    fun setSubscriptionYearMonth(ym: String) {
        _subscriptionYearMonth.value = ym
    }

    fun addSubscription(
        name: String,
        cardId: Long,
        billingDayOfMonth: Int,
        totalMonthlyAmount: Double,
        category: String,
        participants: List<Pair<String, Double>>, // List of Name to Amount
        periodicity: String = "MENSUAL"
    ) {
        viewModelScope.launch {
            val summary = participants.joinToString(", ") { "${it.first}: \$${String.format(java.util.Locale.US, "%.2f", it.second)}" }
            val currentYM = _subscriptionYearMonth.value

            val subId = repository.insertSubscription(
                Subscription(
                    name = name.trim(),
                    cardId = cardId,
                    billingDayOfMonth = billingDayOfMonth.coerceIn(1, 31),
                    totalMonthlyAmount = totalMonthlyAmount,
                    category = category.trim().ifBlank { "Servicios" },
                    startMonth = currentYM,
                    participantsSummary = summary,
                    periodicity = periodicity,
                    firestoreId = java.util.UUID.randomUUID().toString()
                )
            )

            // Insert initial trackings for current month
            val trackings = participants.map { (pName, amount) ->
                SubscriptionPaymentTracking(
                    subscriptionId = subId,
                    yearMonth = currentYM,
                    participantName = pName.trim(),
                    amountOwed = amount,
                    isPaid = false,
                    firestoreId = java.util.UUID.randomUUID().toString()
                )
            }
            repository.insertTrackings(trackings)

            // Create initial expense charge for the current month linked to subscription
            val cal = Calendar.getInstance()
            val (yearStr, monthStr) = currentYM.split("-")
            val year = yearStr.toIntOrNull() ?: cal.get(Calendar.YEAR)
            val month = monthStr.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)
            cal.set(year, month - 1, billingDayOfMonth.coerceIn(1, 28), 12, 0, 0)

            val exp = Expense(
                cardId = cardId,
                concept = name.trim(),
                amount = totalMonthlyAmount,
                dateMillis = cal.timeInMillis,
                category = category.trim().ifBlank { "Servicios" },
                isSubscription = true,
                subscriptionId = subId,
                targetStatementMonth = currentYM,
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            repository.insertExpense(exp)
        }
    }

    fun updateSubscription(
        subscription: Subscription,
        newName: String,
        newCardId: Long,
        newBillingDay: Int,
        newTotalMonthlyAmount: Double,
        newCategory: String,
        participants: List<Pair<String, Double>>,
        affectCurrentMonth: Boolean = true,
        periodicity: String = "MENSUAL"
    ) {
        viewModelScope.launch {
            val summary = participants.joinToString(", ") { "${it.first}: \$${String.format(java.util.Locale.US, "%.2f", it.second)}" }
            val updated = subscription.copy(
                name = newName.trim().ifBlank { subscription.name },
                cardId = newCardId,
                billingDayOfMonth = newBillingDay.coerceIn(1, 31),
                totalMonthlyAmount = newTotalMonthlyAmount,
                category = newCategory.trim().ifBlank { subscription.category },
                participantsSummary = summary,
                periodicity = periodicity
            )
            repository.updateSubscription(updated)

            val currentYM = _subscriptionYearMonth.value

            // Requisito 5: Solo afecta movimientos futuros o mes en curso según la confirmación del usuario
            val relatedExpenses = allExpenses.value.filter { it.subscriptionId == subscription.id }
            for (exp in relatedExpenses) {
                val expYM = exp.targetStatementMonth ?: CreditCardCalculator.extractYearMonth(exp.dateMillis)
                val shouldUpdate = if (affectCurrentMonth) (expYM >= currentYM) else (expYM > currentYM)
                if (shouldUpdate) {
                    val updatedExp = exp.copy(
                        concept = newName.trim().ifBlank { exp.concept },
                        amount = newTotalMonthlyAmount,
                        cardId = newCardId
                    )
                    repository.updateExpense(updatedExp)
                }
            }

            if (affectCurrentMonth) {
                // Actualizar trackings del mes actual
                val existingTrackings = subscriptionTrackings.value.filter { it.subscriptionId == subscription.id }
                val existingMap = existingTrackings.associateBy { it.participantName.lowercase() }
                val newTrackings = participants.map { (pName, amount) ->
                    val prev = existingMap[pName.trim().lowercase()]
                    SubscriptionPaymentTracking(
                        id = prev?.id ?: 0,
                        subscriptionId = subscription.id,
                        yearMonth = currentYM,
                        participantName = pName.trim(),
                        amountOwed = amount,
                        isPaid = prev?.isPaid ?: false,
                        paidDateMillis = prev?.paidDateMillis
                    )
                }
                repository.insertTrackings(newTrackings)
            }
        }
    }

    /**
     * Requisito 4: Las suscripciones se deben poder eliminar.
     * No afectará los registros pasados, solamente los futuros, y se pregunta si el mes actual permanece.
     */
    fun deleteSubscription(
        subscription: Subscription,
        keepCurrentMonthCharge: Boolean
    ) {
        viewModelScope.launch {
            val currentYM = _subscriptionYearMonth.value
            val relatedExpenses = allExpenses.value.filter { it.subscriptionId == subscription.id }

            for (exp in relatedExpenses) {
                val expYM = exp.targetStatementMonth ?: CreditCardCalculator.extractYearMonth(exp.dateMillis)
                if (expYM > currentYM) {
                    // Meses futuros siempre se eliminan
                    repository.deleteExpense(exp)
                } else if (expYM == currentYM && !keepCurrentMonthCharge) {
                    // Si el usuario decidió NO mantener el cargo del mes en curso, se elimina el del mes actual
                    repository.deleteExpense(exp)
                }
                // Meses pasados (expYM < currentYM) NUNCA se tocan, quedan intactos
            }

            // Eliminar la suscripción de la base de datos
            repository.deleteSubscription(subscription)
        }
    }

    fun toggleTrackingPaid(tracking: SubscriptionPaymentTracking) {
        viewModelScope.launch {
            val newPaid = !tracking.isPaid
            val updated = tracking.copy(
                isPaid = newPaid,
                paidDateMillis = if (newPaid) System.currentTimeMillis() else null
            )
            repository.updateTracking(updated)
        }
    }

    fun markAllTrackingsPaid(subscriptionId: Long) {
        viewModelScope.launch {
            val trackings = subscriptionTrackings.value.filter { it.subscriptionId == subscriptionId && !it.isPaid }
            for (t in trackings) {
                repository.updateTracking(t.copy(isPaid = true, paidDateMillis = System.currentTimeMillis()))
            }
        }
    }

    fun markAllParticipantsPaid(subscriptionId: Long) {
        markAllTrackingsPaid(subscriptionId)
    }

    fun toggleSubscriptionActive(subscription: Subscription) {
        viewModelScope.launch {
            val updated = subscription.copy(isActive = !subscription.isActive)
            repository.updateSubscription(updated)
        }
    }

    /**
     * Módulo de Gasolina: Registra una nueva carga de combustible y crea el gasto en la tarjeta seleccionada.
     */
    fun addFuelEntry(
        cardId: Long,
        kmDriven: Double,
        fuelType: String,
        pricePerLiter: Double,
        litersLoaded: Double,
        isDivided: Boolean,
        dividedWith: String,
        dividedCount: Int = 2,
        notes: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val totalCost = Math.round(litersLoaded * pricePerLiter * 100.0) / 100.0
            val efficiency = if (litersLoaded > 0.0) Math.round((kmDriven / litersLoaded) * 100.0) / 100.0 else 0.0
            val safeDividedCount = dividedCount.coerceAtLeast(2)
            val card = allCards.value.firstOrNull { it.id == cardId }
            val (cutoffDate, _) = if (card != null) CreditCardCalculator.calculateCycleDates(card, Date(dateMillis)) else Pair(Date(dateMillis), Date(dateMillis))
            val targetMonth = CreditCardCalculator.extractYearMonth(cutoffDate.time)

            // Requisito 1: Los movimientos de cargo en la tarjeta solamente deben decir "Gasolina"
            val concept = "Gasolina"
            val fullNotes = buildString {
                append("$fuelType • Rendimiento: ${String.format(java.util.Locale.US, "%.2f", efficiency)} km/l • ${String.format(java.util.Locale.US, "%.5f", litersLoaded)} L @ $${String.format(java.util.Locale.US, "%.2f", pricePerLiter)}/L")
                if (isDivided) {
                    append(" • Dividido entre $safeDividedCount personas")
                    if (dividedWith.isNotBlank()) {
                        append(" ($dividedWith)")
                    }
                }
                if (notes.isNotBlank()) {
                    append(" • $notes")
                }
            }

            // Registrar como gasto en la tarjeta seleccionada
            val expense = Expense(
                cardId = cardId,
                concept = concept,
                amount = totalCost,
                dateMillis = dateMillis,
                beneficiary = if (isDivided) "Dividido" else "Personal",
                category = "Gasolina",
                isMsi = false,
                notes = fullNotes,
                targetStatementMonth = targetMonth,
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            val expenseId = repository.insertExpense(expense)

            // Registrar la entrada de combustible.
            // Corrección: el monto personal se calcula entre el número real de personas (dividedCount),
            // en vez de asumir siempre una división 50/50 sin importar cuántas personas participen.
            val fuelEntry = FuelEntry(
                cardId = cardId,
                expenseId = expenseId,
                kmDriven = kmDriven,
                fuelType = fuelType,
                pricePerLiter = pricePerLiter,
                litersLoaded = litersLoaded,
                totalCost = totalCost,
                efficiencyKmPerL = efficiency,
                isDivided = isDivided,
                personalShare = if (isDivided) Math.round((totalCost / safeDividedCount) * 100.0) / 100.0 else totalCost,
                dividedWith = dividedWith,
                dividedCount = safeDividedCount,
                dateMillis = dateMillis,
                notes = notes,
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            repository.insertFuelEntry(fuelEntry)
        }
    }

    fun deleteFuelEntry(entry: FuelEntry) {
        viewModelScope.launch {
            // Requisito 6: Las cargas de gasolina deben poder editarse/eliminarse únicamente en los próximos 60 días
            val ageMillis = System.currentTimeMillis() - entry.dateMillis
            if (ageMillis > 60L * 24 * 60 * 60 * 1000) {
                return@launch
            }
            if (entry.expenseId != null) {
                repository.deleteExpenseById(entry.expenseId)
            }
            repository.deleteFuelEntry(entry)
        }
    }

    /**
     * Permite editar una carga de combustible y sincronizar su movimiento de cargo en la tarjeta.
     */
    fun updateFuelEntry(
        entry: FuelEntry,
        newCardId: Long,
        newKmDriven: Double,
        newFuelType: String,
        newPricePerLiter: Double,
        newLitersLoaded: Double,
        newIsDivided: Boolean,
        newDividedWith: String,
        newDividedCount: Int = 2,
        newNotes: String,
        newDateMillis: Long
    ) {
        viewModelScope.launch {
            val totalCost = Math.round(newLitersLoaded * newPricePerLiter * 100.0) / 100.0
            val efficiency = if (newLitersLoaded > 0.0) Math.round((newKmDriven / newLitersLoaded) * 100.0) / 100.0 else 0.0
            val safeDividedCount = newDividedCount.coerceAtLeast(2)
            val card = allCards.value.firstOrNull { it.id == newCardId }
            val (cutoffDate, _) = if (card != null) CreditCardCalculator.calculateCycleDates(card, Date(newDateMillis)) else Pair(Date(newDateMillis), Date(newDateMillis))
            val targetMonth = CreditCardCalculator.extractYearMonth(cutoffDate.time)

            val concept = "Gasolina"
            val fullNotes = buildString {
                append("$newFuelType • Rendimiento: ${String.format(java.util.Locale.US, "%.2f", efficiency)} km/l • ${String.format(java.util.Locale.US, "%.5f", newLitersLoaded)} L @ $${String.format(java.util.Locale.US, "%.2f", newPricePerLiter)}/L")
                if (newIsDivided) {
                    append(" • Dividido entre $safeDividedCount personas")
                    if (newDividedWith.isNotBlank()) {
                        append(" ($newDividedWith)")
                    }
                }
                if (newNotes.isNotBlank()) {
                    append(" • $newNotes")
                }
            }

            // Sincronizar o actualizar el gasto asociado.
            // Se busca primero el gasto existente y se usa .copy() para preservar su firestoreId
            // (antes se reconstruía desde cero y se perdía el identificador de sincronización).
            if (entry.expenseId != null) {
                val existingExpense = repository.getExpenseById(entry.expenseId)
                val updatedExpense = (existingExpense ?: Expense(id = entry.expenseId, cardId = newCardId, concept = concept, amount = totalCost, dateMillis = newDateMillis)).copy(
                    cardId = newCardId,
                    concept = concept,
                    amount = totalCost,
                    dateMillis = newDateMillis,
                    beneficiary = if (newIsDivided) "Dividido" else "Personal",
                    category = "Gasolina",
                    isMsi = false,
                    notes = fullNotes,
                    targetStatementMonth = targetMonth
                )
                repository.updateExpense(updatedExpense)
            }

            // Corrección: el monto personal se calcula entre el número real de personas (dividedCount).
            val updatedEntry = entry.copy(
                cardId = newCardId,
                kmDriven = newKmDriven,
                fuelType = newFuelType,
                pricePerLiter = newPricePerLiter,
                litersLoaded = newLitersLoaded,
                totalCost = totalCost,
                efficiencyKmPerL = efficiency,
                isDivided = newIsDivided,
                personalShare = if (newIsDivided) Math.round((totalCost / safeDividedCount) * 100.0) / 100.0 else totalCost,
                dividedWith = newDividedWith,
                dividedCount = safeDividedCount,
                dateMillis = newDateMillis,
                notes = newNotes
            )
            repository.updateFuelEntry(updatedEntry)
        }
    }

    /**
     * Permite editar una compra a Meses Sin Intereses (MSI).
     */
    fun updateMsiExpense(
        expense: Expense,
        newConcept: String,
        newMonthlyAmount: Double,
        newTotalPurchaseAmount: Double,
        newCardId: Long,
        newBeneficiary: String,
        newCategory: String,
        newTotalMonths: Int,
        newCurrentInstallment: Int,
        newNotes: String
    ) {
        viewModelScope.launch {
            val calculatedMonthly = if (newTotalPurchaseAmount > 0.0 && newTotalMonths > 0) {
                Math.round((newTotalPurchaseAmount / newTotalMonths) * 100.0) / 100.0
            } else {
                newMonthlyAmount
            }
            val calculatedTotal = if (newTotalPurchaseAmount > 0.0) {
                newTotalPurchaseAmount
            } else {
                newMonthlyAmount * newTotalMonths
            }

            // Corrección: editar la cuota actual o el plazo no recalculaba el mes de corte guardado
            // (targetStatementMonth), que quedaba desincronizado de la fecha real de compra —
            // la tabla de amortización podía terminar mostrando cuotas en meses anteriores a esa
            // fecha, algo imposible. Ahora se recalcula siempre a partir de la fecha de compra real
            // (expense.dateMillis, que esta pantalla no modifica) y el día de corte de la tarjeta.
            val safeCurrentInstallment = newCurrentInstallment.coerceIn(0, newTotalMonths)
            val card = allCards.value.firstOrNull { it.id == newCardId }
            val newTargetStatementMonth = if (card != null && safeCurrentInstallment >= 1) {
                CreditCardCalculator.calculateStatementMonthForInstallment(
                    purchaseDateMillis = expense.dateMillis,
                    cardCutoffDay = card.cutoffDay,
                    installmentNumber = safeCurrentInstallment
                )
            } else {
                expense.targetStatementMonth
            }

            val updated = expense.copy(
                concept = newConcept.trim().ifBlank { expense.concept },
                amount = calculatedMonthly,
                msiTotalPurchaseAmount = calculatedTotal,
                cardId = newCardId,
                beneficiary = newBeneficiary.trim().ifBlank { expense.beneficiary },
                category = newCategory.trim().ifBlank { expense.category },
                msiTotalMonths = newTotalMonths.coerceAtLeast(1),
                msiCurrentInstallment = safeCurrentInstallment,
                targetStatementMonth = newTargetStatementMonth,
                notes = newNotes
            )
            repository.updateExpense(updated)
        }
    }

    // --- Módulo de Servicios (agua, luz, gas) ---
    // Totalmente aislado de tarjetas/gastos: no crea Expense, no toca ninguna tarjeta ni estado de cuenta.

    fun addServiceEntry(
        serviceType: String,
        dateMillis: Long,
        amount: Double,
        consumption: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entry = ServiceEntry(
                serviceType = serviceType,
                dateMillis = dateMillis,
                amount = amount,
                consumption = consumption,
                notes = notes.trim(),
                firestoreId = java.util.UUID.randomUUID().toString()
            )
            repository.insertServiceEntry(entry)
        }
    }

    fun updateServiceEntry(
        entry: ServiceEntry,
        newDateMillis: Long,
        newAmount: Double,
        newConsumption: Double,
        newNotes: String
    ) {
        viewModelScope.launch {
            repository.updateServiceEntry(
                entry.copy(
                    dateMillis = newDateMillis,
                    amount = newAmount,
                    consumption = newConsumption,
                    notes = newNotes.trim()
                )
            )
        }
    }

    fun deleteServiceEntry(entry: ServiceEntry) {
        viewModelScope.launch {
            repository.deleteServiceEntry(entry)
        }
    }

    // --- Módulo de Sincronización y Cuenta Firebase ---

    fun onFirebaseUserAuthenticated(user: FirebaseAccountInfo) {
        // Actualizar perfil local con los datos de la cuenta de Google
        if (user.displayName.isNotBlank() || user.email.isNotBlank()) {
            val nameParts = user.displayName.split(" ").filter { it.isNotBlank() }
            val short = nameParts.firstOrNull() ?: _userProfile.value.shortName
            updateUserProfile(
                fullName = if (user.displayName.isNotBlank()) user.displayName else _userProfile.value.fullName,
                email = if (user.email.isNotBlank()) user.email else _userProfile.value.email,
                shortName = short
            )
        }
        cloudSyncManager.checkCurrentAuth()
    }

    fun syncDataToCloud() {
        viewModelScope.launch {
            cloudSyncManager.uploadAllLocalDataToCloud()
        }
    }

    fun restoreDataFromCloud() {
        viewModelScope.launch {
            cloudSyncManager.restoreFromCloudToLocal()
        }
    }

    fun signOutFromCloud() {
        viewModelScope.launch {
            googleAuthManager.signOut()
            cloudSyncManager.signOut()
        }
    }
}

/**
 * Lista de strings editable por el usuario (conceptos sugeridos, personas/beneficiarios, etc.) que
 * se persiste en DataStore para sobrevivir a cerrar y volver a abrir el formulario donde se usa.
 * Centraliza el patrón de guardar/cargar que antes se repetía casi idéntico para cada lista.
 *
 * [legacyPrefs]/[legacyPrefsKey] son el SharedPreferences y la clave que se usaban antes de migrar
 * a DataStore: solo se leen como valor inicial síncrono (para no mostrar los valores por defecto un
 * instante mientras la migración automática de DataStore termina), nunca se vuelven a escribir ahí.
 */
private class PersistedStringListPref(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>,
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val key: androidx.datastore.preferences.core.Preferences.Key<String>,
    legacyPrefs: android.content.SharedPreferences,
    legacyPrefsKey: String,
    default: List<String>
) {
    private val legacyValue = parse(legacyPrefs.getString(legacyPrefsKey, null), default)

    val state: StateFlow<List<String>> = dataStore.data
        .map { parse(it[key], legacyValue) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), legacyValue)

    fun update(list: List<String>) {
        val clean = list.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        scope.launch {
            dataStore.edit { it[key] = clean.joinToString("||") }
        }
    }

    private fun parse(raw: String?, default: List<String>): List<String> {
        val list = raw?.split("||")?.map { it.trim() }?.filter { it.isNotBlank() } ?: return default
        return list.ifEmpty { default }
    }
}
