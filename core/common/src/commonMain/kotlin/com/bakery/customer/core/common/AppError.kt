package com.bakery.customer.core.common

sealed class AppError {
    data object Network : AppError()
    data object Unauthorized : AppError()
    data class ServerError(val message: String) : AppError()
    data object PaymentFailed : AppError()
    data class Unknown(val message: String) : AppError()
}
