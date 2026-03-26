package com.middin.innovatie.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MemoryEntry::class, Product::class, LocalChatMessage::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun productDao(): ProductDao
    abstract fun localChatMessageDao(): LocalChatMessageDao
}
