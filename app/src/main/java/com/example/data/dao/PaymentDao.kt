package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE cardId = :cardId ORDER BY dateMillis DESC")
    fun getPaymentsByCard(cardId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE targetStatementMonth = :statementMonth ORDER BY dateMillis DESC")
    fun getPaymentsByStatementMonth(statementMonth: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE targetStatementMonth = :statementMonth AND cardId = :cardId ORDER BY dateMillis DESC")
    fun getPaymentsByStatementAndCard(statementMonth: String, cardId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<Payment>)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}
