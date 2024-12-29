package com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.auth.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.auth.SessionManager
import com.noisevisionsoftware.szytadieta.domain.auth.ValidationManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.exceptions.ErrorMapper
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    networkManager: NetworkConnectivityManager
) : BaseViewModel(networkManager) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    val userSession = sessionManager.userSessionFlow

    sealed class AuthState {
        data object Initial : AuthState()
        data object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
        data object LoggedOut : AuthState()
    }

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            delay(1500)
            safeApiCall { authRepository.getCurrentUserData() }
                .onSuccess { user ->
                    user?.let {
                        sessionManager.saveUserSession(it)
                        _authState.value = AuthState.Success(it)
                    } ?: run {
                        _authState.value = AuthState.LoggedOut
                    }
                }
                .onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                ValidationManager.validateEmail(email).getOrThrow()
                ValidationManager.validatePassword(password).getOrThrow()

                _authState.value = AuthState.Loading
                safeApiCall { authRepository.login(email, password) }
                    .onSuccess { user ->
                        sessionManager.saveUserSession(user)
                        _authState.value = AuthState.Success(user)
                        showSuccess("Zalogowano pomyślnie")
                    }
                    .onFailure { throwable ->
                        handleError(throwable)
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                showError(e.message)
            }
        }
    }

    fun register(nickname: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                ValidationManager.validateNickname(nickname).getOrThrow()
                ValidationManager.validateEmail(email).getOrThrow()
                ValidationManager.validatePassword(password).getOrThrow()
                ValidationManager.validatePasswordConfirmation(password, confirmPassword)
                    .getOrThrow()

                _authState.value = AuthState.Loading
                safeApiCall { authRepository.register(nickname, email, password) }
                    .onSuccess { user ->
                        sessionManager.saveUserSession(user)
                        _authState.value = AuthState.Success(user)
                        showSuccess("Konto zostało utworzone")
                    }
                    .onFailure { throwable ->
                        handleError(throwable)
                    }
            } catch (e: AppException) {
                handleError(e)
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            safeApiCall { authRepository.getCurrentUserData() }
                .onSuccess { user ->
                    user?.let {
                        _authState.value = AuthState.Success(it)
                    } ?: run {
                        handleError(AppException.AuthException("Nie znaleziono danych użytkownika"))
                    }
                }
                .onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                if (email.isBlank()) {
                    throw AppException.ValidationException("Wprowadź adres email")
                }

                _authState.value = AuthState.Loading
                safeApiCall { authRepository.resetPassword(email) }
                    .onSuccess {
                        showSuccess("Link do resetowania hasła został wysłany na podany adres email")
                        _authState.value = AuthState.Initial
                    }
                    .onFailure { throwable ->
                        handleError(throwable)
                    }
            } catch (e: AppException) {
                handleError(e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            safeApiCall {
                sessionManager.clearSession()
                authRepository.logout()
            }
                .onSuccess {
                    _authState.value = AuthState.LoggedOut
                }
                .onFailure {
                    showError("Błąd podczas wylogowywania")
                }
        }
    }

    private fun handleError(throwable: Throwable) {
        val appException = when (throwable) {
            is AppException -> throwable
            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
            else -> AppException.UnknownException()
        }
        _authState.value = AuthState.Error(appException.message)
        showError(appException.message)
    }
}