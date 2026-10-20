package com.rustor.core.common.error

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoInternet(cause: Throwable? = null) : AppException(NO_INTERNET_ERROR_MESSAGE, cause)
    class Timeout(cause: Throwable? = null) : AppException(TIMEOUT_ERROR_MESSAGE, cause)
    class Parsing(cause: Throwable? = null) : AppException(PARSING_ERROR_MESSAGE, cause)
    class Unknown(cause: Throwable? = null) : AppException(UNKNOWN_ERROR_MESSAGE, cause)

    companion object {
        private const val NO_INTERNET_ERROR_MESSAGE = "NO INTERNET"
        private const val TIMEOUT_ERROR_MESSAGE = "TIMEOUT ERROR"
        private const val PARSING_ERROR_MESSAGE = "PARSING ERROR"
        private const val UNKNOWN_ERROR_MESSAGE = "UNKNOWN ERROR"
    }
}
