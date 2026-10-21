package com.rustor.feature.showcase.uiState

import com.rustor.core.common.error.AppException
import com.rustor.core.model.App
import com.rustor.core.model.CategoryId
import com.rustor.feature.showcase.error.ShowcaseError

internal fun computeFeedState(
    apps: List<App>?,
    syncStatus: SyncStatus,
    mode: ShowcaseMode,
    selectedCategory: CategoryId?,
): FeedState {
    if (apps == null) return FeedState.Loading

    if (apps.isNotEmpty()) {
        val isRefreshing = syncStatus == SyncStatus.Syncing(SyncTrigger.User)
        return FeedState.Content(apps, isRefreshing)
    }

    val isFiltered = mode is ShowcaseMode.Category || selectedCategory != null
    if (isFiltered) return FeedState.EmptyCategory

    return when (syncStatus) {
        is SyncStatus.Syncing -> FeedState.Loading
        is SyncStatus.Failed -> FeedState.Error(syncStatus.error.toShowcaseError())
        SyncStatus.Idle -> FeedState.EmptyCatalog
    }
}

internal fun AppException.toShowcaseError(): ShowcaseError = when (this) {
    is AppException.NoInternet, is AppException.Timeout -> ShowcaseError.NoConnection
    is AppException.Parsing -> ShowcaseError.ServiceUnavailable
    is AppException.Unknown -> ShowcaseError.Unknown
}
