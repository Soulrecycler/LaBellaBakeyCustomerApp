package com.bakery.customer.feature.catalog.domain

interface CatalogRepository {
    suspend fun getHome(): Result<HomeData>
    suspend fun getItems(search: String? = null, type: String? = null): Result<List<Item>>
}
