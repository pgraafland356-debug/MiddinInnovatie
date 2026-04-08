package com.middin.innovatie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocalChatMessageDao {
    @Query("SELECT * FROM local_chat_messages ORDER BY createdAtEpochMs ASC, id ASC")
    suspend fun getAllOrdered(): List<LocalChatMessage>

    @Insert
    suspend fun insert(message: LocalChatMessage)

    @Query("SELECT COUNT(*) FROM local_chat_messages")
    suspend fun count(): Long

    @Query("DELETE FROM local_chat_messages")
    suspend fun deleteAll()
}
