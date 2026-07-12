package com.bakery.customer.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bakery.customer.core.designsystem.BakeryTheme
import com.bakery.customer.core.ui.LoadingState
import com.bakery.customer.feature.auth.domain.AuthRepository
import com.bakery.customer.feature.auth.domain.AuthSession
import com.bakery.customer.feature.home.HomeScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data object Home

@Serializable
data class ItemDetails(val itemId: String)

@Composable
fun App() {
    BakeryTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val authRepository = koinInject<AuthRepository>()
            val session by authRepository.session.collectAsStateWithLifecycle(initialValue = AuthSession.Unknown)

            when (val current = session) {
                is AuthSession.Unknown -> LoadingState()
                is AuthSession.Unauthenticated -> LoginPlaceholder(authRepository)
                is AuthSession.Authenticated -> BakeryNavHost()
            }
        }
    }
}

@Composable
private fun LoginPlaceholder(authRepository: AuthRepository) {
    val scope = rememberCoroutineScope()
    Button(onClick = { scope.launch { authRepository.signInWithGoogle() } }) {
        Text("Continue with Google")
    }
}

@Composable
private fun BakeryNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(onItemClick = { itemId -> navController.navigate(ItemDetails(itemId)) })
        }
        composable<ItemDetails> {
            // Placeholder until the Item Details screen (build plan step A5) lands.
            Text("Item details coming soon")
        }
    }
}
