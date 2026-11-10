package com.rustor.feature.appdetails.ui

import com.rustor.core.common.error.UiError
import com.rustor.core.model.App

sealed interface AppDetailsUiState {
    data object Loading : AppDetailsUiState

    data object NotFound : AppDetailsUiState

    data class Error(val error: UiError) : AppDetailsUiState

    data class Content(val app: App, val isRefreshing: Boolean) : AppDetailsUiState
}
