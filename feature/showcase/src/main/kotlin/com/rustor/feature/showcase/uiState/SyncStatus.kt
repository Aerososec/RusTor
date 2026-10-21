package com.rustor.feature.showcase.uiState

import com.rustor.core.common.error.AppException

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data class Syncing(val trigger: SyncTrigger) : SyncStatus
    data class Failed(val error: AppException) : SyncStatus
}
