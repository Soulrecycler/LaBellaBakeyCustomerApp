package com.bakery.customer.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(entities = [CartLineEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}

// Room's KSP processor generates the platform `actual` for this at build time.
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

/** Platform-specific factory for the builder; Android needs a Context, iOS doesn't. */
expect class DatabaseBuilderFactory {
    fun create(): RoomDatabase.Builder<AppDatabase>
}

fun DatabaseBuilderFactory.build(): AppDatabase =
    create()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
