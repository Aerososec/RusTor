@file:Suppress("TooManyFunctions")

package com.rustor.feature.search.test

import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.feature.search.fake.FakeSearchRepository
import com.rustor.feature.search.fake.MainDispatcherRule
import com.rustor.feature.search.fake.TestSource
import com.rustor.feature.search.ui.SearchIntent
import com.rustor.feature.search.ui.SearchResultKind
import com.rustor.feature.search.ui.SearchResultsState
import com.rustor.feature.search.ui.SearchSideEffect
import com.rustor.feature.search.viewModel.SearchViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LargeClass")
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSearchRepository()
    private val effects = mutableListOf<SearchSideEffect>()

    private val tools = Category(CategoryId("tools"), "Инструменты")
    private val games = Category(CategoryId("games"), "Игры")

    private val notes = app("notes", "Заметки", tools)
    private val noteGame = app("note-game", "Заметки приключений", games)
    private val maps = app("maps", "Organic Maps", tools)

    private val catalog = listOf(notes, noteGame, maps)
    private val popular = listOf(maps, notes)
    private val allCategories = listOf(tools, games)

    @Test
    fun loadingRemainsUntilRepositoryEmits() = runTest {
        seedCatalog()
        val gate = CompletableDeferred<Unit>()
        repository.feed.subscriptionGate = gate

        val viewModel = startViewModel()

        assertEquals(
            SearchResultsState.Loading,
            viewModel.uiState.value.results,
        )

        gate.complete(Unit)
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, popular)
    }

    @Test
    fun emptyQueryShowsPopularAndCategories() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        assertContent(viewModel, SearchResultKind.Popular, popular)
        assertEquals(allCategories, viewModel.uiState.value.categories)
        assertEquals("", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(repository.refreshCalls.isEmpty())
    }

    @Test
    fun emptyCatalogWithoutErrorsProducesEmptyContent() = runTest {
        val viewModel = startViewModel()

        assertContent(
            viewModel,
            SearchResultKind.Popular,
            emptyList(),
        )
        assertTrue(effects.isEmpty())
    }

    @Test
    fun searchIgnoresCaseAndSupportsCyrillicSubstrings() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        enterQuery(viewModel, "МЕТ")

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes, noteGame),
        )

        enterQuery(viewModel, "mAp")

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(maps),
        )
    }

    @Test
    fun searchTrimsInputButPreservesDisplayedQuery() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        val query = "  ЗАМЕТКИ  "

        enterQuery(viewModel, query)

        assertEquals(query, viewModel.uiState.value.query)
        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes, noteGame),
        )
    }

    @Test
    fun queryUpdatesBeforeDebouncedResults() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("заметки"))
        runCurrent()

        assertEquals("заметки", viewModel.uiState.value.query)
        assertContent(viewModel, SearchResultKind.Popular, popular)

        advanceTimeBy(DEBOUNCE_MS - 1)
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, popular)

        advanceTimeBy(1)
        runCurrent()

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes, noteGame),
        )
    }

    @Test
    fun debounceUsesLatestQuery() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("заметки"))
        runCurrent()
        advanceTimeBy(DEBOUNCE_MS / 2)

        viewModel.onIntent(SearchIntent.QueryChanged("maps"))
        runCurrent()

        advanceTimeBy(DEBOUNCE_MS - 1)
        runCurrent()

        assertEquals("maps", viewModel.uiState.value.query)
        assertContent(viewModel, SearchResultKind.Popular, popular)

        advanceTimeBy(1)
        runCurrent()

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(maps),
        )
    }

    @Test
    fun deletingCharactersRestoresPreviouslyExcludedApps() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        enterQuery(viewModel, "заметки приключений")

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(noteGame),
        )

        enterQuery(viewModel, "заметки")

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes, noteGame),
        )
    }

    @Test
    fun clearingQueryImmediatelyRestoresPopular() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "заметки")

        viewModel.onIntent(SearchIntent.QueryChanged(""))
        runCurrent()

        assertEquals("", viewModel.uiState.value.query)
        assertContent(viewModel, SearchResultKind.Popular, popular)
    }

    @Test
    fun whitespaceQueryImmediatelyRestoresPopular() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "заметки")

        viewModel.onIntent(SearchIntent.QueryChanged("   "))
        runCurrent()

        assertEquals("   ", viewModel.uiState.value.query)
        assertContent(viewModel, SearchResultKind.Popular, popular)
    }

    @Test
    fun categoryChangesFilterExistingQueryAndAllRemovesFilter() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "заметки")

        viewModel.onIntent(SearchIntent.CategorySelected(games.id))
        runCurrent()

        assertEquals(games.id, viewModel.uiState.value.selectedCategoryId)
        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(noteGame),
        )

        viewModel.onIntent(SearchIntent.CategorySelected(tools.id))
        runCurrent()

        assertEquals("заметки", viewModel.uiState.value.query)
        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes),
        )

        viewModel.onIntent(SearchIntent.CategorySelected(null))
        runCurrent()

        assertEquals(null, viewModel.uiState.value.selectedCategoryId)
        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes, noteGame),
        )
    }

    @Test
    fun noMatchesInCategoryShowsGlobalPopular() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "maps")

        viewModel.onIntent(SearchIntent.CategorySelected(games.id))
        runCurrent()

        assertEquals(games.id, viewModel.uiState.value.selectedCategoryId)
        assertContent(viewModel, SearchResultKind.NoMatches, popular)
    }

    @Test
    fun categoryWithEmptyQueryKeepsGlobalPopular() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        viewModel.onIntent(SearchIntent.CategorySelected(games.id))
        runCurrent()

        assertEquals(games.id, viewModel.uiState.value.selectedCategoryId)
        assertContent(viewModel, SearchResultKind.Popular, popular)
    }

    @Test
    fun noMatchesShowsPopularSubsetInRepositoryOrder() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        enterQuery(viewModel, "несуществующее приложение")

        assertContent(
            viewModel,
            SearchResultKind.NoMatches,
            listOf(maps, notes),
        )
    }

    @Test
    fun noMatchesWithEmptyPopularProducesEmptyContent() = runTest {
        seedCatalog()
        repository.popular.emit(emptyList())
        val viewModel = startViewModel()

        enterQuery(viewModel, "несуществующее приложение")

        assertContent(
            viewModel,
            SearchResultKind.NoMatches,
            emptyList(),
        )
    }

    @Test
    fun catalogEmissionRecomputesCurrentQueryAndCategory() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "заметки")
        viewModel.onIntent(SearchIntent.CategorySelected(tools.id))
        runCurrent()

        val extraTool = app("extra-tool", "Быстрые заметки", tools)
        val extraGame = app("extra-game", "Новые заметки", games)

        repository.feed.emit(catalog + extraTool + extraGame)
        runCurrent()

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(notes, extraTool),
        )
        assertEquals("заметки", viewModel.uiState.value.query)
        assertEquals(tools.id, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun popularEmissionUpdatesDisplayedSelection() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "несуществующее приложение")

        repository.popular.emit(listOf(noteGame, maps))
        runCurrent()

        assertContent(
            viewModel,
            SearchResultKind.NoMatches,
            listOf(noteGame, maps),
        )
    }

    @Test
    fun popularReadFailureShowsErrorWhenPopularIsNeeded() = runTest {
        seedCatalog()
        val viewModel = startViewModel()

        repository.popular.fail(AppException.NoInternet())
        runCurrent()

        assertError(viewModel)
    }

    @Test
    fun popularReadFailureKeepsMatchesUntilPopularIsNeeded() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        enterQuery(viewModel, "maps")

        repository.popular.fail(AppException.NoInternet())
        runCurrent()

        assertContent(
            viewModel,
            SearchResultKind.Matches,
            listOf(maps),
        )

        enterQuery(viewModel, "несуществующее приложение")

        assertError(viewModel)
    }

    @Test
    fun refreshKeepsContentAndPublishesUpdatedData() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate
        repository.onRefreshSuccess = {
            repository.feed.emit(listOf(noteGame))
            repository.popular.emit(listOf(noteGame))
        }

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()

        assertEquals(listOf(false), repository.refreshCalls)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertContent(viewModel, SearchResultKind.Popular, popular)

        gate.complete(Unit)
        runCurrent()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertContent(
            viewModel,
            SearchResultKind.Popular,
            listOf(noteGame),
        )
        assertTrue(effects.isEmpty())
    }

    @Test
    fun activeRefreshIgnoresRepeatedRequests() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()
        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()

        assertEquals(listOf(false), repository.refreshCalls)
        assertTrue(viewModel.uiState.value.isRefreshing)

        gate.complete(Unit)
        runCurrent()

        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun refreshFailureWithCachedAppsSendsExactlyOneSnackbar() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        repository.refreshError = AppException.NoInternet()

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, popular)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(
            listOf(
                SearchSideEffect.ShowSnackbarError(UiError.NoConnection),
            ),
            effects,
        )
    }

    @Test
    fun refreshFailureWithEmptyCatalogShowsErrorWithoutSnackbar() = runTest {
        val viewModel = startViewModel()
        repository.refreshError = AppException.NoInternet()

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()

        assertError(viewModel)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun refreshFailureUsesCatalogAvailabilityWithNoMatches() = runTest {
        seedCatalog()
        repository.popular.emit(emptyList())
        val viewModel = startViewModel()
        enterQuery(viewModel, "несуществующее приложение")
        repository.refreshError = AppException.NoInternet()

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()
        advanceTimeBy(DEBOUNCE_MS)
        runCurrent()

        assertContent(
            viewModel,
            SearchResultKind.NoMatches,
            emptyList(),
        )
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(
            listOf(
                SearchSideEffect.ShowSnackbarError(UiError.NoConnection),
            ),
            effects,
        )
    }

    @Test
    fun successfulRefreshCanProduceEmptyCatalog() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        repository.onRefreshSuccess = {
            repository.feed.emit(emptyList())
            repository.popular.emit(emptyList())
        }

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, emptyList())
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun feedReadRetryShowsLoadingAndRestoresObservation() = runTest {
        verifyReadRetry(repository.feed)
    }

    @Test
    fun categoriesReadRetryShowsLoadingAndRestoresObservation() = runTest {
        verifyReadRetry(repository.categories)
    }

    @Test
    fun popularReadRetryShowsLoadingAndRestoresObservation() = runTest {
        verifyReadRetry(repository.popular)
    }

    @Test
    fun repeatedReadFailureReturnsErrorAfterLoading() = runTest {
        seedCatalog()
        val viewModel = startViewModel()
        repository.feed.fail(AppException.NoInternet())
        runCurrent()
        assertError(viewModel)

        val gate = CompletableDeferred<Unit>()
        repository.feed.subscriptionGate = gate

        viewModel.onIntent(SearchIntent.Retry)
        runCurrent()

        assertEquals(
            SearchResultsState.Loading,
            viewModel.uiState.value.results,
        )

        gate.complete(Unit)
        runCurrent()

        assertError(viewModel)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(repository.refreshCalls.isEmpty())
    }

    @Test
    fun refreshRestoresFailedFeedObservation() = runTest {
        verifyRefreshAfterReadFailure(repository.feed)
    }

    @Test
    fun refreshRestoresFailedCategoriesObservation() = runTest {
        verifyRefreshAfterReadFailure(repository.categories)
    }

    @Test
    fun refreshRestoresFailedPopularObservation() = runTest {
        verifyRefreshAfterReadFailure(repository.popular)
    }

    @Test
    fun retryAfterRefreshFailureLoadsAgainAndClearsError() = runTest {
        val viewModel = startViewModel()
        repository.refreshError = AppException.NoInternet()

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()
        assertError(viewModel)

        repository.refreshError = null
        repository.onRefreshSuccess = { seedCatalog() }
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate

        viewModel.onIntent(SearchIntent.Retry)
        runCurrent()

        assertEquals(listOf(false, false), repository.refreshCalls)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(
            SearchResultsState.Loading,
            viewModel.uiState.value.results,
        )

        gate.complete(Unit)
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, popular)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())

        // Последующее пустое содержимое не должно вернуть старую ошибку.
        repository.feed.emit(emptyList())
        repository.popular.emit(emptyList())
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, emptyList())
    }

    @Test
    fun successfulEmptyRetryClearsPreviousRefreshError() = runTest {
        val viewModel = startViewModel()
        repository.refreshError = AppException.NoInternet()

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()
        assertError(viewModel)

        repository.refreshError = null
        viewModel.onIntent(SearchIntent.Retry)
        runCurrent()

        assertEquals(listOf(false, false), repository.refreshCalls)
        assertContent(viewModel, SearchResultKind.Popular, emptyList())
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun repeatedRefreshFailureReturnsErrorAndStopsIndicator() = runTest {
        val viewModel = startViewModel()
        repository.refreshError = AppException.NoInternet()

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()
        assertError(viewModel)

        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate

        viewModel.onIntent(SearchIntent.Retry)
        runCurrent()

        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(
            SearchResultsState.Loading,
            viewModel.uiState.value.results,
        )

        gate.complete(Unit)
        runCurrent()

        assertEquals(listOf(false, false), repository.refreshCalls)
        assertError(viewModel)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())
    }

    private fun TestScope.verifyReadRetry(source: TestSource<*>) {
        seedCatalog()
        val viewModel = startViewModel()

        source.fail(AppException.NoInternet())
        runCurrent()
        assertError(viewModel)

        val subscriptionsBeforeRetry = source.subscriptionCount

        // Новые данные сами по себе не оживляют завершившийся поток.
        seedCatalog()
        runCurrent()
        assertError(viewModel)

        val gate = CompletableDeferred<Unit>()
        source.subscriptionGate = gate

        viewModel.onIntent(SearchIntent.Retry)
        runCurrent()

        assertEquals(
            SearchResultsState.Loading,
            viewModel.uiState.value.results,
        )
        assertEquals(
            subscriptionsBeforeRetry + 1,
            source.subscriptionCount,
        )
        assertTrue(repository.refreshCalls.isEmpty())

        gate.complete(Unit)
        runCurrent()

        assertContent(viewModel, SearchResultKind.Popular, popular)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())
    }

    private fun TestScope.verifyRefreshAfterReadFailure(source: TestSource<*>) {
        seedCatalog()
        val viewModel = startViewModel()
        source.fail(AppException.NoInternet())
        runCurrent()
        assertError(viewModel)

        val subscriptionsBeforeRefresh = source.subscriptionCount
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate
        repository.onRefreshSuccess = { seedCatalog() }

        viewModel.onIntent(SearchIntent.SearchResultRefresh)
        runCurrent()

        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(
            SearchResultsState.Loading,
            viewModel.uiState.value.results,
        )

        gate.complete(Unit)
        runCurrent()

        assertEquals(listOf(false), repository.refreshCalls)
        assertEquals(
            subscriptionsBeforeRefresh + 1,
            source.subscriptionCount,
        )
        assertContent(viewModel, SearchResultKind.Popular, popular)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(effects.isEmpty())
    }

    private fun TestScope.startViewModel(): SearchViewModel {
        val viewModel = SearchViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        runCurrent()
        return viewModel
    }

    private fun TestScope.enterQuery(viewModel: SearchViewModel, query: String) {
        viewModel.onIntent(SearchIntent.QueryChanged(query))
        runCurrent()
        advanceTimeBy(DEBOUNCE_MS)
        runCurrent()
    }

    private fun seedCatalog() {
        repository.feed.emit(catalog)
        repository.popular.emit(popular)
        repository.categories.emit(allCategories)
    }

    private fun assertContent(viewModel: SearchViewModel, kind: SearchResultKind, apps: List<App>) {
        assertEquals(
            SearchResultsState.Content(apps = apps, kind = kind),
            viewModel.uiState.value.results,
        )
    }

    private fun assertError(viewModel: SearchViewModel) {
        assertEquals(
            SearchResultsState.Error(UiError.NoConnection),
            viewModel.uiState.value.results,
        )
    }

    private fun app(id: String, name: String, category: Category) = App(
        id = AppId(id),
        name = name,
        iconUrl = null,
        category = category,
        summary = "",
        description = "",
        developer = "Test developer",
        ageRating = AgeRating.ZERO_PLUS,
        screenshots = emptyList(),
        apkUrl = null,
    )

    private companion object {
        const val DEBOUNCE_MS = 400L
    }
}
