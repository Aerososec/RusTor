package com.rustor.test

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.UserPreferencesException
import com.rustor.fake.FakeUserPreferencesRepository
import com.rustor.fake.MainDispatcherRule
import com.rustor.ui.AppUiState
import com.rustor.viewModel.AppViewModel
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule: TestRule = MainDispatcherRule()

    private val repository = FakeUserPreferencesRepository()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any<String>(), any<String>(), any<Throwable>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `opening stays in loading until preferences emit`() = runTest {
        withViewModel { viewModel, states ->
            assertEquals(1, repository.subscriptionCount)
            assertEquals(AppUiState.Loading, viewModel.uiState.value)
            assertEquals(listOf(AppUiState.Loading), states)
        }
    }

    @Test
    fun `incomplete onboarding opens onboarding screen`() = runTest {
        repository.emitCompleted(false)

        withViewModel { viewModel, states ->
            assertEquals(AppUiState.Onboarding, viewModel.uiState.value)
            assertEquals(
                listOf(AppUiState.Loading, AppUiState.Onboarding),
                states,
            )
        }
    }

    @Test
    fun `completed onboarding opens main directly on startup`() = runTest {
        repository.emitCompleted(true)

        withViewModel { viewModel, states ->
            assertEquals(AppUiState.Main, viewModel.uiState.value)
            assertEquals(
                listOf(AppUiState.Loading, AppUiState.Main),
                states,
            )
        }
    }

    @Test
    fun `completing onboarding switches to main`() = runTest {
        repository.emitCompleted(false)

        withViewModel { viewModel, states ->
            assertEquals(AppUiState.Onboarding, viewModel.uiState.value)

            repository.completeOnboarding()
            runCurrent()

            assertEquals(AppUiState.Main, viewModel.uiState.value)
            assertEquals(
                listOf(AppUiState.Loading, AppUiState.Onboarding, AppUiState.Main),
                states,
            )
        }
    }

    @Test
    fun `read failure shows error`() = runTest {
        withViewModel { viewModel, states ->
            repository.failReading(readFailure())
            runCurrent()

            assertEquals(AppUiState.Error, viewModel.uiState.value)
            assertEquals(
                listOf(AppUiState.Loading, AppUiState.Error),
                states,
            )
        }
    }

    @Test
    fun `retry starts another read and opens main after success`() = runTest {
        repository.failReading(readFailure())

        withViewModel { viewModel, states ->
            assertEquals(AppUiState.Error, viewModel.uiState.value)
            assertEquals(1, repository.subscriptionCount)

            repository.resetReadResult()
            viewModel.retry()
            runCurrent()

            assertEquals(AppUiState.Loading, viewModel.uiState.value)
            assertEquals(2, repository.subscriptionCount)

            repository.emitCompleted(true)
            runCurrent()

            assertEquals(AppUiState.Main, viewModel.uiState.value)
            assertEquals(
                listOf(
                    AppUiState.Loading,
                    AppUiState.Error,
                    AppUiState.Loading,
                    AppUiState.Main,
                ),
                states,
            )
        }
    }

    @Test
    fun `another read failure after retry shows error again`() = runTest {
        repository.failReading(readFailure())

        withViewModel { viewModel, states ->
            assertEquals(AppUiState.Error, viewModel.uiState.value)

            repository.resetReadResult()
            viewModel.retry()
            runCurrent()

            assertEquals(AppUiState.Loading, viewModel.uiState.value)
            assertEquals(2, repository.subscriptionCount)

            repository.failReading(readFailure())
            runCurrent()

            assertEquals(AppUiState.Error, viewModel.uiState.value)
            assertEquals(
                listOf(
                    AppUiState.Loading,
                    AppUiState.Error,
                    AppUiState.Loading,
                    AppUiState.Error,
                ),
                states,
            )
        }
    }

    @Test
    fun `retry during active observation keeps the original subscription`() = runTest {
        repository.emitCompleted(false)

        withViewModel { viewModel, states ->
            assertEquals(AppUiState.Onboarding, viewModel.uiState.value)
            assertEquals(1, repository.subscriptionCount)

            viewModel.retry()
            viewModel.retry()
            runCurrent()

            assertEquals(1, repository.subscriptionCount)
            assertEquals(AppUiState.Onboarding, viewModel.uiState.value)

            repository.completeOnboarding()
            runCurrent()

            assertEquals(AppUiState.Main, viewModel.uiState.value)
            assertEquals(1, repository.subscriptionCount)
            assertEquals(
                listOf(AppUiState.Loading, AppUiState.Onboarding, AppUiState.Main),
                states,
            )
        }
    }

    private suspend fun TestScope.withViewModel(check: suspend (AppViewModel, List<AppUiState>) -> Unit) {
        val viewModel = AppViewModel(repository)
        val states = mutableListOf<AppUiState>()

        val stateCollection = backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler),
        ) {
            viewModel.uiState.toList(states)
        }

        try {
            runCurrent()
            check(viewModel, states)
        } finally {
            stateCollection.cancel()
            viewModel.viewModelScope.cancel()
            runCurrent()
        }
    }

    private fun readFailure(): UserPreferencesException =
        UserPreferencesException(IOException("Unable to read preferences"))
}
