package com.middin.innovatie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY createdAtEpochMs DESC LIMIT 3")
    fun observeTop3(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product)
}
