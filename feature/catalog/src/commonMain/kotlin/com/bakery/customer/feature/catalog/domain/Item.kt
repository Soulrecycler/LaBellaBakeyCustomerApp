package com.bakery.customer.feature.catalog.domain

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val type: String,
)

data class Offer(
    val id: String,
    val title: String,
    val description: String,
    val discountPercent: Int,
)

data class HomeData(
    val itemOfTheDay: List<Item> = emptyList(),
    val offersOfTheDay: List<Offer> = emptyList(),
    val items: List<Item> = emptyList(),
)
