package com.rustor.feature.search.ui

import com.rustor.core.common.error.UiError

sealed interface SearchSideEffect {
    data class ShowSnackbarError(val error: UiError) : SearchSideEffect
}
