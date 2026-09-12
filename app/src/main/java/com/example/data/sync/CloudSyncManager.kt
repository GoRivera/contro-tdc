package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.FuelEntry
import com.example.data.model.Payment
import com.example.data.model.ServiceEntry
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import com.example.data.repository.CardRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Gestor central de sincronización en la nube mediante Cloud Firestore (Spark Plan 100% Gratuito).
 * Mantiene la base de datos local (Room) como fuente primaria y sincroniza con Firestore bajo /users/{uid}/...
 *
 * Todas las entidades se identifican en la nube por un `firestoreId` (UUID) estable, generado una sola vez
 * al crear el registro localmente. Esto evita que dos dispositivos distintos, cada uno con su propia
 * numeración autoincremental de Room (1, 2, 3...), terminen sobrescribiéndose entre sí al sincronizar:
 * el emparejamiento entre nube y base de datos local siempre se hace por `firestoreId`, nunca por el
 * id numérico local. Las relaciones entre entidades (tarjeta de un gasto, suscripción de un tracking, etc.)
 * también viajan a la nube como el `firestoreId` de la entidad relacionada, para poder remapearlas
 * correctamente sin importar qué id numérico les asigne Room en el dispositivo de destino.
 */
class CloudSyncManager(
    private val context: Context,
    private val repository: CardRepository
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseAccountInfo?>(null)
    val currentUser: StateFlow<FirebaseAccountInfo?> = _currentUser.asStateFlow()

    val isFirebaseInitialized: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    init {
        checkCurrentAuth()
    }

    fun checkCurrentAuth() {
        if (!isFirebaseInitialized) {
            _currentUser.value = null
            _syncState.value = SyncState.FirebaseNotConfigured
            return
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                _currentUser.value = FirebaseAccountInfo(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Usuario",
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
            } else {
                _currentUser.value = null
                _syncState.value = SyncState.Idle
            }
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "Error checking Firebase Auth state", e)
            _currentUser.value = null
            _syncState.value = SyncState.FirebaseNotConfigured
        }
    }

    // --- Helpers para garantizar firestoreId estable en registros creados antes de esta versión ---

    private suspend fun ensureCardFirestoreId(card: CreditCard): CreditCard {
        if (card.firestoreId.isNotBlank()) return card
        val updated = card.copy(firestoreId = UUID.randomUUID().toString())
        repository.updateCard(updated)
        return updated
    }

    private suspend fun ensureExpenseFirestoreId(expense: Expense): Expense {
        if (expense.firestoreId.isNotBlank()) return expense
        val updated = expense.copy(firestoreId = UUID.randomUUID().toString())
        repository.updateExpense(updated)
        return updated
    }

    private suspend fun ensurePaymentFirestoreId(payment: Payment): Payment {
        if (payment.firestoreId.isNotBlank()) return payment
        val updated = payment.copy(firestoreId = UUID.randomUUID().toString())
        repository.updatePayment(updated)
        return updated
    }

    private suspend fun ensureSubscriptionFirestoreId(subscription: Subscription): Subscription {
        if (subscription.firestoreId.isNotBlank()) return subscription
        val updated = subscription.copy(firestoreId = UUID.randomUUID().toString())
        repository.updateSubscription(updated)
        return updated
    }

    private suspend fun ensureTrackingFirestoreId(tracking: SubscriptionPaymentTracking): SubscriptionPaymentTracking {
        if (tracking.firestoreId.isNotBlank()) return tracking
        val updated = tracking.copy(firestoreId = UUID.randomUUID().toString())
        repository.updateTracking(updated)
        return updated
    }

    private suspend fun ensureFuelEntryFirestoreId(entry: FuelEntry): FuelEntry {
        if (entry.firestoreId.isNotBlank()) return entry
        val updated = entry.copy(firestoreId = UUID.randomUUID().toString())
        repository.updateFuelEntry(updated)
        return updated
    }

    private suspend fun ensureServiceEntryFirestoreId(entry: ServiceEntry): ServiceEntry {
        if (entry.firestoreId.isNotBlank()) return entry
        val updated = entry.copy(firestoreId = UUID.randomUUID().toString())
        repository.updateServiceEntry(updated)
        return updated
    }

    /**
     * Sube todos los registros locales existentes a Firestore bajo la cuenta autenticada.
     */
    suspend fun uploadAllLocalDataToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized) {
            _syncState.value = SyncState.FirebaseNotConfigured
            return@withContext Result.failure(IllegalStateException("Firebase no está configurado aún."))
        }

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            _syncState.value = SyncState.Error("No hay sesión activa para sincronizar.")
            return@withContext Result.failure(IllegalStateException("Usuario no autenticado"))
        }

        _syncState.value = SyncState.Syncing

        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(user.uid)

            // 1. Obtener datos locales y garantizar que todos tengan un firestoreId estable
            val cards = repository.allCards.first().map { ensureCardFirestoreId(it) }
            val cardFidByLocalId = cards.associate { it.id to it.firestoreId }

            val subscriptions = repository.allSubscriptions.first().map { ensureSubscriptionFirestoreId(it) }
            val subFidByLocalId = subscriptions.associate { it.id to it.firestoreId }

            val expenses = repository.allExpenses.first().map { ensureExpenseFirestoreId(it) }
            val expenseFidByLocalId = expenses.associate { it.id to it.firestoreId }

            val payments = repository.allPayments.first().map { ensurePaymentFirestoreId(it) }
            val trackings = repository.allTrackings.first().map { ensureTrackingFirestoreId(it) }
            val fuelEntries = repository.allFuelEntries.first().map { ensureFuelEntryFirestoreId(it) }
            val serviceEntries = repository.allServiceEntries.first().map { ensureServiceEntryFirestoreId(it) }

            val batch = firestore.batch()

            for (card in cards) {
                val doc = userDocRef.collection("cards").document(card.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to card.firestoreId,
                    "name" to card.name,
                    "bank" to card.bank,
                    "cutoffDay" to card.cutoffDay,
                    "paymentDueDay" to card.paymentDueDay,
                    "creditLimit" to card.creditLimit,
                    "primaryColorHex" to card.primaryColorHex,
                    "secondaryColorHex" to card.secondaryColorHex,
                    "last4Digits" to card.last4Digits,
                    "network" to card.network,
                    "isActive" to card.isActive,
                    "isDepartmental" to card.isDepartmental,
                    "graceDays" to card.graceDays,
                    "cardholderName" to card.cardholderName,
                    "annualInterestRatePercent" to card.annualInterestRatePercent
                )
                batch.set(doc, map, SetOptions.merge())
            }

            for (sub in subscriptions) {
                val doc = userDocRef.collection("subscriptions").document(sub.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to sub.firestoreId,
                    "name" to sub.name,
                    "cardFirestoreId" to (cardFidByLocalId[sub.cardId] ?: ""),
                    "billingDayOfMonth" to sub.billingDayOfMonth,
                    "totalMonthlyAmount" to sub.totalMonthlyAmount,
                    "category" to sub.category,
                    "startMonth" to sub.startMonth,
                    "isActive" to sub.isActive,
                    "notes" to sub.notes,
                    "participantsSummary" to sub.participantsSummary,
                    "periodicity" to sub.periodicity
                )
                batch.set(doc, map, SetOptions.merge())
            }

            // Subir gastos
            for (exp in expenses) {
                val doc = userDocRef.collection("expenses").document(exp.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to exp.firestoreId,
                    "cardFirestoreId" to (cardFidByLocalId[exp.cardId] ?: ""),
                    "concept" to exp.concept,
                    "amount" to exp.amount,
                    "dateMillis" to exp.dateMillis,
                    "beneficiary" to exp.beneficiary,
                    "category" to exp.category,
                    "isMsi" to exp.isMsi,
                    "msiTotalMonths" to exp.msiTotalMonths,
                    "msiCurrentInstallment" to exp.msiCurrentInstallment,
                    "msiTotalPurchaseAmount" to exp.msiTotalPurchaseAmount,
                    "notes" to exp.notes,
                    "targetStatementMonth" to exp.targetStatementMonth,
                    "isSubscription" to exp.isSubscription,
                    "subscriptionFirestoreId" to (exp.subscriptionId?.let { subFidByLocalId[it] } ?: "")
                )
                batch.set(doc, map, SetOptions.merge())
            }

            // Subir pagos
            for (pay in payments) {
                val doc = userDocRef.collection("payments").document(pay.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to pay.firestoreId,
                    "cardFirestoreId" to (cardFidByLocalId[pay.cardId] ?: ""),
                    "concept" to pay.concept,
                    "amount" to pay.amount,
                    "dateMillis" to pay.dateMillis,
                    "sourcePayer" to pay.sourcePayer,
                    "targetStatementMonth" to pay.targetStatementMonth,
                    "notes" to pay.notes
                )
                batch.set(doc, map, SetOptions.merge())
            }

            // Subir seguimiento de pagos de suscripciones (quién ya pagó cada mes)
            for (t in trackings) {
                val doc = userDocRef.collection("subscriptionTrackings").document(t.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to t.firestoreId,
                    "subscriptionFirestoreId" to (subFidByLocalId[t.subscriptionId] ?: ""),
                    "yearMonth" to t.yearMonth,
                    "participantName" to t.participantName,
                    "amountOwed" to t.amountOwed,
                    "isPaid" to t.isPaid,
                    "paidDateMillis" to (t.paidDateMillis ?: 0L)
                )
                batch.set(doc, map, SetOptions.merge())
            }

            // Subir gasolina
            for (fuel in fuelEntries) {
                val doc = userDocRef.collection("fuel").document(fuel.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to fuel.firestoreId,
                    "cardFirestoreId" to (cardFidByLocalId[fuel.cardId] ?: ""),
                    "expenseFirestoreId" to (fuel.expenseId?.let { expenseFidByLocalId[it] } ?: ""),
                    "kmDriven" to fuel.kmDriven,
                    "fuelType" to fuel.fuelType,
                    "pricePerLiter" to fuel.pricePerLiter,
                    "litersLoaded" to fuel.litersLoaded,
                    "totalCost" to fuel.totalCost,
                    "efficiencyKmPerL" to fuel.efficiencyKmPerL,
                    "isDivided" to fuel.isDivided,
                    "personalShare" to fuel.personalShare,
                    "dividedWith" to fuel.dividedWith,
                    "dividedCount" to fuel.dividedCount,
                    "dateMillis" to fuel.dateMillis,
                    "notes" to fuel.notes
                )
                batch.set(doc, map, SetOptions.merge())
            }

            // Subir servicios (agua, luz, gas) — independiente de tarjetas/gastos
            for (service in serviceEntries) {
                val doc = userDocRef.collection("services").document(service.firestoreId)
                val map = hashMapOf(
                    "firestoreId" to service.firestoreId,
                    "serviceType" to service.serviceType,
                    "dateMillis" to service.dateMillis,
                    "amount" to service.amount,
                    "consumption" to service.consumption,
                    "notes" to service.notes
                )
                batch.set(doc, map, SetOptions.merge())
            }

            // Guardar metadata de sincronización
            batch.set(
                userDocRef,
                hashMapOf(
                    "lastSyncedAt" to System.currentTimeMillis(),
                    "email" to (user.email ?: ""),
                    "displayName" to (user.displayName ?: "")
                ),
                SetOptions.merge()
            )

            batch.commit().await()
            _syncState.value = SyncState.Success("Sincronización a la nube completada con éxito.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "Error al sincronizar con Firestore", e)
            _syncState.value = SyncState.Error("Fallo en sincronización: ${e.localizedMessage ?: "Error desconocido"}")
            Result.failure(e)
        }
    }

    /**
     * Descarga y restaura los datos de la nube hacia la base de datos local, incluyendo tarjetas, gastos,
     * pagos/abonos, suscripciones, su seguimiento mensual de pagos, cargas de gasolina y servicios
     * (agua/luz/gas) — las siete colecciones que se suben en [uploadAllLocalDataToCloud]. El emparejamiento con registros locales
     * existentes se hace por `firestoreId`, así que restaurar es seguro incluso en un dispositivo que ya
     * tenga datos propios: nunca sobrescribe un registro local que no corresponda al mismo `firestoreId`.
     */
    suspend fun restoreFromCloudToLocal(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized) {
            _syncState.value = SyncState.FirebaseNotConfigured
            return@withContext Result.failure(IllegalStateException("Firebase no está configurado."))
        }

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            _syncState.value = SyncState.Error("No hay usuario autenticado.")
            return@withContext Result.failure(IllegalStateException("Usuario no autenticado"))
        }

        _syncState.value = SyncState.Syncing

        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(user.uid)

            // 1. Restaurar tarjetas primero: son la base de la que dependen gastos, pagos, suscripciones y gasolina
            val cardLocalIdByFid = mutableMapOf<String, Long>()
            val cardsSnap = userDocRef.collection("cards").get().await()
            for (doc in cardsSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val existing = repository.getCardByFirestoreId(fid)
                val card = CreditCard(
                    id = existing?.id ?: 0L,
                    firestoreId = fid,
                    name = data["name"] as? String ?: "Tarjeta",
                    bank = data["bank"] as? String ?: "Banco",
                    cutoffDay = (data["cutoffDay"] as? Number)?.toInt() ?: 1,
                    paymentDueDay = (data["paymentDueDay"] as? Number)?.toInt() ?: 20,
                    creditLimit = (data["creditLimit"] as? Number)?.toDouble() ?: 0.0,
                    primaryColorHex = (data["primaryColorHex"] as? Number)?.toLong() ?: 0xFF004481,
                    secondaryColorHex = (data["secondaryColorHex"] as? Number)?.toLong() ?: 0xFF001E36,
                    last4Digits = data["last4Digits"] as? String ?: "••••",
                    network = data["network"] as? String ?: "Mastercard",
                    isActive = data["isActive"] as? Boolean ?: true,
                    isDepartmental = data["isDepartmental"] as? Boolean ?: false,
                    graceDays = (data["graceDays"] as? Number)?.toInt() ?: 20,
                    cardholderName = data["cardholderName"] as? String ?: "Titular",
                    annualInterestRatePercent = (data["annualInterestRatePercent"] as? Number)?.toDouble() ?: 55.0
                )
                val localId = if (existing != null) {
                    repository.updateCard(card)
                    existing.id
                } else {
                    repository.insertCard(card)
                }
                cardLocalIdByFid[fid] = localId
            }

            // 2. Restaurar suscripciones (antes de gastos y de su seguimiento de pagos, que dependen de ellas)
            val subLocalIdByFid = mutableMapOf<String, Long>()
            val subsSnap = userDocRef.collection("subscriptions").get().await()
            for (doc in subsSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val cardFid = data["cardFirestoreId"] as? String ?: ""
                val localCardId = cardLocalIdByFid[cardFid] ?: 0L
                val existing = repository.getSubscriptionByFirestoreId(fid)
                val sub = Subscription(
                    id = existing?.id ?: 0L,
                    firestoreId = fid,
                    name = data["name"] as? String ?: "Suscripción",
                    cardId = localCardId,
                    billingDayOfMonth = (data["billingDayOfMonth"] as? Number)?.toInt() ?: 1,
                    totalMonthlyAmount = (data["totalMonthlyAmount"] as? Number)?.toDouble() ?: 0.0,
                    category = data["category"] as? String ?: "Servicios",
                    startMonth = data["startMonth"] as? String ?: "2026-01",
                    isActive = data["isActive"] as? Boolean ?: true,
                    notes = data["notes"] as? String ?: "",
                    participantsSummary = data["participantsSummary"] as? String ?: "",
                    periodicity = data["periodicity"] as? String ?: "MENSUAL"
                )
                val localId = if (existing != null) {
                    repository.updateSubscription(sub)
                    existing.id
                } else {
                    repository.insertSubscription(sub)
                }
                subLocalIdByFid[fid] = localId
            }

            // 3. Restaurar gastos
            val expenseLocalIdByFid = mutableMapOf<String, Long>()
            val expensesSnap = userDocRef.collection("expenses").get().await()
            for (doc in expensesSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val cardFid = data["cardFirestoreId"] as? String ?: ""
                val localCardId = cardLocalIdByFid[cardFid] ?: continue
                val subFid = data["subscriptionFirestoreId"] as? String ?: ""
                val localSubId = if (subFid.isNotBlank()) subLocalIdByFid[subFid] else null
                val existing = repository.getExpenseByFirestoreId(fid)
                val expense = Expense(
                    id = existing?.id ?: 0L,
                    firestoreId = fid,
                    cardId = localCardId,
                    concept = data["concept"] as? String ?: "Gasto",
                    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                    dateMillis = (data["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    beneficiary = data["beneficiary"] as? String ?: "Personal",
                    category = data["category"] as? String ?: "General",
                    isMsi = data["isMsi"] as? Boolean ?: false,
                    msiTotalMonths = (data["msiTotalMonths"] as? Number)?.toInt() ?: 1,
                    msiCurrentInstallment = (data["msiCurrentInstallment"] as? Number)?.toInt() ?: 1,
                    msiTotalPurchaseAmount = (data["msiTotalPurchaseAmount"] as? Number)?.toDouble() ?: 0.0,
                    notes = data["notes"] as? String ?: "",
                    targetStatementMonth = data["targetStatementMonth"] as? String ?: "",
                    isSubscription = data["isSubscription"] as? Boolean ?: false,
                    subscriptionId = localSubId
                )
                val localId = if (existing != null) {
                    repository.updateExpense(expense)
                    existing.id
                } else {
                    repository.insertExpense(expense)
                }
                expenseLocalIdByFid[fid] = localId
            }

            // 4. Restaurar pagos/abonos
            val paymentsSnap = userDocRef.collection("payments").get().await()
            for (doc in paymentsSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val cardFid = data["cardFirestoreId"] as? String ?: ""
                val localCardId = cardLocalIdByFid[cardFid] ?: continue
                val existing = repository.getPaymentByFirestoreId(fid)
                val payment = Payment(
                    id = existing?.id ?: 0L,
                    cardId = localCardId,
                    concept = data["concept"] as? String ?: "Pago",
                    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                    dateMillis = (data["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    sourcePayer = data["sourcePayer"] as? String ?: "Personal",
                    targetStatementMonth = data["targetStatementMonth"] as? String ?: "",
                    notes = data["notes"] as? String ?: "",
                    firestoreId = fid
                )
                if (existing != null) repository.updatePayment(payment) else repository.insertPayment(payment)
            }

            // 5. Restaurar seguimiento de pagos de suscripciones (quién ya pagó cada mes)
            val trackingsSnap = userDocRef.collection("subscriptionTrackings").get().await()
            for (doc in trackingsSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val subFid = data["subscriptionFirestoreId"] as? String ?: ""
                val localSubId = subLocalIdByFid[subFid] ?: continue
                val existing = repository.getTrackingByFirestoreId(fid)
                val paidMillis = (data["paidDateMillis"] as? Number)?.toLong() ?: 0L
                val tracking = SubscriptionPaymentTracking(
                    id = existing?.id ?: 0L,
                    subscriptionId = localSubId,
                    yearMonth = data["yearMonth"] as? String ?: "",
                    participantName = data["participantName"] as? String ?: "",
                    amountOwed = (data["amountOwed"] as? Number)?.toDouble() ?: 0.0,
                    isPaid = data["isPaid"] as? Boolean ?: false,
                    paidDateMillis = if (paidMillis > 0L) paidMillis else null,
                    firestoreId = fid
                )
                if (existing != null) repository.updateTracking(tracking) else repository.insertTracking(tracking)
            }

            // 6. Restaurar cargas de combustible
            val fuelSnap = userDocRef.collection("fuel").get().await()
            for (doc in fuelSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val cardFid = data["cardFirestoreId"] as? String ?: ""
                val localCardId = cardLocalIdByFid[cardFid] ?: continue
                val expenseFid = data["expenseFirestoreId"] as? String ?: ""
                val localExpenseId = if (expenseFid.isNotBlank()) expenseLocalIdByFid[expenseFid] else null
                val existing = repository.getFuelEntryByFirestoreId(fid)
                val entry = FuelEntry(
                    id = existing?.id ?: 0L,
                    cardId = localCardId,
                    expenseId = localExpenseId,
                    kmDriven = (data["kmDriven"] as? Number)?.toDouble() ?: 0.0,
                    fuelType = data["fuelType"] as? String ?: "",
                    pricePerLiter = (data["pricePerLiter"] as? Number)?.toDouble() ?: 0.0,
                    litersLoaded = (data["litersLoaded"] as? Number)?.toDouble() ?: 0.0,
                    totalCost = (data["totalCost"] as? Number)?.toDouble() ?: 0.0,
                    efficiencyKmPerL = (data["efficiencyKmPerL"] as? Number)?.toDouble() ?: 0.0,
                    isDivided = data["isDivided"] as? Boolean ?: false,
                    personalShare = (data["personalShare"] as? Number)?.toDouble() ?: 0.0,
                    dividedWith = data["dividedWith"] as? String ?: "",
                    dividedCount = (data["dividedCount"] as? Number)?.toInt() ?: 2,
                    dateMillis = (data["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    notes = data["notes"] as? String ?: "",
                    firestoreId = fid
                )
                if (existing != null) repository.updateFuelEntry(entry) else repository.insertFuelEntry(entry)
            }

            // 7. Restaurar servicios (agua, luz, gas)
            val servicesSnap = userDocRef.collection("services").get().await()
            for (doc in servicesSnap.documents) {
                val data = doc.data ?: continue
                val fid = (data["firestoreId"] as? String)?.ifBlank { doc.id } ?: doc.id
                val existing = repository.getServiceEntryByFirestoreId(fid)
                val entry = ServiceEntry(
                    id = existing?.id ?: 0L,
                    serviceType = data["serviceType"] as? String ?: com.example.data.model.ServiceType.AGUA,
                    dateMillis = (data["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                    consumption = (data["consumption"] as? Number)?.toDouble() ?: 0.0,
                    notes = data["notes"] as? String ?: "",
                    firestoreId = fid
                )
                if (existing != null) repository.updateServiceEntry(entry) else repository.insertServiceEntry(entry)
            }

            _syncState.value = SyncState.Success("Datos restaurados exitosamente desde la nube.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "Error al restaurar desde Firestore", e)
            _syncState.value = SyncState.Error("Fallo al restaurar: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            if (isFirebaseInitialized) {
                FirebaseAuth.getInstance().signOut()
            }
            _currentUser.value = null
            _syncState.value = SyncState.Idle
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "Error signing out", e)
        }
    }
}
