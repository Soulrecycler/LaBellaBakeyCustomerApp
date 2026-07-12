package com.bakery.customer.app.di

import com.bakery.customer.feature.auth.di.authModule
import com.bakery.customer.feature.catalog.di.catalogModule
import com.bakery.customer.feature.home.di.homeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(platformModule: Module, config: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule, authModule, catalogModule, homeModule)
    }
}
