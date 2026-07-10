package com.bakery.customer.app

import android.app.Application
import com.bakery.customer.app.di.initKoin
import com.bakery.customer.core.database.DatabaseBuilderFactory
import com.bakery.customer.core.database.SessionStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module

class BakeryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val androidModule = module {
            single { DatabaseBuilderFactory(get()) }
            single { SessionStoreFactory(get()) }
        }
        initKoin(platformModule = androidModule) {
            androidContext(this@BakeryApp)
            androidLogger()
        }
    }
}
