package com.rustor.feature.onboarding.test.test

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.UserPreferencesException
import com.rustor.feature.onboarding.test.fake.FakeUserPreferencesRepository
import com.rustor.feature.onboarding.test.fake.MainDispatcherRule
import com.rustor.feature.onboarding.ui.OnboardingIntent
import com.rustor.feature.onboarding.ui.OnboardingUiState
import com.rustor.feature.onboarding.viewModel.OnboardingViewModel
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    fun `opening shows ready without saving`() = runTest {
        withViewModel { viewModel ->
            assertEquals(OnboardingUiState.Ready, viewModel.uiState.value)
            assertEquals(0, repository.saveCalls)
        }
    }

    @Test
    fun `continue starts one save and keeps saving after success`() = runTest {
        withViewModel { viewModel ->
            viewModel.onIntent(OnboardingIntent.ContinueClicked)
            runCurrent()

            assertEquals(OnboardingUiState.Saving, viewModel.uiState.value)
            assertEquals(1, repository.saveCalls)

            repository.saveResult.complete(Unit)
            runCurrent()

            assertTrue(repository.onboardingCompleted.first())
            assertEquals(OnboardingUiState.Saving, viewModel.uiState.value)
            assertEquals(1, repository.saveCalls)
        }
    }

    @Test
    fun `repeated continue while saving is ignored`() = runTest {
        withViewModel { viewModel ->
            viewModel.onIntent(OnboardingIntent.ContinueClicked)
            runCurrent()

            viewModel.onIntent(OnboardingIntent.ContinueClicked)
            runCurrent()

            assertEquals(OnboardingUiState.Saving, viewModel.uiState.value)
            assertEquals(1, repository.saveCalls)

            repository.saveResult.complete(Unit)
            runCurrent()

            assertEquals(1, repository.saveCalls)
        }
    }

    @Test
    fun `save failure shows save failed state`() = runTest {
        withViewModel { viewModel ->
            viewModel.onIntent(OnboardingIntent.ContinueClicked)
            runCurrent()

            repository.saveResult.completeExceptionally(saveFailure())
            runCurrent()

            assertEquals(OnboardingUiState.SaveFailed, viewModel.uiState.value)
            assertEquals(1, repository.saveCalls)
        }
    }

    @Test
    fun `continue after failure starts another save`() = runTest {
        withViewModel { viewModel ->
            viewModel.onIntent(OnboardingIntent.ContinueClicked)
            runCurrent()

            repository.saveResult.completeExceptionally(saveFailure())
            runCurrent()

            assertEquals(OnboardingUiState.SaveFailed, viewModel.uiState.value)
            assertEquals(1, repository.saveCalls)

            val retryGate = CompletableDeferred<Unit>()
            repository.saveResult = retryGate

            viewModel.onIntent(OnboardingIntent.ContinueClicked)
            runCurrent()

            assertEquals(OnboardingUiState.Saving, viewModel.uiState.value)
            assertEquals(2, repository.saveCalls)

            retryGate.complete(Unit)
            runCurrent()

            assertTrue(repository.onboardingCompleted.first())
            assertEquals(OnboardingUiState.Saving, viewModel.uiState.value)
        }
    }

    private suspend fun TestScope.withViewModel(check: suspend (OnboardingViewModel) -> Unit) {
        val viewModel = OnboardingViewModel(repository)

        try {
            runCurrent()
            check(viewModel)
        } finally {
            viewModel.viewModelScope.cancel()
            runCurrent()
        }
    }

    private fun saveFailure(): UserPreferencesException =
        UserPreferencesException(IOException("Unable to write preferences"))
}
