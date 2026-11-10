package com.rustor.feature.appdetails.ui

import com.rustor.core.common.error.UiError

sealed interface AppDetailsSideEffect {
    data class ShowSnackbarError(val error: UiError) : AppDetailsSideEffect
}
