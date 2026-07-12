package com.bakery.customer.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM CartLineEntity")
    fun observeAll(): Flow<List<CartLineEntity>>

    @Upsert
    suspend fun upsert(line: CartLineEntity)

    @Delete
    suspend fun delete(line: CartLineEntity)
}
