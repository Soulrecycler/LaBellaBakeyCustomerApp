package com.bakery.customer.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

/** Platform-specific factory; Android needs a Context, iOS doesn't. */
expect class SessionStoreFactory {
    fun create(): DataStore<Preferences>
}

class SessionStore(private val dataStore: DataStore<Preferences>) {
    val authToken: Flow<String?> = dataStore.data.map { it[AUTH_TOKEN_KEY] }

    suspend fun setAuthToken(token: String?) {
        dataStore.edit { prefs ->
            if (token == null) prefs.remove(AUTH_TOKEN_KEY) else prefs[AUTH_TOKEN_KEY] = token
        }
    }
}
