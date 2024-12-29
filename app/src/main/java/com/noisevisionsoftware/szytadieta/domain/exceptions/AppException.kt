package com.noisevisionsoftware.szytadieta.domain.exceptions

sealed class AppException(override val message: String) : Exception(message) {
    data class AuthException(override val message: String) : AppException(message)
    data class NetworkException(override val message: String) : AppException(message)
    data class ValidationException(override val message: String) : AppException(message)
    data class UnknownException(override val message: String = "Wystąpił nieoczekiwany błąd") :
        AppException(message)
}