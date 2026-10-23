package com.rustor.feature.showcase

import androidx.lifecycle.SavedStateHandle
import com.rustor.core.common.error.AppException
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.feature.showcase.error.ShowcaseError
import com.rustor.feature.showcase.fake.FakeCatalogRepository
import com.rustor.feature.showcase.fake.MainDispatcherRule
import com.rustor.feature.showcase.uiState.FeedState
import com.rustor.feature.showcase.uiState.ShowcaseHeader
import com.rustor.feature.showcase.uiState.ShowcaseIntent
import com.rustor.feature.showcase.uiState.ShowcaseMode
import com.rustor.feature.showcase.uiState.ShowcaseSideEffect
import com.rustor.feature.showcase.uiState.ShowcaseUiState
import com.rustor.feature.showcase.viewModel.ShowcaseViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShowcaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCatalogRepository()
    private val savedStateHandle = SavedStateHandle()

    private val games = Category(id = CategoryId("games"), title = "Игры")
    private val tools = Category(id = CategoryId("tools"), title = "Инструменты")
    private val someApps = listOf(app("a", games), app("b", games))

    @Test
    fun `main starts in loading without empty catalog flash`() = runTest {
        repository.gate = CompletableDeferred()

        val viewModel = createViewModel(ShowcaseMode.Main)
        val states = collectStates(viewModel)
        advanceUntilIdle()

        assertTrue(states.none { it.feed is FeedState.EmptyCatalog })
        assertEquals(FeedState.Loading, viewModel.uiState.value.feed)
    }

    @Test
    fun `background failure with data keeps content and sends no effects`() = runTest {
        repository.feed.value = someApps
        repository.refreshError = AppException.NoInternet()

        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        assertEquals(FeedState.Content(someApps, isRefreshing = false), viewModel.uiState.value.feed)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `user refresh failure with data keeps content and shows snackbar`() = runTest {
        repository.feed.value = someApps
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        repository.refreshError = AppException.NoInternet()
        viewModel.onIntent(ShowcaseIntent.Refresh)
        advanceUntilIdle()

        assertEquals(FeedState.Content(someApps, isRefreshing = false), viewModel.uiState.value.feed)
        assertEquals(listOf(ShowcaseSideEffect.ShowSnackbarError(ShowcaseError.NoConnection)), effects)
    }

    @Test
    fun `user refresh failure without data shows error screen without snackbar`() = runTest {
        repository.refreshError = AppException.NoInternet()
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onIntent(ShowcaseIntent.Refresh)
        advanceUntilIdle()

        assertEquals(FeedState.Error(ShowcaseError.NoConnection), viewModel.uiState.value.feed)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `user refresh during background sync cancels background`() = runTest {
        repository.feed.value = someApps
        repository.gate = CompletableDeferred()
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        val effects = collectEffects(viewModel)

        viewModel.onIntent(ShowcaseIntent.Refresh)
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(false, true), repository.refreshCalls)
        assertEquals(1, repository.cancelledCalls)
        assertEquals(FeedState.Content(someApps, isRefreshing = false), viewModel.uiState.value.feed)
        assertEquals(listOf(ShowcaseSideEffect.ScrollToTop), effects)
    }

    @Test
    fun `repeated user refresh while own refresh runs is ignored`() = runTest {
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        advanceUntilIdle()

        repository.gate = CompletableDeferred()
        viewModel.onIntent(ShowcaseIntent.Refresh)
        viewModel.onIntent(ShowcaseIntent.Refresh)
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(false, true), repository.refreshCalls)
    }

    @Test
    fun `category mode does not sync on open but refreshes on user request`() = runTest {
        val viewModel = createViewModel(ShowcaseMode.Category(games.id))
        collectStates(viewModel)
        advanceUntilIdle()

        assertTrue(repository.refreshCalls.isEmpty())

        viewModel.onIntent(ShowcaseIntent.Refresh)
        advanceUntilIdle()

        assertEquals(listOf(true), repository.refreshCalls)
    }

    @Test
    fun `selected chip is saved in saved state handle`() = runTest {
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)

        viewModel.onIntent(ShowcaseIntent.SelectCategory(games.id))
        advanceUntilIdle()

        assertEquals("games", savedStateHandle.get<String>("selected_category"))
    }

    @Test
    fun `successful user refresh scrolls to top`() = runTest {
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onIntent(ShowcaseIntent.Refresh)
        advanceUntilIdle()

        assertEquals(listOf(ShowcaseSideEffect.ScrollToTop), effects)
    }

    @Test
    fun `cancelled background sync never shows error`() = runTest {
        repository.gate = CompletableDeferred()
        val viewModel = createViewModel(ShowcaseMode.Main)
        val states = collectStates(viewModel)

        viewModel.onIntent(ShowcaseIntent.Refresh)
        repository.gate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.cancelledCalls)
        assertTrue(states.none { it.feed is FeedState.Error })
    }

    @Test
    fun `selected chip survives view model recreation`() = runTest {
        val game = app("g", games)
        val tool = app("t", tools)
        repository.feed.value = listOf(game, tool)
        val first = createViewModel(ShowcaseMode.Main)
        collectStates(first)
        first.onIntent(ShowcaseIntent.SelectCategory(games.id))
        advanceUntilIdle()

        val restored = createViewModel(ShowcaseMode.Main)
        collectStates(restored)
        advanceUntilIdle()

        val header = restored.uiState.value.header as ShowcaseHeader.Main
        assertEquals(games.id, header.selectedCategory)
        assertEquals(FeedState.Content(listOf(game), isRefreshing = false), restored.uiState.value.feed)
    }

    @Test
    fun `selecting chip filters feed and back shows all again`() = runTest {
        val game = app("g", games)
        val tool = app("t", tools)
        repository.feed.value = listOf(game, tool)
        val viewModel = createViewModel(ShowcaseMode.Main)
        collectStates(viewModel)
        advanceUntilIdle()

        viewModel.onIntent(ShowcaseIntent.SelectCategory(games.id))
        advanceUntilIdle()
        assertEquals(FeedState.Content(listOf(game), isRefreshing = false), viewModel.uiState.value.feed)

        viewModel.onIntent(ShowcaseIntent.Back)
        advanceUntilIdle()
        assertEquals(FeedState.Content(listOf(game, tool), isRefreshing = false), viewModel.uiState.value.feed)
    }

    private fun createViewModel(mode: ShowcaseMode) = ShowcaseViewModel(repository, savedStateHandle, mode)

    private fun TestScope.collectStates(viewModel: ShowcaseViewModel): List<ShowcaseUiState> {
        val states = mutableListOf<ShowcaseUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }
        return states
    }

    private fun TestScope.collectEffects(viewModel: ShowcaseViewModel): List<ShowcaseSideEffect> {
        val effects = mutableListOf<ShowcaseSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        return effects
    }

    private fun app(id: String, category: Category) = App(
        id = AppId(id),
        name = "App $id",
        developer = "Dev",
        category = category,
        ageRating = AgeRating.ZERO_PLUS,
        iconUrl = null,
        summary = "",
        description = "",
        screenshots = emptyList(),
        apkUrl = null,
    )
}
