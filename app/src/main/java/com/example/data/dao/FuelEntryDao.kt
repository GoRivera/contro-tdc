package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries ORDER BY dateMillis DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE dateMillis >= :sinceMillis ORDER BY dateMillis DESC")
    fun getFuelEntriesSince(sinceMillis: Long): Flow<List<FuelEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelEntry(entry: FuelEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFuelEntries(entries: List<FuelEntry>)

    @Update
    suspend fun updateFuelEntry(entry: FuelEntry)

    @Delete
    suspend fun deleteFuelEntry(entry: FuelEntry)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteFuelEntryById(id: Long)
}
