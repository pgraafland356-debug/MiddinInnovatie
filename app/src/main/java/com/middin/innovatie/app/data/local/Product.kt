package com.middin.innovatie.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    /** `file://` or `content://` from camera/gallery. */
    val imageUri: String?,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
)
