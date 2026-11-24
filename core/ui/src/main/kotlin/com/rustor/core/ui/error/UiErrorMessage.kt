package com.rustor.core.ui.error

import androidx.annotation.StringRes
import com.rustor.core.common.error.UiError
import com.rustor.core.ui.R

@StringRes
fun UiError.messageResId(): Int = when (this) {
    UiError.NoConnection -> R.string.rustor_error_no_connection
    UiError.ServiceUnavailable -> R.string.rustor_error_service_unavailable
    UiError.Unknown -> R.string.rustor_error_unknown
}
