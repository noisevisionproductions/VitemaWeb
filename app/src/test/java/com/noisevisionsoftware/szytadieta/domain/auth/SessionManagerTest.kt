package com.noisevisionsoftware.szytadieta.domain.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import com.noisevisionsoftware.szytadieta.domain.model.User
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SessionManagerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(UnconfinedTestDispatcher() + Job())
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var sessionManager: SessionManager

    private val testUser = User(
        id = "test_id",
        email = "test@example.com",
        nickname = "testUser",
        createdAt = 123456789
    )

    @Before
    fun setUp() {
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = { testContext.preferencesDataStoreFile("test_session") }
        )
        sessionManager = SessionManager(testContext)
    }

    @After
    fun cleanUp() {
        testCoroutineScope.cancel()
        File(testContext.filesDir, "datastore/test_session.preferences_pb").delete()
    }


    @Test
    fun saveUserSession_ShouldSaveUserData() = runTest {
        sessionManager.saveUserSession(testUser)

        val savedUser = sessionManager.userSessionFlow.first()
        assertThat(savedUser).isNotNull()
        assertThat(savedUser?.id).isEqualTo(testUser.id)
        assertThat(savedUser?.email).isEqualTo(testUser.email)
    }

    @Test
    fun clearSession_ShouldRemoveAllUserData() = runTest {
        sessionManager.saveUserSession(testUser)
        assertThat(sessionManager.userSessionFlow.first()).isNotNull()

        sessionManager.clearSession()

        val userAfterClear = sessionManager.userSessionFlow.first()
        assertThat(userAfterClear).isNull()
    }

    @Test
    fun getUserSessionFlow_ShouldReturnNull_WhenNoUserDataExists() = runTest {
        val user = sessionManager.userSessionFlow.first()

        assertThat(user).isNull()
    }

    @Test
    fun userSessionFlow_ShouldReturnValidUser_WhenDataExists() = runTest {
        sessionManager.saveUserSession(testUser)

        val savedUser = sessionManager.userSessionFlow.first()

        assertThat(savedUser).isNotNull()
        assertThat(savedUser?.id).isEqualTo(testUser.id)
        assertThat(savedUser?.email).isEqualTo(testUser.email)
    }

    @Test
    fun saveUserSession_ShouldOverwriteExistingData() = runTest {
        val initialUser = testUser.copy(
            id = "initial_id",
            email = "initial@example.com"
        )
        sessionManager.saveUserSession(initialUser)

        val newUser = testUser.copy(
            id = "new_id",
            email = "new@example.com"
        )
        sessionManager.saveUserSession(newUser)

        val savedUser = sessionManager.userSessionFlow.first()
        assertThat(savedUser).isNotNull()
        assertThat(savedUser?.id).isEqualTo(newUser.id)
        assertThat(savedUser?.email).isEqualTo(newUser.email)
    }
}