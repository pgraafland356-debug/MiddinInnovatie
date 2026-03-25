package com.middin.innovatie.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Long

    @Query("SELECT * FROM products ORDER BY createdAtEpochMs DESC LIMIT 3")
    fun observeTop3(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE products SET description = :description WHERE name = :name AND description = ''")
    suspend fun updateDescriptionIfEmpty(name: String, description: String)
}
