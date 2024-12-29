package com.noisevisionsoftware.szytadieta.domain.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NetworkConnectivityManagerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var networkManager: NetworkConnectivityManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var context: Context
    private lateinit var mockNetwork: Network
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = false)
        mockNetwork = mockk()
        mockNetworkCapabilities = mockk()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns mockNetwork
        every { connectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every {
            connectivityManager.registerNetworkCallback(
                any<NetworkRequest>(),
                any<ConnectivityManager.NetworkCallback>()
            )
        } returns Unit
        every {
            connectivityManager.unregisterNetworkCallback(
                any<ConnectivityManager.NetworkCallback>()
            )
        } returns Unit

        networkManager = NetworkConnectivityManager(context)
    }

    @Test
    fun isCurrentlyConnected_ShouldReturnTrue_WhenNetworkHasInternetCapability() {
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        val result = networkManager.isCurrentlyConnected()

        assertThat(result).isTrue()
    }

    @Test
    fun isCurrentlyConnected_ShouldReturnFalse_WhenNetworkDoesNotHaveInternetCapability() {
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        val result = networkManager.isCurrentlyConnected()

        assertThat(result).isFalse()
    }

    @Test
    fun isCurrentlyConnected_ShouldReturnFalse_WhenNetworkCapabilitiesAreNull() {
        every { connectivityManager.getNetworkCapabilities(any()) } returns null

        val result = networkManager.isCurrentlyConnected()

        assertThat(result).isFalse()
    }

    @Test
    fun isNetworkConnected_ShouldEmitInitialState() = runTest {
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        networkManager.isNetworkConnected.test {
            assertThat(awaitItem()).isTrue()
            cancel()
        }
    }

    @Test
    fun isNetworkConnected_ShouldEmitStateChanges() = runTest {
        val callbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every {
            connectivityManager.registerNetworkCallback(
                any(),
                capture(callbackSlot)
            )
        } returns Unit
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        networkManager.isNetworkConnected.test {
            assertThat(awaitItem()).isTrue()

            callbackSlot.captured.onLost(mockNetwork)
            assertThat(awaitItem()).isFalse()

            callbackSlot.captured.onAvailable(mockNetwork)
            assertThat(awaitItem()).isTrue()

            cancel()
        }
    }
}