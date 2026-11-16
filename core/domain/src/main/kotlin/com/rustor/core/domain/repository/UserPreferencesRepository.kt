package com.rustor.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val onboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()
}
