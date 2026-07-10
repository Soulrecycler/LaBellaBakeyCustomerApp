package com.bakery.customer.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CartLineEntity(
    @PrimaryKey val itemId: String,
    val quantity: Int,
)
