package com.bakery.customer.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun httpClient(
    baseUrl: String,
    tokenProvider: () -> String?,
): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        level = LogLevel.INFO
    }
    install(Auth) {
        bearer {
            loadTokens { tokenProvider()?.let { BearerTokens(it, "") } }
        }
    }
    defaultRequest {
        url(baseUrl)
    }
}
