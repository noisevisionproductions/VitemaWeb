package com.noisevisionsoftware.szytadieta.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.common.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val networkManager: NetworkConnectivityManager
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(true)

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    init {
        observeNetworkConnection()
    }

    private fun observeNetworkConnection() {
        viewModelScope.launch {
            networkManager.isNetworkConnected.collect { isConnected ->
                _isNetworkAvailable.update { isConnected }
                if (!isConnected) {
                    showNetworkError()
                }
            }
        }
    }

    protected fun showError(message: String) {
        viewModelScope.launch {
            if (!_isNetworkAvailable.value) {
                showNetworkError()
                return@launch
            }

            if (_uiEvent.value is UiEvent.ShowError &&
                (_uiEvent.value as UiEvent.ShowError).message == message
            ) {
                _uiEvent.value = null
                delay(100)
            }
            _uiEvent.value = UiEvent.ShowError(message)
        }
    }

    fun showSuccess(message: String) {
        viewModelScope.launch {
            if (_isNetworkAvailable.value) {
                if (_uiEvent.value is UiEvent.ShowSuccess &&
                    (_uiEvent.value as UiEvent.ShowSuccess).message == message
                ) {
                    _uiEvent.value = null
                    delay(100)
                }
                _uiEvent.value = UiEvent.ShowSuccess(message)
            } else {
                showNetworkError()
            }
        }
    }

    fun resetUiEvent() {
        _uiEvent.value = null
    }

    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Result<T>
    ): Result<T> {
        return if (!_isNetworkAvailable.value) {
            Result.failure(AppException.NetworkException("Brak połączenia z internetem"))
        } else {
            try {
                apiCall()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun showNetworkError() {
        viewModelScope.launch {
            val networkErrorMessage = "Brak połączenia z internetem"
            if (_uiEvent.value is UiEvent.ShowError &&
                (_uiEvent.value as UiEvent.ShowError).message == networkErrorMessage
            ) {
                _uiEvent.value = null
                delay(100)
            }
            _uiEvent.value = UiEvent.ShowError(networkErrorMessage)
        }
    }
}