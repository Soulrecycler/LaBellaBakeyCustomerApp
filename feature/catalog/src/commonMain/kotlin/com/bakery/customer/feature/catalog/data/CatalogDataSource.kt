package com.bakery.customer.feature.catalog.data

import com.bakery.customer.feature.catalog.domain.HomeData
import com.bakery.customer.feature.catalog.domain.Item

interface CatalogDataSource {
    suspend fun getHome(): HomeData
    suspend fun getItems(search: String?, type: String?): List<Item>
}

/** Hardcoded data source used until the real server integration lands. */
class FakeCatalogDataSource : CatalogDataSource {
    private val items = listOf(
        Item("1", "Sourdough Loaf", "Slow-fermented sourdough.", 220.0, "", "bread"),
        Item("2", "Chocolate Croissant", "Buttery, chocolate-filled.", 90.0, "", "pastry"),
        Item("3", "Red Velvet Slice", "Cream-cheese frosted.", 150.0, "", "cake"),
    )

    override suspend fun getHome(): HomeData = HomeData(
        itemOfTheDay = items.take(1),
        offersOfTheDay = emptyList(),
        items = items,
    )

    override suspend fun getItems(search: String?, type: String?): List<Item> = items.filter { item ->
        (search.isNullOrBlank() || item.name.contains(search, ignoreCase = true)) &&
            (type.isNullOrBlank() || item.type == type)
    }
}
