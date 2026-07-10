package com.bakery.customer.feature.auth.di

import com.bakery.customer.core.database.SessionStore
import com.bakery.customer.core.database.SessionStoreFactory
import com.bakery.customer.feature.auth.data.AuthRepositoryImpl
import com.bakery.customer.feature.auth.domain.AuthRepository
import org.koin.dsl.module

val authModule = module {
    single { SessionStore(get<SessionStoreFactory>().create()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
