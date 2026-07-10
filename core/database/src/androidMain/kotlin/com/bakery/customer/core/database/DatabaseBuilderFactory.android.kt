package com.bakery.customer.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseBuilderFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<AppDatabase> {
        val dbFile = context.getDatabasePath("bakery.db")
        return Room.databaseBuilder<AppDatabase>(context = context, name = dbFile.absolutePath)
    }
}
