package com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.noisevisionsoftware.szytadieta.HiltTestApplication_Application
import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import com.noisevisionsoftware.szytadieta.domain.auth.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.auth.SessionManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.common.UiEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@HiltAndroidTest
@Config(application = HiltTestApplication_Application::class)
@RunWith(RobolectricTestRunner::class)
class AuthViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var networkManager: NetworkConnectivityManager

    private val email = "test@example.com"
    private val password = "password123"
    private val nickname = "testUser"
    private val userId = "testUserId"
    private val testUser = User(
        id = userId,
        email = email,
        nickname = nickname,
        createdAt = 123456789
    )

    @Before
    fun setUp() {
        hiltRule.inject()

        authRepository = mockk(relaxed = false)
        sessionManager = mockk(relaxed = false)
        networkManager = mockk(relaxed = true)

        coEvery { networkManager.isNetworkConnected } returns flowOf(true)
        every { networkManager.isCurrentlyConnected() } returns true

        every { sessionManager.userSessionFlow } returns flowOf(null)
        coEvery { sessionManager.saveUserSession(any()) } returns Unit
        coEvery { sessionManager.clearSession() } returns Unit

        coEvery { authRepository.getCurrentUserData() } returns Result.success(null)

        viewModel = AuthViewModel(authRepository, sessionManager, networkManager)
    }

    @Test
    fun register_ShouldEmitError_WhenPasswordsDoesNotMatch() = runTest {
        coEvery { authRepository.register(nickname, email, password) } returns Result.success(
            testUser
        )
        val authStateJob = launch {
            viewModel.authState.test {
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

                viewModel.register(nickname, email, password, "different_password")

                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Error("Hasła nie są identyczne"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        authStateJob.join()
        coVerify(exactly = 0) { authRepository.register(any(), any(), any()) }
    }

    @Test
    fun getCurrentUser_ShouldUpdateStateToSuccess_WhenUserExists() = runTest {
        coEvery { authRepository.getCurrentUserData() } returns Result.success(testUser)

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.getCurrentUser()

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Success(testUser))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_ShouldUpdateStateToSuccess_WhenCredentialsAreValid() = runTest {
        coEvery { authRepository.login(email, password) } returns Result.success(testUser)

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.login(email, password)

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Success(testUser))
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            authRepository.login(email, password)
            sessionManager.saveUserSession(testUser)
        }
    }

    @Test
    fun login_ShouldUpdateStateToError_WhenCredentialsAreInvalid() = runTest {
        val errorMessage = "Nieprawidłowe dane logowania"
        coEvery { authRepository.login(email, password) } returns Result.failure(
            AppException.AuthException(errorMessage)
        )

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.login(email, password)

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Error(errorMessage))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun register_ShouldUpdateStateToSuccess_WhenDataIsValid() = runTest {
        coEvery { authRepository.register(nickname, email, password) } returns Result.success(
            testUser
        )

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.register(nickname, email, password, password)

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Success(testUser))
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            authRepository.register(nickname, email, password)
            sessionManager.saveUserSession(testUser)
        }
    }

    @Test
    fun resetPassword_ShouldUpdateStateToInitial_WhenSuccessful() = runTest {
        coEvery { authRepository.resetPassword(email) } returns Result.success(Unit)

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.resetPassword(email)

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { authRepository.resetPassword(email) }
    }

    @Test
    fun resetPassword_ShouldUpdateStateToError_WhenEmailIsBlank() = runTest {
        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.resetPassword("")

            assertThat(awaitItem()).isEqualTo(
                AuthViewModel.AuthState.Error("Wprowadź adres email")
            )
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { authRepository.resetPassword(any()) }
    }

    @Test
    fun logout_ShouldUpdateStateToLoggedOut_WhenSuccessful() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.logout()

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.LoggedOut)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            sessionManager.clearSession()
            authRepository.logout()
        }
    }

    @Test
    fun uiEvents_ShouldEmitSuccessMessage_WhenLoginIsSuccessful() = runTest {
        coEvery { authRepository.login(email, password) } returns Result.success(testUser)

        viewModel.uiEvent.test {
            assertThat(awaitItem()).isNull()

            viewModel.login(email, password)

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowSuccess::class.java)
            assertThat((event as UiEvent.ShowSuccess).message).isEqualTo("Zalogowano pomyślnie")
        }
    }

    @Test
    fun uiEvents_ShouldEmitErrorMessage_WhenLoginFails() = runTest {
        val errorMessage = "Nieprawidłowe dane logowania"
        coEvery { authRepository.login(email, password) } returns Result.failure(
            AppException.AuthException(errorMessage)
        )

        viewModel.uiEvent.test {
            assertThat(awaitItem()).isNull()

            viewModel.login(email, password)

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowError::class.java)
            assertThat((event as UiEvent.ShowError).message).isEqualTo(errorMessage)
        }
    }
}