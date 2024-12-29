package com.noisevisionsoftware.szytadieta.ui.screens.weight

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.noisevisionsoftware.szytadieta.domain.auth.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.Weight
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.WeightRepository
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager
) : BaseViewModel(networkManager) {

    private val _weightState = MutableStateFlow<WeightState>(WeightState.Initial)
    val weightState = _weightState.asStateFlow()

    sealed class WeightState {
        data object Initial : WeightState()
        data object Loading : WeightState()
        data class Success(val weights: List<Weight>) : WeightState()
        data class Error(val exception: AppException) : WeightState()
    }

    init {
        loadWeights()
    }

    fun addWeight(weight: Double, note: String = "") {
        if (weight <= 0) {
            handleError(AppException.ValidationException("Waga musi być większa niż 0"))
            return
        }
        handleOperation {
            val currentUser = getCurrentUserOrThrow()

            val weightEntry = Weight(
                userId = currentUser.uid,
                weight = weight,
                note = note
            )

            safeApiCall { weightRepository.addWeight(weightEntry) }
                .onSuccess {
                    showSuccess("Pomyślnie dodano wagę")
                    loadWeights()
                }
                .onFailure { throwable ->
                    throw AppException.UnknownException(
                        throwable.message ?: "Błąd podczas dodawania wagi"
                    )
                }
        }

    }

    fun deleteWeight(weightId: String) {
        handleOperation {
            safeApiCall { weightRepository.deleteWeight(weightId) }
                .onSuccess {
                    showSuccess("Pomyślnie usunięto wpis")
                    loadWeights()
                }
                .onFailure { throwable ->
                    throw AppException.UnknownException(
                        throwable.message ?: "Błąd podczas usuwania wpisu"
                    )
                }
        }
    }

    private fun loadWeights() {
        handleOperation {
            val currentUser = getCurrentUserOrThrow()

            safeApiCall { weightRepository.getUserWeights(currentUser.uid) }
                .onSuccess { weights ->
                    _weightState.value = WeightState.Success(weights)
                }
                .onFailure { throwable ->
                    throw AppException.UnknownException(
                        throwable.message ?: "Błąd podczas pobierania historii wagi"
                    )
                }
        }
    }

    private fun getCurrentUserOrThrow(): FirebaseUser {
        return authRepository.getCurrentUser()
            ?: throw AppException.AuthException("Użytkownik nie jest zalogowany")
    }

    private fun handleOperation(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _weightState.value = WeightState.Loading
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        val exception = when (throwable) {
            is AppException -> throwable
            else -> AppException.UnknownException(
                throwable.message ?: "Wystąpił nieoczekiwany błąd"
            )
        }
        _weightState.value = WeightState.Error(exception)
        showError(exception.message)
    }
}