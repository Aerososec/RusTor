package com.rustor.feature.appdetails.test

import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.feature.appdetails.fake.MainDispatcherRule
import com.rustor.feature.appdetails.fake.testApp
import com.rustor.feature.appdetails.fake.withAppDetails
import com.rustor.feature.appdetails.ui.AppDetailsSideEffect
import com.rustor.feature.appdetails.ui.AppDetailsUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `cached app is shown without refresh`() = runTest {
        withAppDetails(cachedApp = testApp) {
            assertContent()
            assertEquals(setOf(testApp.id), repository.observedAppIds.toSet())
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `missing app triggers refresh and shows loading`() = runTest {
        val pendingRefresh = CompletableDeferred<Unit>()

        withAppDetails(configure = { gate = pendingRefresh }) {
            assertEquals(AppDetailsUiState.Loading, state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `app appears after initial refresh`() = runTest {
        val pendingRefresh = CompletableDeferred<Unit>()

        withAppDetails(configure = { gate = pendingRefresh }) {
            assertEquals(AppDetailsUiState.Loading, state)

            repository.setApp(testApp)
            pendingRefresh.complete(Unit)
            runCurrent()

            assertContent()
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `missing app after successful refresh shows not found`() = runTest {
        withAppDetails {
            assertEquals(AppDetailsUiState.NotFound, state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `initial refresh failure without app shows error`() = runTest {
        withAppDetails(configure = { refreshError = AppException.NoInternet() }) {
            assertEquals(AppDetailsUiState.Error(UiError.NoConnection), state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh keeps content until updated app arrives`() = runTest {
        val pendingRefresh = CompletableDeferred<Unit>()
        val updatedApp = testApp.copy(description = "Обновлённое описание")

        withAppDetails(
            cachedApp = testApp,
            configure = { gate = pendingRefresh },
        ) {
            assertContent()

            refresh()
            runCurrent()

            assertContent(isRefreshing = true)
            assertEquals(listOf(false), repository.refreshCalls)

            repository.setApp(updatedApp)
            pendingRefresh.complete(Unit)
            runCurrent()

            assertContent(updatedApp)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh failure keeps content and sends one snackbar`() = runTest {
        withAppDetails(
            cachedApp = testApp,
            configure = { refreshError = AppException.NoInternet() },
        ) {
            refresh()
            runCurrent()

            assertContent()
            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(
                listOf(AppDetailsSideEffect.ShowSnackbarError(UiError.NoConnection)),
                effects,
            )
        }
    }

    @Test
    fun `successful refresh can remove app`() = runTest {
        val pendingRefresh = CompletableDeferred<Unit>()

        withAppDetails(
            cachedApp = testApp,
            configure = { gate = pendingRefresh },
        ) {
            refresh()
            runCurrent()
            assertContent(isRefreshing = true)

            repository.setApp(null)
            pendingRefresh.complete(Unit)
            runCurrent()

            assertEquals(AppDetailsUiState.NotFound, state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `repeated refresh during request is ignored`() = runTest {
        val pendingRefresh = CompletableDeferred<Unit>()

        withAppDetails(
            cachedApp = testApp,
            configure = { gate = pendingRefresh },
        ) {
            refresh()
            runCurrent()

            refresh()
            refresh()
            runCurrent()

            assertContent(isRefreshing = true)
            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(0, repository.cancelledCalls)

            pendingRefresh.complete(Unit)
            runCurrent()

            assertContent()
            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(0, repository.cancelledCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh during initial loading is ignored`() = runTest {
        val pendingRefresh = CompletableDeferred<Unit>()

        withAppDetails(configure = { gate = pendingRefresh }) {
            assertEquals(AppDetailsUiState.Loading, state)
            assertEquals(listOf(false), repository.refreshCalls)

            refresh()
            runCurrent()

            assertEquals(AppDetailsUiState.Loading, state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(0, repository.cancelledCalls)

            pendingRefresh.complete(Unit)
            runCurrent()

            assertEquals(AppDetailsUiState.NotFound, state)
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh can run again after completion`() = runTest {
        val firstRefresh = CompletableDeferred<Unit>()
        val secondRefresh = CompletableDeferred<Unit>()

        withAppDetails(
            cachedApp = testApp,
            configure = { gate = firstRefresh },
        ) {
            refresh()
            runCurrent()
            assertContent(isRefreshing = true)

            firstRefresh.complete(Unit)
            runCurrent()
            assertContent()

            repository.gate = secondRefresh
            refresh()
            runCurrent()

            assertContent(isRefreshing = true)
            assertEquals(listOf(false, false), repository.refreshCalls)

            secondRefresh.complete(Unit)
            runCurrent()

            assertContent()
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `retry from error can recover`() = runTest {
        withAppDetails(configure = { refreshError = AppException.NoInternet() }) {
            assertEquals(AppDetailsUiState.Error(UiError.NoConnection), state)

            val pendingRefresh = CompletableDeferred<Unit>()
            repository.refreshError = null
            repository.gate = pendingRefresh

            refresh()
            runCurrent()

            assertEquals(AppDetailsUiState.Loading, state)
            assertEquals(listOf(false, false), repository.refreshCalls)

            repository.setApp(testApp)
            pendingRefresh.complete(Unit)
            runCurrent()

            assertContent()
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh from not found can load app`() = runTest {
        withAppDetails {
            assertEquals(AppDetailsUiState.NotFound, state)

            val pendingRefresh = CompletableDeferred<Unit>()
            repository.gate = pendingRefresh

            refresh()
            runCurrent()

            assertEquals(AppDetailsUiState.Loading, state)
            assertEquals(listOf(false, false), repository.refreshCalls)

            repository.setApp(testApp)
            pendingRefresh.complete(Unit)
            runCurrent()

            assertContent()
            assertTrue(effects.isEmpty())
        }
    }
}
