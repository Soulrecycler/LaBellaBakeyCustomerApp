package com.bakery.customer.feature.auth.data

import com.bakery.customer.core.database.SessionStore
import com.bakery.customer.feature.auth.domain.AuthRepository
import com.bakery.customer.feature.auth.domain.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val sessionStore: SessionStore,
) : AuthRepository {
    override val session: Flow<AuthSession> = sessionStore.authToken.map { token ->
        if (token != null) AuthSession.Authenticated(token) else AuthSession.Unauthenticated
    }

    // Placeholder until native Google Sign-In is wired per platform; issues a fake local token
    // so the auth gate and downstream screens are testable before that integration lands.
    override suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        sessionStore.setAuthToken("fake-session-token")
    }

    override suspend fun signOut() {
        sessionStore.setAuthToken(null)
    }
}
