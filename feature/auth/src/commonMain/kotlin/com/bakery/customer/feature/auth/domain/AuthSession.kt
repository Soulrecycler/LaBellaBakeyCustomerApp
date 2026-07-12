package com.bakery.customer.feature.auth.domain

sealed class AuthSession {
    data object Unknown : AuthSession()
    data object Unauthenticated : AuthSession()
    data class Authenticated(val token: String) : AuthSession()
}

interface AuthRepository {
    val session: kotlinx.coroutines.flow.Flow<AuthSession>
    suspend fun signInWithGoogle(): Result<Unit>
    suspend fun signOut()
}
