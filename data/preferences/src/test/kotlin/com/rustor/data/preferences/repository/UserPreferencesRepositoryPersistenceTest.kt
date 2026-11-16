package com.rustor.data.preferences.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class UserPreferencesRepositoryPersistenceTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `missing onboarding key reads as false`() = runTest {
        withRepository { repository ->
            assertFalse(repository.onboardingCompleted.first())
        }
    }

    @Test
    fun `completing onboarding emits true to an active subscriber`() = runTest {
        withRepository { repository ->
            repository.onboardingCompleted.test {
                assertFalse(awaitItem())

                repository.completeOnboarding()

                assertTrue(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `onboarding completion persists after reopening datastore`() = runTest {
        val file = preferencesFile()

        withRepository(file) { repository ->
            repository.completeOnboarding()
        }

        assertTrue(file.isFile)

        withRepository(file) { reopenedRepository ->
            assertTrue(reopenedRepository.onboardingCompleted.first())
        }
    }

    @Test
    fun `completing onboarding twice keeps the flag true`() = runTest {
        withRepository { repository ->
            repository.completeOnboarding()
            repository.completeOnboarding()

            assertTrue(repository.onboardingCompleted.first())
        }
    }

    private suspend fun TestScope.withRepository(
        file: File = preferencesFile(),
        check: suspend (UserPreferencesRepositoryImpl) -> Unit,
    ) {
        val storeJob = SupervisorJob()
        val storeScope = CoroutineScope(
            StandardTestDispatcher(testScheduler) + storeJob,
        )

        try {
            val dataStore = PreferenceDataStoreFactory.create(
                scope = storeScope,
                produceFile = { file },
            )

            check(UserPreferencesRepositoryImpl(dataStore))
        } finally {
            storeJob.cancelAndJoin()
        }
    }

    private fun preferencesFile(): File = File(temporaryFolder.root, "user_preferences.preferences_pb")
}
