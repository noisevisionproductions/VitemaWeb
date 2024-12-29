package com.noisevisionsoftware.szytadieta.domain.exceptions

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthEmailException

object ErrorMapper {
    fun mapFirebaseAuthError(e: Exception): AppException {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> {
                AppException.AuthException(mapErrorCode(e.errorCode))
            }

            is FirebaseAuthInvalidUserException -> {
                AppException.AuthException(mapErrorCode(e.errorCode))
            }

            is FirebaseAuthEmailException -> {
                when {
                    e.message?.contains("email-already-in-use") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_EMAIL_ALREADY_IN_USE"))

                    else -> AppException.AuthException(
                        e.localizedMessage ?: "Błąd związany z adresem email"
                    )
                }
            }

            is FirebaseException -> {
                when {
                    e.message?.contains("network") == true ->
                        AppException.NetworkException("Problem z połączeniem internetowym")

                    e.message?.contains("too-many-requests") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_OPERATION_NOT_ALLOWED"))

                    e.message?.contains("operation-not-allowed") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_OPERATION_NOT_ALLOWED"))

                    e.message?.contains("expired") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_USER_TOKEN_EXPIRED"))

                    e.message?.contains("CONFIGURATION_NOT_FOUND") == true ->
                        AppException.AuthException("Błąd konfiguracji. Spróbuj ponownie później")

                    else -> AppException.UnknownException()
                }
            }

            is IllegalArgumentException ->
                AppException.ValidationException("Nieprawidłowe dane: ${e.message}")

            else -> AppException.UnknownException()
        }
    }

    private fun mapErrorCode(errorCode: String): String {
        return when (errorCode) {
            "ERROR_INVALID_CUSTOM_TOKEN" -> "Błędny token uwierzytelniający"
            "ERROR_CUSTOM_TOKEN_MISMATCH" -> "Token nie pasuje do tej aplikacji"
            "ERROR_INVALID_CREDENTIAL" -> "Nieprawidłowe dane uwierzytelniające"
            "ERROR_INVALID_EMAIL" -> "Nieprawidłowy adres email"
            "ERROR_WRONG_PASSWORD" -> "Nieprawidłowe hasło"
            "ERROR_USER_MISMATCH" -> "Dane nie pasują do bieżącego użytkownika"
            "ERROR_REQUIRES_RECENT_LOGIN" -> "Ta operacja wymaga ponownego zalogowania"
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Konto istnieje z inną metodą logowania"

            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ten adres email jest już używany"
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Te dane uwierzytelniające są już używane"
            "ERROR_USER_DISABLED" -> "Konto zostało zablokowane"
            "ERROR_USER_TOKEN_EXPIRED" -> "Sesja wygasła. Zaloguj się ponownie"
            "ERROR_USER_NOT_FOUND" -> "Nie znaleziono użytkownika"
            "ERROR_INVALID_USER_TOKEN" -> "Nieprawidłowy token użytkownika"
            "ERROR_OPERATION_NOT_ALLOWED" -> "Operacja niedozwolona"
            "ERROR_WEAK_PASSWORD" -> "Hasło musi zawierać minimum 8 znaków, wielką literę, cyfrę i znak specjalny"
            else -> "Wystąpił nieoczekiwany błąd"
        }
    }
}