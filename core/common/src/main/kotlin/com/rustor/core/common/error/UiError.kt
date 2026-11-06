package com.rustor.core.common.error

enum class UiError {
    NoConnection,
    ServiceUnavailable,
    Unknown,
}

fun AppException.toUiError(): UiError = when (this) {
    is AppException.NoInternet, is AppException.Timeout -> UiError.NoConnection
    is AppException.Parsing -> UiError.ServiceUnavailable
    is AppException.Unknown -> UiError.Unknown
}
