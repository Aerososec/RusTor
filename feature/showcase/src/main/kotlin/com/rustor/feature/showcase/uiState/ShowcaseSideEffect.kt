package com.rustor.feature.showcase.uiState

import com.rustor.feature.showcase.error.ShowcaseError

sealed interface ShowcaseSideEffect {
    data object ScrollToTop : ShowcaseSideEffect
    data class ShowSnackbarError(val error: ShowcaseError) : ShowcaseSideEffect
}
