package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CreditCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY name ASC")
    fun getAllCards(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCards(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): CreditCard?

    @Query("SELECT * FROM credit_cards WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getCardByFirestoreId(firestoreId: String): CreditCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCards(cards: List<CreditCard>)

    @Update
    suspend fun updateCard(card: CreditCard)

    @Delete
    suspend fun deleteCard(card: CreditCard)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)
}
