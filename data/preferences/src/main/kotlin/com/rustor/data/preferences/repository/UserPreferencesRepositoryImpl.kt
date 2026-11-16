package com.rustor.data.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.rustor.core.common.error.UserPreferencesException
import com.rustor.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

internal class UserPreferencesRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {

    override val onboardingCompleted: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }
        .distinctUntilChanged()
        .catch { error ->
            if (error is IOException) {
                throw UserPreferencesException(error)
            }

            throw error
        }

    override suspend fun completeOnboarding() {
        try {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED] = true
            }
        } catch (error: IOException) {
            throw UserPreferencesException(error)
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED =
            booleanPreferencesKey("onboarding_completed")
    }
}
