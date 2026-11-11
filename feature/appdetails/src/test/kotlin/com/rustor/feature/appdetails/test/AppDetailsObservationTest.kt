package com.rustor.feature.appdetails.test

import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.feature.appdetails.fake.MainDispatcherRule
import com.rustor.feature.appdetails.fake.testApp
import com.rustor.feature.appdetails.fake.withAppDetails
import com.rustor.feature.appdetails.ui.AppDetailsSideEffect
import com.rustor.feature.appdetails.ui.AppDetailsUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailsObservationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `repository changes update displayed app`() = runTest {
        withAppDetails(cachedApp = testApp) {
            assertContent()
            val updatedApp = testApp.copy(name = "Новый калькулятор")

            repository.setApp(updatedApp)
            runCurrent()

            assertContent(updatedApp)
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `removed app becomes not found and can reappear`() = runTest {
        withAppDetails(cachedApp = testApp) {
            assertContent()

            repository.setApp(null)
            runCurrent()
            assertEquals(AppDetailsUiState.NotFound, state)

            repository.setApp(testApp)
            runCurrent()

            assertContent()
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `old refresh error does not affect later app removal`() = runTest {
        withAppDetails(
            cachedApp = testApp,
            configure = { refreshError = AppException.NoInternet() },
        ) {
            refresh()
            runCurrent()

            val expectedEffects = listOf(
                AppDetailsSideEffect.ShowSnackbarError(UiError.NoConnection),
            )
            assertContent()
            assertEquals(expectedEffects, effects)

            repository.setApp(null)
            runCurrent()

            assertEquals(AppDetailsUiState.NotFound, state)
            assertEquals(expectedEffects, effects)
            assertEquals(listOf(false), repository.refreshCalls)
        }
    }

    @Test
    fun `app appearing after error clears error for later emissions`() = runTest {
        withAppDetails(configure = { refreshError = AppException.NoInternet() }) {
            assertEquals(AppDetailsUiState.Error(UiError.NoConnection), state)

            repository.setApp(testApp)
            runCurrent()

            assertContent()
            assertTrue(effects.isEmpty())

            repository.setApp(null)
            runCurrent()

            assertEquals(AppDetailsUiState.NotFound, state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `read failure without content shows error`() = runTest {
        withAppDetails(configure = { failRead(AppException.Unknown()) }) {
            assertEquals(AppDetailsUiState.Error(UiError.Unknown), state)
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `read failure with content preserves last content`() = runTest {
        withAppDetails(cachedApp = testApp) {
            val updatedApp = testApp.copy(description = "Последнее описание из базы")
            repository.setApp(updatedApp)
            runCurrent()
            assertContent(updatedApp)

            repository.failRead(AppException.Unknown())
            runCurrent()

            assertContent(updatedApp)
            assertEquals(
                listOf(AppDetailsSideEffect.ShowSnackbarError(UiError.Unknown)),
                effects,
            )
            assertTrue(repository.refreshCalls.isEmpty())
        }
    }

    @Test
    fun `retry after read failure restarts observation`() = runTest {
        withAppDetails(configure = { failRead(AppException.Unknown()) }) {
            assertEquals(AppDetailsUiState.Error(UiError.Unknown), state)

            repository.setApp(testApp)
            refresh()
            runCurrent()

            assertContent()
            assertEquals(listOf(false), repository.refreshCalls)

            val updatedApp = testApp.copy(description = "Описание после повтора")
            repository.setApp(updatedApp)
            runCurrent()

            assertContent(updatedApp)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }
}
