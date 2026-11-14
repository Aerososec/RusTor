package com.rustor.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.rustor.core.common.error.UiError
import com.rustor.core.model.AppId
import com.rustor.core.model.CategoryId
import com.rustor.core.ui.components.search.RusTorSearchField
import com.rustor.core.ui.components.state.RusTorErrorState
import com.rustor.core.ui.components.state.RusTorLoadingState
import com.rustor.feature.search.ui.RusTorSearchFilters
import com.rustor.feature.search.ui.RusTorSearchResults
import com.rustor.feature.search.ui.SearchIntent
import com.rustor.feature.search.ui.SearchResultKind
import com.rustor.feature.search.ui.SearchResultsState
import com.rustor.feature.search.ui.SearchSideEffect
import com.rustor.feature.search.ui.SearchUiState
import com.rustor.feature.search.viewModel.SearchViewModel

@Composable
fun SearchRoute(onAppClick: (AppId) -> Unit, modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<SearchViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is SearchSideEffect.ShowSnackbarError ->
                        snackbarHostState.showSnackbar(
                            message = "Временные неполадки " +
                                effect.error.userMessage(),
                        )
                }
            }
        }
    }

    SearchScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        onAppClick = onAppClick,
        modifier = modifier,
    )
}

@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onAppClick: (AppId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequest = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequest.requestFocus()
    }

    val onRefresh = {
        onIntent(SearchIntent.SearchResultRefresh)
    }

    val onRetry = {
        onIntent(SearchIntent.Retry)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                RusTorSearchField(
                    query = uiState.query,
                    onQueryChange = { query ->
                        onIntent(SearchIntent.QueryChanged(query))
                    },
                    onSearch = {},
                    modifier = Modifier.focusRequester(focusRequest),
                )

                RusTorSearchFilters(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId?.value,
                    onCategorySelected = { categoryId ->
                        onIntent(
                            SearchIntent.CategorySelected(
                                categoryId = categoryId?.let { CategoryId(it) },
                            ),
                        )
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing &&
                uiState.results != SearchResultsState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            SearchResult(
                uiState = uiState,
                onAppClick = onAppClick,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SearchResult(uiState: SearchUiState, onAppClick: (AppId) -> Unit, onRetry: () -> Unit, modifier: Modifier) {
    when (uiState.results) {
        is SearchResultsState.Content -> {
            val result = uiState.results
            RusTorSearchResults(
                apps = result.apps,
                showingPopular = result.kind == SearchResultKind.Popular ||
                    result.kind == SearchResultKind.NoMatches,
                noResults = result.kind == SearchResultKind.NoMatches,
                onAppClick = onAppClick,
                modifier = modifier,
            )
        }

        is SearchResultsState.Error -> {
            RusTorErrorState(
                title = stringResource(com.rustor.core.ui.R.string.rustor_error_title),
                description = uiState.results.error.userMessage(),
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        SearchResultsState.Loading -> {
            RusTorLoadingState(
                text = stringResource(com.rustor.core.ui.R.string.rustor_loading),
                modifier = modifier.verticalScroll(rememberScrollState()),
            )
        }
    }
}

private fun UiError.userMessage(): String = when (this) {
    UiError.NoConnection -> "Проверь подключение к интернету."
    UiError.ServiceUnavailable -> "Сервис временно недоступен."
    UiError.Unknown -> "Попробуй ещё раз."
}
