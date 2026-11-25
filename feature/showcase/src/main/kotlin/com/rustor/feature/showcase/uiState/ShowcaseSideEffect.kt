package com.rustor.feature.showcase.uiState

import com.rustor.core.common.error.UiError

sealed interface ShowcaseSideEffect {
    data class ShowSnackbarError(val error: UiError) : ShowcaseSideEffect
}
