package com.rustor.feature.showcase.uiState

import com.rustor.core.common.error.toUiError
import com.rustor.core.model.App
import com.rustor.core.model.CategoryId

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
        is SyncStatus.Failed -> FeedState.Error(syncStatus.error.toUiError())
        SyncStatus.Idle -> FeedState.EmptyCatalog
    }
}
