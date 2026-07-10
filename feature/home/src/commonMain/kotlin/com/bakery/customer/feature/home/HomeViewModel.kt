package com.bakery.customer.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakery.customer.core.common.AppError
import com.bakery.customer.feature.catalog.domain.CatalogRepository
import com.bakery.customer.feature.catalog.domain.Item
import com.bakery.customer.feature.catalog.domain.Offer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val itemOfTheDay: List<Item> = emptyList(),
    val offersOfTheDay: List<Offer> = emptyList(),
    val items: List<Item> = emptyList(),
    val error: AppError? = null,
)

class HomeViewModel(private val repository: CatalogRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getHome()
                .onSuccess { data ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            itemOfTheDay = data.itemOfTheDay,
                            offersOfTheDay = data.offersOfTheDay,
                            items = data.items,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = AppError.Unknown(e.message ?: "Unknown error")) }
                }
        }
    }
}
