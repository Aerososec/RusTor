package com.rustor.fake

import com.rustor.core.common.error.UserPreferencesException
import com.rustor.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class FakeUserPreferencesRepository : UserPreferencesRepository {

    private val readResult = MutableStateFlow<Result<Boolean>?>(null)

    var subscriptionCount = 0
        private set

    override val onboardingCompleted: Flow<Boolean> = flow {
        subscriptionCount++

        emitAll(
            readResult
                .filterNotNull()
                .map { result -> result.getOrThrow() },
        )
    }

    fun emitCompleted(completed: Boolean) {
        readResult.value = Result.success(completed)
    }

    fun failReading(error: UserPreferencesException) {
        readResult.value = Result.failure(error)
    }

    fun resetReadResult() {
        readResult.value = null
    }

    override suspend fun completeOnboarding() {
        emitCompleted(true)
    }
}
