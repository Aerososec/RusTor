package com.rustor.feature.showcase.uiState

import com.rustor.core.model.App
import com.rustor.feature.showcase.error.ShowcaseError

sealed interface FeedState {
    data object Loading : FeedState
    data class Error(val error: ShowcaseError) : FeedState
    data object EmptyCatalog : FeedState
    data object EmptyCategory : FeedState
    data class Content(val apps: List<App>, val isRefreshing: Boolean) : FeedState
}
