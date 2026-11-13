package com.rustor.feature.search.ui

import com.rustor.core.common.error.UiError
import com.rustor.core.model.App
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId

data class SearchUiState(
    val query: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: CategoryId? = null,
    val results: SearchResultsState = SearchResultsState.Loading,
)

sealed interface SearchResultsState {
    data object Loading : SearchResultsState

    data class Error(val error: UiError) : SearchResultsState

    data class Content(val apps: List<App>, val kind: SearchResultKind, val isRefreshing: Boolean = false) :
        SearchResultsState
}

enum class SearchResultKind {
    Matches,
    Popular,
    NoMatches,
}
