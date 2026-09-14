package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ServiceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceEntryDao {
    @Query("SELECT * FROM service_entries ORDER BY dateMillis DESC")
    fun getAllServiceEntries(): Flow<List<ServiceEntry>>

    @Query("SELECT * FROM service_entries WHERE serviceType = :serviceType ORDER BY dateMillis DESC")
    fun getServiceEntriesByType(serviceType: String): Flow<List<ServiceEntry>>

    @Query("SELECT * FROM service_entries WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getServiceEntryByFirestoreId(firestoreId: String): ServiceEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceEntry(entry: ServiceEntry): Long

    @Update
    suspend fun updateServiceEntry(entry: ServiceEntry)

    @Delete
    suspend fun deleteServiceEntry(entry: ServiceEntry)

    @Query("DELETE FROM service_entries WHERE id = :id")
    suspend fun deleteServiceEntryById(id: Long)

    @Query("DELETE FROM service_entries")
    suspend fun deleteAllServiceEntries()
}
