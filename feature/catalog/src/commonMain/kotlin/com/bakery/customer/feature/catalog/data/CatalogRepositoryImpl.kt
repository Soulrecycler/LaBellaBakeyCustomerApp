package com.bakery.customer.feature.catalog.data

import com.bakery.customer.feature.catalog.domain.CatalogRepository
import com.bakery.customer.feature.catalog.domain.HomeData
import com.bakery.customer.feature.catalog.domain.Item

class CatalogRepositoryImpl(
    private val dataSource: CatalogDataSource,
) : CatalogRepository {
    override suspend fun getHome(): Result<HomeData> = runCatching { dataSource.getHome() }

    override suspend fun getItems(search: String?, type: String?): Result<List<Item>> =
        runCatching { dataSource.getItems(search, type) }
}
