package com.rustor.feature.onboarding.test.fake

import com.rustor.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeUserPreferencesRepository : UserPreferencesRepository {

    private val completed = MutableStateFlow(false)
    override val onboardingCompleted: Flow<Boolean> = completed

    var saveResult = CompletableDeferred<Unit>()

    var saveCalls = 0
        private set

    override suspend fun completeOnboarding() {
        saveCalls++
        saveResult.await()
        completed.value = true
    }
}
