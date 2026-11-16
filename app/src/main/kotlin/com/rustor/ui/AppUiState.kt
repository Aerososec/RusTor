package com.rustor.ui

sealed interface AppUiState {
    data object Loading : AppUiState

    data object Onboarding : AppUiState

    data object Main : AppUiState

    data object Error : AppUiState
}
