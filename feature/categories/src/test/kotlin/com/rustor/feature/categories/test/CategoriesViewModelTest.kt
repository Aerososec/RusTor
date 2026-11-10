package com.rustor.feature.categories.test

import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import com.rustor.feature.categories.fake.FakeCatalogRepository
import com.rustor.feature.categories.fake.MainDispatcherRule
import com.rustor.feature.categories.ui.CategoriesIntent
import com.rustor.feature.categories.ui.CategoriesUiState
import com.rustor.feature.categories.ui.CategorySideEffect
import com.rustor.feature.categories.viewModel.CategoriesViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCatalogRepository()

    private val games = CategorySummary(
        id = CategoryId("games"),
        title = "Игры",
        appCount = 2,
    )
    private val tools = CategorySummary(
        id = CategoryId("tools"),
        title = "Инструменты",
        appCount = 0,
    )
    private val categories = listOf(games, tools)

    @Test
    fun `opening observes categories without refreshing`() = runTest {
        repository.categorySummaries.value = categories

        withViewModel { viewModel, effects ->
            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `empty repository shows no categories`() = runTest {
        withViewModel { viewModel, effects ->
            assertEquals(CategoriesUiState.NoCategoriesUi, viewModel.uiState.value)
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `repository updates are reflected in state`() = runTest {
        repository.categorySummaries.value = categories

        withViewModel { viewModel, effects ->
            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )

            val updatedCategories = listOf(
                games.copy(title = "Мобильные игры", appCount = 1),
                CategorySummary(
                    id = CategoryId("finance"),
                    title = "Финансы",
                    appCount = 2,
                ),
            )
            repository.categorySummaries.value = updatedCategories
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(updatedCategories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertTrue(repository.refreshCalls.isEmpty())
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh keeps content and shows progress`() = runTest {
        repository.categorySummaries.value = categories
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        withViewModel { viewModel, effects ->
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = true),
                viewModel.uiState.value,
            )
            assertEquals(listOf(false), repository.refreshCalls)

            gate.complete(Unit)
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh without categories shows loading then empty`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        withViewModel { viewModel, effects ->
            assertEquals(CategoriesUiState.NoCategoriesUi, viewModel.uiState.value)

            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(CategoriesUiState.Loading, viewModel.uiState.value)
            assertEquals(listOf(false), repository.refreshCalls)

            gate.complete(Unit)
            runCurrent()

            assertEquals(CategoriesUiState.NoCategoriesUi, viewModel.uiState.value)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh loads categories into empty screen`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        withViewModel { viewModel, effects ->
            assertEquals(CategoriesUiState.NoCategoriesUi, viewModel.uiState.value)

            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(CategoriesUiState.Loading, viewModel.uiState.value)

            repository.categorySummaries.value = categories
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = true),
                viewModel.uiState.value,
            )

            gate.complete(Unit)
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh failure with content shows snackbar`() = runTest {
        repository.categorySummaries.value = categories
        repository.refreshError = AppException.NoInternet()

        withViewModel { viewModel, effects ->
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertEquals(
                listOf(CategorySideEffect.ShowSnackbarError(UiError.NoConnection)),
                effects,
            )
            assertEquals(listOf(false), repository.refreshCalls)
        }
    }

    @Test
    fun `refresh failure without categories shows error`() = runTest {
        repository.refreshError = AppException.NoInternet()

        withViewModel { viewModel, effects ->
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(
                CategoriesUiState.Error(UiError.NoConnection),
                viewModel.uiState.value,
            )
            assertEquals(listOf(false), repository.refreshCalls)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `repeated refresh while request is running is ignored`() = runTest {
        repository.categorySummaries.value = categories
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        withViewModel { viewModel, effects ->
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            viewModel.onIntent(CategoriesIntent.Refresh)
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(0, repository.cancelledCalls)
            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = true),
                viewModel.uiState.value,
            )

            gate.complete(Unit)
            runCurrent()

            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(0, repository.cancelledCalls)
            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `refresh can run again after success`() = runTest {
        repository.categorySummaries.value = categories

        withViewModel { viewModel, effects ->
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(listOf(false), repository.refreshCalls)
            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )

            val secondGate = CompletableDeferred<Unit>()
            repository.gate = secondGate

            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(listOf(false, false), repository.refreshCalls)
            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = true),
                viewModel.uiState.value,
            )

            secondGate.complete(Unit)
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `retry after failure can recover`() = runTest {
        repository.refreshError = AppException.NoInternet()

        withViewModel { viewModel, effects ->
            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(
                CategoriesUiState.Error(UiError.NoConnection),
                viewModel.uiState.value,
            )

            repository.refreshError = null
            val retryGate = CompletableDeferred<Unit>()
            repository.gate = retryGate

            viewModel.onIntent(CategoriesIntent.Refresh)
            runCurrent()

            assertEquals(listOf(false, false), repository.refreshCalls)
            assertEquals(CategoriesUiState.Loading, viewModel.uiState.value)

            repository.categorySummaries.value = categories
            retryGate.complete(Unit)
            runCurrent()

            assertEquals(
                CategoriesUiState.Content(categories, isRefreshing = false),
                viewModel.uiState.value,
            )
            assertTrue(effects.isEmpty())
        }
    }

    private suspend fun TestScope.withViewModel(
        check: suspend (CategoriesViewModel, List<CategorySideEffect>) -> Unit,
    ) {
        val viewModel = CategoriesViewModel(repository)
        val effects = mutableListOf<CategorySideEffect>()

        val stateCollection = backgroundScope.launch {
            viewModel.uiState.collect()
        }
        val effectCollection = backgroundScope.launch {
            viewModel.effects.toList(effects)
        }

        try {
            runCurrent()
            check(viewModel, effects)
        } finally {
            stateCollection.cancel()
            effectCollection.cancel()
            viewModel.viewModelScope.cancel()
            runCurrent()
        }
    }
}
