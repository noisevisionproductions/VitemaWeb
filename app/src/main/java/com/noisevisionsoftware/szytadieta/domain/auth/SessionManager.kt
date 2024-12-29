package com.noisevisionsoftware.szytadieta.domain.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noisevisionsoftware.szytadieta.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    suspend fun saveUserSession(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val userSessionFlow: Flow<User?> = dataStore.data.map { preferences ->
        if (preferences[USER_ID_KEY] != null) {
            User(
                id = preferences[USER_ID_KEY] ?: "",
                email = preferences[USER_EMAIL_KEY] ?: ""
            )
        } else {
            null
        }
    }
}