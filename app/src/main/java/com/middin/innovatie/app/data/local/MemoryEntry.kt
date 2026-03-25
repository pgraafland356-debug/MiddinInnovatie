package com.middin.innovatie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "collective_memory")
data class MemoryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val authorName: String,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
)
