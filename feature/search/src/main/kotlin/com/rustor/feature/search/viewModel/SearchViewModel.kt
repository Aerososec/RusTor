package com.rustor.feature.search.viewModel

import android.database.sqlite.SQLiteException
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

    private val apps = observeWithRetry {
        repository.observeFeed()
    }

    private val categories = observeWithRetry {
        repository.observeCategories()
    }

    private val selectedCategory = MutableStateFlow<CategoryId?>(null)
    private val query = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val searchQuery = query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce { text ->
            if (text.isEmpty()) 0L else SEARCH_DEBOUNCE_MS
        }

    private val searchResultState: Flow<Pair<CategoryId?, SearchResultsState>> = combine(
        apps,
        selectedCategory,
        searchQuery,
    ) { appsResult, categoryId, text ->
        val results = appsResult.fold(
            onSuccess = { catalog ->
                computeSearchResultState(
                    catalog = catalog,
                    text = text,
                    categoryId = categoryId,
                )
            },
            onFailure = { error ->
                SearchResultsState.Error(error.toReadUiError())
            },
        )

        categoryId to results
    }.onStart {
        emit(selectedCategory.value to SearchResultsState.Loading)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        categories,
        query,
        searchResultState,
        isRefreshing,
    ) { categoriesResult, input, (categoryId, results), refreshing ->
        val categoriesError = categoriesResult.exceptionOrNull()

        SearchUiState(
            query = input,
            categories = categoriesResult.getOrDefault(emptyList()),
            selectedCategoryId = categoryId,
            results = when {
                categoriesError != null -> SearchResultsState.Error(
                    categoriesError.toReadUiError(),
                )

                results is SearchResultsState.Content -> results.copy(
                    isRefreshing = refreshing,
                )

                else -> results
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(),
    )

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.CategorySelected -> selectCategory(intent.categoryId)
            is SearchIntent.QueryChanged -> queryChanged(intent.query)
            SearchIntent.SearchResultRefresh -> refresh()
            SearchIntent.Retry -> retryGeneration.update { it + 1L }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> observeWithRetry(source: () -> Flow<T>): Flow<Result<T>> = retryGeneration.flatMapLatest {
        flow<Result<T>> {
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

        val popularApps = search("", catalog, categoryId)

        return SearchResultsState.Content(
            apps = popularApps,
            kind = if (text.isEmpty()) {
                SearchResultKind.Popular
            } else {
                SearchResultKind.NoMatches
            },
        )
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return

        isRefreshing.value = true

        refreshJob = viewModelScope.launch {
            try {
                repository.refresh(reshuffle = false)
            } catch (error: AppException) {
                _effects.send(
                    SearchSideEffect.ShowSnackbarError(
                        error = error.toUiError(),
                    ),
                )
            } finally {
                isRefreshing.value = false
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
