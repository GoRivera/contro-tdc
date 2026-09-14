package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY billingDayOfMonth ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY billingDayOfMonth ASC")
    fun getActiveSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): Subscription?

    @Query("SELECT * FROM subscriptions WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getSubscriptionByFirestoreId(firestoreId: String): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription): Long

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Long)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAllSubscriptions()

    @Query("DELETE FROM subscription_payment_trackings")
    suspend fun deleteAllTrackings()

    // Tracking queries
    @Query("SELECT * FROM subscription_payment_trackings WHERE yearMonth = :yearMonth")
    fun getTrackingsForMonth(yearMonth: String): Flow<List<SubscriptionPaymentTracking>>

    @Query("SELECT * FROM subscription_payment_trackings WHERE subscriptionId = :subId")
    fun getTrackingsForSubscription(subId: Long): Flow<List<SubscriptionPaymentTracking>>

    @Query("SELECT * FROM subscription_payment_trackings WHERE subscriptionId = :subId AND yearMonth = :yearMonth")
    suspend fun getTrackingsForSubscriptionAndMonth(subId: Long, yearMonth: String): List<SubscriptionPaymentTracking>

    @Query("SELECT * FROM subscription_payment_trackings")
    fun getAllTrackings(): Flow<List<SubscriptionPaymentTracking>>

    @Query("SELECT * FROM subscription_payment_trackings WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getTrackingByFirestoreId(firestoreId: String): SubscriptionPaymentTracking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackings(trackings: List<SubscriptionPaymentTracking>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracking(tracking: SubscriptionPaymentTracking): Long

    @Update
    suspend fun updateTracking(tracking: SubscriptionPaymentTracking)

    @Query("DELETE FROM subscription_payment_trackings WHERE subscriptionId = :subId")
    suspend fun deleteTrackingsForSubscription(subId: Long)
}
