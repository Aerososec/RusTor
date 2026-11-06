package com.rustor.feature.categories.ui

import com.rustor.core.common.error.AppException

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Failed(val error: AppException) : SyncStatus
}
