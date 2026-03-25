package com.middin.innovatie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM collective_memory ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<MemoryEntry>>

    @Insert
    suspend fun insert(entry: MemoryEntry)
}
