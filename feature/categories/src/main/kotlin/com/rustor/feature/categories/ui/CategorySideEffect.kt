package com.rustor.feature.categories.ui

import com.rustor.core.common.error.UiError

sealed interface CategorySideEffect {
    data class ShowSnackbarError(val error: UiError) : CategorySideEffect
}
