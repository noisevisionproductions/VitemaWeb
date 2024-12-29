package com.noisevisionsoftware.szytadieta.ui.screens.profile

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.auth.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.auth.ValidationManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.Gender
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager
) : BaseViewModel(networkManager) {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState = _profileState.asStateFlow()

    sealed class ProfileState {
        data object Initial : ProfileState()
        data object Loading : ProfileState()
        data class Success(val user: User) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    private var tempBirthDate: Long? = null
    private var tempGender: Gender? = null
    var profileUpdateMessageShown = false

    fun checkProfileCompletion(): Flow<Boolean> = flow {
        authRepository.getCurrentUserData()
            .onSuccess { user ->
                val isCompleted = user?.let {
                    val completed = it.profileCompleted && it.birthDate != null && it.gender != null
                    completed
                } ?: false
                emit(isCompleted)
            }
            .onFailure {
                emit(false)
            }
    }

    fun setTempBirthDate(birthDate: Long) {
        tempBirthDate = birthDate
    }

    fun setTempGender(gender: Gender) {
        tempGender = gender
    }

    fun saveProfile() {
        viewModelScope.launch {
            try {
                tempBirthDate?.let { birthDate ->
                    ValidationManager.validateBirthDate(birthDate).getOrThrow()
                    _profileState.value = ProfileState.Loading

                    updateUserField { currentUser ->
                        currentUser.copy(
                            birthDate = birthDate,
                            gender = tempGender,
                            profileCompleted = true
                        )
                    }
                }
            } catch (e: AppException) {
                handleError(e)
            }
        }
    }

    private suspend fun updateUserField(updateUser: (User) -> User) {
        authRepository.getCurrentUser()?.let {
            safeApiCall { authRepository.getCurrentUserData() }
                .onSuccess { currentUser ->
                    currentUser?.let { user ->
                        val updatedUser = updateUser(user)
                        safeApiCall { authRepository.updateUserData(updatedUser) }
                            .onSuccess {
                                _profileState.value = ProfileState.Success(updatedUser)
                                showSuccess("Profil został zaktualizowany")
                                tempBirthDate = null
                                tempGender = null
                            }
                            .onFailure { throwable ->
                                handleError(throwable)
                            }
                    }
                }
        }
    }

    private fun handleError(throwable: Throwable) {
        val appException = when (throwable) {
            is AppException -> throwable
            else -> AppException.UnknownException()
        }
        _profileState.value = ProfileState.Error(appException.message)
        showError(appException.message)
    }
}