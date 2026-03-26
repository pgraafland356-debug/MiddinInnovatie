package com.middin.innovatie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_chat_messages")
data class LocalChatMessage(
    @PrimaryKey val id: String,
    val text: String,
    val authorName: String,
    val createdAtEpochMs: Long,
)
