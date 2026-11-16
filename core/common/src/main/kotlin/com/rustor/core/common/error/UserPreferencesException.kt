package com.rustor.core.common.error

class UserPreferencesException(cause: Throwable) :
    Exception(
        "Unable to access user preferences",
        cause,
    )
