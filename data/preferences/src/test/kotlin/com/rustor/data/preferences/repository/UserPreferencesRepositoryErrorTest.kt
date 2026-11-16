package com.rustor.data.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.rustor.core.common.error.UserPreferencesException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFailsWith

class UserPreferencesRepositoryErrorTest {

    @Test
    fun `read io exception is wrapped and preserved as cause`() = runTest {
        val cause = IOException("Unable to read preferences")
        val dataStore = TestPreferencesDataStore(
            data = flow { throw cause },
        )
        val repository = UserPreferencesRepositoryImpl(dataStore)

        val error = assertFailsWith<UserPreferencesException> {
            repository.onboardingCompleted.first()
        }

        assertSame(cause, error.cause)
    }

    @Test
    fun `write io exception is wrapped and preserved as cause`() = runTest {
        val cause = IOException("Unable to write preferences")
        val dataStore = TestPreferencesDataStore(
            update = { throw cause },
        )
        val repository = UserPreferencesRepositoryImpl(dataStore)

        val error = assertFailsWith<UserPreferencesException> {
            repository.completeOnboarding()
        }

        assertSame(cause, error.cause)
    }

    @Test
    fun `cancelling an active read propagates cancellation`() = runTest {
        val readStarted = CompletableDeferred<Unit>()
        val dataStore = TestPreferencesDataStore(
            data = flow {
                readStarted.complete(Unit)
                awaitCancellation()
            },
        )
        val repository = UserPreferencesRepositoryImpl(dataStore)

        val reader = launch {
            assertFailsWith<CancellationException> {
                repository.onboardingCompleted.first()
            }
        }

        readStarted.await()
        reader.cancelAndJoin()
    }

    @Test
    fun `cancelling an active write propagates cancellation`() = runTest {
        val writeStarted = CompletableDeferred<Unit>()
        val dataStore = TestPreferencesDataStore(
            update = {
                writeStarted.complete(Unit)
                awaitCancellation()
            },
        )
        val repository = UserPreferencesRepositoryImpl(dataStore)

        val writer = launch {
            assertFailsWith<CancellationException> {
                repository.completeOnboarding()
            }
        }

        writeStarted.await()
        writer.cancelAndJoin()
    }

    private class TestPreferencesDataStore(
        override val data: Flow<Preferences> = flowOf(emptyPreferences()),
        private val update: suspend () -> Preferences = {
            error("Unexpected call to updateData")
        },
    ) : DataStore<Preferences> {

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = update()
    }
}
