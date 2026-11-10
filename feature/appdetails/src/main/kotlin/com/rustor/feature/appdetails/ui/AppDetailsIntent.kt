package com.rustor.feature.appdetails.ui

sealed interface AppDetailsIntent {
    data object Refresh : AppDetailsIntent
}
