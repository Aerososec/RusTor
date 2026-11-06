package com.rustor.feature.showcase.uiState

import com.rustor.core.common.error.UiError
import com.rustor.core.model.App

sealed interface FeedState {
    data object Loading : FeedState
    data class Error(val error: UiError) : FeedState
    data object EmptyCatalog : FeedState
    data object EmptyCategory : FeedState
    data class Content(val apps: List<App>, val isRefreshing: Boolean) : FeedState
}
