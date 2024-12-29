package com.noisevisionsoftware.szytadieta.ui.base

import app.cash.turbine.test
import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.common.UiEvent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val networkManager: NetworkConnectivityManager = mockk()
    private val networkStatusFlow = MutableStateFlow(true)

    init {
        every { networkManager.isNetworkConnected } returns networkStatusFlow
    }

    private class TestViewModel(
        networkManager: NetworkConnectivityManager
    ) : BaseViewModel(networkManager) {
        fun emitError(message: String) = showError(message)
        fun emitSuccess(message: String) = showSuccess(message)
        suspend fun testApiCall() = safeApiCall { Result.success(true) }
    }

    @Test
    fun `when showing error with network available, should emit error event`() = runTest {
        val viewModel = TestViewModel(networkManager)
        val errorMessage = "Test error"

        viewModel.uiEvent.test {
            viewModel.emitError(errorMessage)

            assertEquals(null, awaitItem())
            val event = awaitItem()
            assertTrue(event is UiEvent.ShowError)
            assertEquals(errorMessage, (event as UiEvent.ShowError).message)
        }
    }

    @Test
    fun `when showing success with network available, should emit success event`() = runTest {
        val viewModel = TestViewModel(networkManager)
        val successMessage = "Test success"

        viewModel.uiEvent.test {
            viewModel.emitSuccess(successMessage)

            assertEquals(null, awaitItem())
            val event = awaitItem()
            assertTrue(event is UiEvent.ShowSuccess)
            assertEquals(successMessage, (event as UiEvent.ShowSuccess).message)
        }
    }

    @Test
    fun `safeApiCall should return failure when network is unavailable`() = runTest {
        val viewModel = TestViewModel(networkManager)

        networkStatusFlow.value = false
        advanceTimeBy(100)

        val result = viewModel.testApiCall()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(
            "Expected NetworkException but was ${exception?.javaClass}",
            exception is AppException.NetworkException
        )
        assertEquals("Brak połączenia z internetem", exception?.message)
    }
}