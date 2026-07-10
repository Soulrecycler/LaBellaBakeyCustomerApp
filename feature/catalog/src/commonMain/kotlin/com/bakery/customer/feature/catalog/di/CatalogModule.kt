package com.bakery.customer.feature.catalog.di

import com.bakery.customer.feature.catalog.data.CatalogDataSource
import com.bakery.customer.feature.catalog.data.CatalogRepositoryImpl
import com.bakery.customer.feature.catalog.data.FakeCatalogDataSource
import com.bakery.customer.feature.catalog.domain.CatalogRepository
import org.koin.dsl.module

val catalogModule = module {
    single<CatalogDataSource> { FakeCatalogDataSource() }
    single<CatalogRepository> { CatalogRepositoryImpl(get()) }
}
