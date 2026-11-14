package com.rustor.feature.search.viewModel

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.core.common.error.toUiError
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.CategoryId
import com.rustor.feature.search.ui.SearchIntent
import com.rustor.feature.search.ui.SearchResultKind
import com.rustor.feature.search.ui.SearchResultsState
import com.rustor.feature.search.ui.SearchSideEffect
import com.rustor.feature.search.ui.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: CatalogRepository) : ViewModel() {

    private var refreshJob: Job? = null

    private val isRefreshing = MutableStateFlow(false)
    private val retryGeneration = MutableStateFlow(0L)

    private val _effects = Channel<SearchSideEffect>(Channel.BUFFERED)
    val effects: Flow<SearchSideEffect> = _effects.receiveAsFlow()

    private val apps = observeResult {
        repository.observeFeed()
    }

    private val popularApps = observeResult {
        repository.observePopularApps()
    }

    private val categories = observeResult {
        repository.observeCategories()
    }

    private val selectedCategory = MutableStateFlow<CategoryId?>(null)
    private val query = MutableStateFlow("")

    private val refreshError = MutableStateFlow<UiError?>(null)

    @OptIn(FlowPreview::class)
    private val searchQuery = query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce { text ->
            if (text.isEmpty()) 0L else SEARCH_DEBOUNCE_MS
        }

    private val searchResultState: Flow<Pair<CategoryId?, SearchResultsState>> =
        combine(
            apps,
            popularApps,
            selectedCategory,
            searchQuery,
            refreshError,
        ) { appsResult, popularAppsResult, categoryId, text, updateError ->
            val results = appsResult.fold(
                onSuccess = { catalog ->
                    if (catalog.isEmpty() && updateError != null) {
                        SearchResultsState.Error(updateError)
                    } else {
                        computeSearchResultState(
                            catalog = catalog,
                            popularAppsResult = popularAppsResult,
                            text = text,
                            categoryId = categoryId,
                        )
                    }
                },
                onFailure = { error ->
                    SearchResultsState.Error(error.toReadUiError())
                },
            )

            categoryId to results
        }

    val uiState: StateFlow<SearchUiState> = observeUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUiState(): Flow<SearchUiState> = retryGeneration.flatMapLatest {
        combine(
            categories,
            query,
            searchResultState,
            isRefreshing,
        ) { categoriesResult, input, (categoryId, results), refreshing ->
            val categoriesError = categoriesResult.exceptionOrNull()

            val resolvedResults = if (categoriesError != null) {
                SearchResultsState.Error(categoriesError.toReadUiError())
            } else {
                results
            }

            SearchUiState(
                query = input,
                categories = categoriesResult.getOrDefault(emptyList()),
                selectedCategoryId = categoryId,
                results = if (
                    refreshing && resolvedResults is SearchResultsState.Error
                ) {
                    SearchResultsState.Loading
                } else {
                    resolvedResults
                },
                isRefreshing = refreshing,
            )
        }.onStart {
            val previousState = uiState.value

            emit(
                previousState.copy(
                    query = query.value,
                    selectedCategoryId = selectedCategory.value,
                    results = previousState.results as? SearchResultsState.Content
                        ?: SearchResultsState.Loading,
                    isRefreshing = isRefreshing.value,
                ),
            )
        }
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.CategorySelected -> selectCategory(intent.categoryId)

            is SearchIntent.QueryChanged -> queryChanged(intent.query)

            SearchIntent.SearchResultRefresh -> refresh()

            SearchIntent.Retry -> {
                if (refreshError.value != null) {
                    refresh()
                } else {
                    retryGeneration.update { it + 1L }
                }
            }
        }
    }

    private fun <T> observeResult(source: () -> Flow<T>): Flow<Result<T>> = flow<Result<T>> {
        emitAll(
            source().map { value ->
                Result.success(value)
            },
        )
    }.catch { error ->
        when (error) {
            is AppException, is SQLiteException -> {
                emit(Result.failure(error))
            }

            else -> throw error
        }
    }

    private fun Throwable.toReadUiError(): UiError = when (this) {
        is AppException -> toUiError()
        else -> UiError.Unknown
    }

    private fun queryChanged(newQuery: String) {
        query.value = newQuery
    }

    private fun selectCategory(categoryId: CategoryId?) {
        selectedCategory.value = categoryId
    }

    private fun search(query: String, apps: List<App>, selectedCategory: CategoryId?): List<App> {
        val normalizedQuery = query.trim()

        return apps.filter { app ->
            val matchesCategory = selectedCategory == null ||
                app.category.id == selectedCategory
            val matchesQuery = app.name.contains(
                normalizedQuery,
                ignoreCase = true,
            )

            matchesCategory && matchesQuery
        }
    }

    private fun computeSearchResultState(
        catalog: List<App>,
        popularAppsResult: Result<List<App>>,
        text: String,
        categoryId: CategoryId?,
    ): SearchResultsState {
        if (text.isNotEmpty()) {
            val matches = search(text, catalog, categoryId)

            if (matches.isNotEmpty()) {
                return SearchResultsState.Content(
                    apps = matches,
                    kind = SearchResultKind.Matches,
                )
            }
        }

        return popularAppsResult.fold(
            onSuccess = { popularApps ->
                SearchResultsState.Content(
                    apps = popularApps,
                    kind = if (text.isEmpty()) {
                        SearchResultKind.Popular
                    } else {
                        SearchResultKind.NoMatches
                    },
                )
            },
            onFailure = { error ->
                SearchResultsState.Error(error.toReadUiError())
            },
        )
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return

        isRefreshing.value = true

        refreshJob = viewModelScope.launch {
            try {
                repository.refresh(reshuffle = false)
                refreshError.value = null
            } catch (error: AppException) {
                val uiError = error.toUiError()
                refreshError.value = uiError

                if (hasCachedApps()) {
                    _effects.send(
                        SearchSideEffect.ShowSnackbarError(
                            error = uiError,
                        ),
                    )
                }
            } finally {
                isRefreshing.value = false
            }

            retryGeneration.update { it + 1L }
        }
    }

    private suspend fun hasCachedApps(): Boolean = try {
        repository.observeFeed().first().isNotEmpty()
    } catch (error: AppException) {
        Log.w(TAG, "Unable to check cached catalog apps", error)
        false
    } catch (error: SQLiteException) {
        Log.w(TAG, "Unable to read cached catalog apps from database", error)
        false
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val TAG = "SearchViewModel"
    }
}
