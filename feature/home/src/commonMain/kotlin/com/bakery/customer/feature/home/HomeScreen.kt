package com.bakery.customer.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakery.customer.core.ui.EmptyState
import com.bakery.customer.core.ui.ErrorState
import com.bakery.customer.core.ui.LoadingState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(error = state.error!!, onRetry = viewModel::load, modifier = modifier)
        state.items.isEmpty() -> EmptyState("No items yet.", modifier)
        else -> LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
            items(state.items, key = { it.id }) { item ->
                Text(text = "${item.name} — ₹${item.price}")
            }
        }
    }
}
