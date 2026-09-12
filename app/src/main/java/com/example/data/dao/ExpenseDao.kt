package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE cardId = :cardId ORDER BY dateMillis DESC")
    fun getExpensesByCard(cardId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isMsi = 1 ORDER BY concept ASC")
    fun getAllMsiExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE targetStatementMonth = :statementMonth ORDER BY dateMillis DESC")
    fun getExpensesByStatementMonth(statementMonth: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE targetStatementMonth = :statementMonth AND cardId = :cardId ORDER BY dateMillis DESC")
    fun getExpensesByStatementAndCard(statementMonth: String, cardId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getExpenseByFirestoreId(firestoreId: String): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<Expense>)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}
